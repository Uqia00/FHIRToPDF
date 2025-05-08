package com.example.fhirpdf.utils;

import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class PdfFhirUtils {

    public static void addTableRow(PdfPTable table, String key, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(key, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        table.addCell(cell1);
        table.addCell(cell2);
    }

    public static String formatDateLocalized(Date date, String language) {
        if (date == null) return "N/A";
        Locale locale = language.equalsIgnoreCase("it") ? Locale.ITALIAN : Locale.ENGLISH;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter);
    }

    public static String formatDate(Date date) {
        if (date == null) return "N/A";
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }

    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static String translateGender(Enumerations.AdministrativeGender gender, String language) {
        if (gender == null) return "N/A";
        return switch (gender.toCode()) {
            case "male" -> language.equals("it") ? "Maschio" : "Male";
            case "female" -> language.equals("it") ? "Femmina" : "Female";
            case "other" -> language.equals("it") ? "Altro" : "Other";
            default -> language.equals("it") ? "Sconosciuto" : "Unknown";
        };
    }

    public static String translateEncounterStatus(String statusCode, String language) {
        if (statusCode == null) return "N/A";
        return switch (statusCode) {
            case "planned" -> language.equals("it") ? "Pianificato" : "Planned";
            case "arrived" -> language.equals("it") ? "Arrivato" : "Arrived";
            case "in-progress" -> language.equals("it") ? "In corso" : "In Progress";
            case "finished" -> language.equals("it") ? "Concluso" : "Finished";
            case "cancelled" -> language.equals("it") ? "Annullato" : "Cancelled";
            default -> statusCode;
        };
    }

    public static String getGeoCoordinate(Address address, String coordinateType) {
        if (address.hasExtension()) {
            for (Extension ext : address.getExtension()) {
                if (ext.getUrl().contains("geolocation")) {
                    for (Extension geoExt : ext.getExtension()) {
                        if (geoExt.getUrl().equals(coordinateType)) {
                            return geoExt.getValue().primitiveValue();
                        }
                    }
                }
            }
        }
        return "N/A";
    }

    public static String getExtensionValue(Patient patient, String extensionUrlPart) {
        for (Extension ext : patient.getExtension()) {
            if (ext.getUrl().contains(extensionUrlPart)) {
                return ext.getValueAsPrimitive().getValueAsString();
            }
        }
        return "N/A";
    }

    public static String getBirthPlace(Patient patient, String field) {
        for (Extension ext : patient.getExtension()) {
            if (ext.getUrl().contains("patient-birthPlace")) {
                Address birthPlace = (Address) ext.getValue();
                return switch (field) {
                    case "city" -> birthPlace.getCity();
                    case "state" -> birthPlace.getState();
                    case "country" -> birthPlace.getCountry();
                    default -> "N/A";
                };
            }
        }
        return "N/A";
    }
    
    public static String appendSuffixToFilename(String filename, boolean hasPatient, boolean hasEncounter) {
        String suffix = "";
        if (hasPatient) suffix += "_patient";
        if (hasEncounter) suffix += "_encounter";
        if (suffix.isEmpty()) return filename;

        int dotIndex = filename.lastIndexOf(".");
        return dotIndex != -1 ? filename.substring(0, dotIndex) + suffix + filename.substring(dotIndex)
                               : filename + suffix;
    }
    
}