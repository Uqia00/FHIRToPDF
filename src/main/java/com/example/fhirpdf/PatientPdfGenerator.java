package com.example.fhirpdf;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

public class PatientPdfGenerator {
	
    public void generate(Bundle bundle, Document document, String language) throws Exception {
    	
        Map<String, String> labels = getLabels(language);
        // Header
        document.add(new Paragraph(labels.get("hospital"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        document.add(new Paragraph(labels.get("department"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Paragraph(labels.get("professor"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(labels.get("reference"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(Chunk.NEWLINE);

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();
            if (resource instanceof Patient) {
                Patient patient = (Patient) resource;

                // Collect Patient Info
                String fullName = patient.getNameFirstRep().getNameAsSingleString();
                String firstName = patient.getNameFirstRep().getGivenAsSingleString();
                String lastName = patient.getNameFirstRep().getFamily();
                String gender = "N/A";

                if (patient.hasGender()) {
                    String genderCode = patient.getGender().toCode(); // "male", "female", "other", "unknown"
                    switch (genderCode.toLowerCase()) {
                        case "female":
                            gender = "it".equalsIgnoreCase(language) ? "Femmina" : "Female";
                            break;
                        case "male":
                            gender = "it".equalsIgnoreCase(language) ? "Maschio" : "Male";
                            break;
                        case "other":
                            gender = "it".equalsIgnoreCase(language) ? "Altro" : "Other";
                            break;
                        case "unknown":
                            gender = "it".equalsIgnoreCase(language) ? "Sconosciuto" : "Unknown";
                            break;
                    }
                }

                String birthDate = formatDate(patient.getBirthDate());
                String phone = patient.hasTelecom() ? patient.getTelecomFirstRep().getValue() : "N/A";
                Address addr = patient.hasAddress() ? patient.getAddressFirstRep() : null;
                String street = (addr != null && addr.hasLine()) ? addr.getLine().get(0).getValue() : "N/A";
                String city = (addr != null) ? addr.getCity() : "N/A";
                String state = (addr != null) ? addr.getState() : "N/A";
                String postalCode = (addr != null) ? addr.getPostalCode() : "N/A";
                String country = (addr != null) ? addr.getCountry() : "N/A";
                String addressUse = (addr != null && addr.hasUse()) ? getAddressUseLabel(addr.getUse().toCode(), language) : labels.get("home"); // Default to home/residenza

                String motherMaidenName = getMothersMaidenName(patient);
                String birthPlaceCity = getBirthPlace(patient, "city");
                String birthPlaceState = getBirthPlace(patient, "state");
                String birthPlaceCountry = getBirthPlace(patient, "country");

                String medicalRecordNumber = getIdentifier(patient, "MR");
                String ssn = getIdentifier(patient, "SS");
                String driverLicense = getIdentifier(patient, "DL");
                String passport = getIdentifier(patient, "PPN");
                String maritalStatus = patient.hasMaritalStatus() ? patient.getMaritalStatus().getText() : "N/A";

                String communicationLanguage = getCommunicationLanguage(patient, language);
                String latitude = (addr != null) ? getGeoCoordinate(addr, "latitude") : "N/A";
                String longitude = (addr != null) ? getGeoCoordinate(addr, "longitude") : "N/A";

                // Generate tax code
                String genderCode = "M";
                if ("female".equalsIgnoreCase(patient.getGender().toCode())) {
                    genderCode = "F";
                }
                String cfComune = (birthPlaceCity != null && !birthPlaceCity.equals("N/A")) ? birthPlaceCity : city;
                String taxCode = TaxCodeGeneratorItalian.generate(firstName, lastName, patient.getBirthDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), genderCode, cfComune);

                // Write patient info table
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                //addTableRow(table, labels.get("fullName"), fullName);
                addTableRow(table, labels.get("firstName"), firstName);
                addTableRow(table, labels.get("lastName"), lastName);
                addTableRow(table, labels.get("gender"), gender);
                addTableRow(table, labels.get("birthDate"), birthDate);
                addTableRow(table, labels.get("taxCode"), taxCode);
                addTableRow(table, labels.get("phone"), phone);
                
                //addTableRow(table, labels.get("addressUse"), addressUse);
                addTableRow(table, labels.get("latitude"), latitude);
                addTableRow(table, labels.get("longitude"), longitude);
                addTableRow(table, labels.get("street"), street);
                addTableRow(table, labels.get("city"), city);
                addTableRow(table, labels.get("state"), state);
                addTableRow(table, labels.get("postalCode"), postalCode);
                addTableRow(table, labels.get("country"), country);

                
                addTableRow(table, labels.get("motherMaidenName"), motherMaidenName);
                addTableRow(table, labels.get("birthPlaceCity"), birthPlaceCity);
                addTableRow(table, labels.get("birthPlaceState"), birthPlaceState);
                addTableRow(table, labels.get("birthPlaceCountry"), birthPlaceCountry);
                addTableRow(table, labels.get("medicalRecordNumber"), medicalRecordNumber);
                addTableRow(table, labels.get("ssn"), ssn);
                addTableRow(table, labels.get("driverLicense"), driverLicense);
                addTableRow(table, labels.get("passport"), passport);
                addTableRow(table, labels.get("maritalStatus"), maritalStatus);
                addTableRow(table, labels.get("communicationLanguage"), communicationLanguage);

                document.add(table);

                document.add(new Paragraph(labels.get("sincerely"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
                document.add(new Paragraph(labels.get("professor"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
                break; // only first patient
            }
        }

        document.close();
        System.out.println("PDF generated at: " + LocalDate.now());
    }
	
    private static Map<String, String> getLabels(String lang) {
        Map<String, String> en = Map.ofEntries(
            Map.entry("hospital", "Fictitious University Hospital"),
            Map.entry("department", "Department of Internal Medicine"),
            Map.entry("professor", "Prof. Malala Miller"),
            Map.entry("reference", "Re: Medical Record of Patient"),
            Map.entry("sincerely", "Sincerely,"),
            Map.entry("fullName", "Full Name:"),
            Map.entry("firstName", "First Name:"),
            Map.entry("lastName", "Last Name:"),
            Map.entry("gender", "Gender:"),
            Map.entry("birthDate", "Birth Date:"),
            Map.entry("phone", "Phone:"),
            Map.entry("street", "Street:"),
            Map.entry("city", "City:"),
            Map.entry("state", "State:"),
            Map.entry("postalCode", "Postal Code:"),
            Map.entry("country", "Country:"),
            Map.entry("motherMaidenName", "Mother's Maiden Name:"),
            Map.entry("birthPlaceCity", "Birth Place City:"),
            Map.entry("birthPlaceState", "Birth Place State:"),
            Map.entry("birthPlaceCountry", "Birth Place Country:"),
            Map.entry("medicalRecordNumber", "Medical Record Number:"),
            Map.entry("ssn", "Social Security Number:"),
            Map.entry("driverLicense", "Driver License Number:"),
            Map.entry("passport", "Passport Number:"),
            Map.entry("maritalStatus", "Marital Status:"),
            Map.entry("communicationLanguage", "Communication Language:"),
            Map.entry("latitude", "Latitude:"),
            Map.entry("longitude", "Longitude:"),
            Map.entry("addressUse", "Address Type:"),
                Map.entry("taxCode", "Tax Code:")        // for English


        );

        Map<String, String> it = Map.ofEntries(
            Map.entry("hospital", "Ospedale Universitario Fittizio"),
            Map.entry("department", "Dipartimento di Medicina Interna"),
            Map.entry("professor", "Prof.ssa Malala Miller"),
            Map.entry("reference", "Oggetto: Cartella clinica del paziente"),
            Map.entry("sincerely", "Cordiali saluti,"),
            Map.entry("fullName", "Nome completo:"),
            Map.entry("firstName", "Nome:"),
            Map.entry("lastName", "Cognome:"),
            Map.entry("gender", "Sesso:"),
            Map.entry("birthDate", "Data di nascita:"),
            Map.entry("phone", "Telefono:"),
            Map.entry("street", "Indirizzo:"),
            Map.entry("city", "Città:"),
            Map.entry("state", "Provincia:"),
            Map.entry("postalCode", "CAP:"),
            Map.entry("country", "Paese:"),
            Map.entry("motherMaidenName", "Nome e Cognome da nubile della madre:"),
            Map.entry("birthPlaceCity", "Città di nascita:"),
            Map.entry("birthPlaceState", "Provincia di nascita:"),
            Map.entry("birthPlaceCountry", "Paese di nascita:"),
            Map.entry("medicalRecordNumber", "Numero di cartella clinica:"),
            Map.entry("ssn", "Numero di previdenza sociale:"),
            Map.entry("driverLicense", "Numero patente:"),
            Map.entry("passport", "Numero passaporto:"),
            Map.entry("maritalStatus", "Stato civile:"),
            Map.entry("communicationLanguage", "Lingua di comunicazione:"),
            Map.entry("latitude", "Latitudine:"),
            Map.entry("longitude", "Longitudine:"),
            Map.entry("addressUse", "Tipo di indirizzo:"),
                Map.entry("taxCode", "Codice Fiscale:")  // for Italian

        );

        return lang.equals("it") ? it : en;
    }

    private static String formatDate(java.util.Date date) {
        if (date == null) return "N/A";
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return localDate.format(formatter);
    }

    private static void addTableRow(PdfPTable table, String key, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(key, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        table.addCell(cell1);
        table.addCell(cell2);
    }

    private static String getMothersMaidenName(Patient patient) {
        for (Extension ext : patient.getExtension()) {
            if (ext.getUrl().contains("patient-mothersMaidenName")) {
                return ext.getValueAsPrimitive().getValueAsString();
            }
        }
        return "N/A";
    }

    private static String getBirthPlace(Patient patient, String field) {
        for (Extension ext : patient.getExtension()) {
            if (ext.getUrl().contains("patient-birthPlace")) {
                Address birthPlace = (Address) ext.getValue();
                switch (field) {
                    case "city": return birthPlace.getCity();
                    case "state": return birthPlace.getState();
                    case "country": return birthPlace.getCountry();
                }
            }
        }
        return "N/A";
    }

    private static String getIdentifier(Patient patient, String code) {
        for (Identifier id : patient.getIdentifier()) {
            if (id.hasType() && id.getType().hasCoding()) {
                for (Coding coding : id.getType().getCoding()) {
                    if (coding.getCode().equals(code)) {
                        return id.getValue();
                    }
                }
            }
        }
        return "N/A";
    }

    private static String getCommunicationLanguage(Patient patient, String language) {
        if (patient.hasCommunication() && !patient.getCommunication().isEmpty()) {
            Patient.PatientCommunicationComponent comm = patient.getCommunicationFirstRep();
            String langCode = null;

            if (comm.hasLanguage() && comm.getLanguage().hasText()) {
                langCode = comm.getLanguage().getText().toLowerCase();
            } else if (comm.hasLanguage() && comm.getLanguage().hasCoding()) {
                langCode = comm.getLanguage().getCodingFirstRep().getDisplay().toLowerCase();
            }

            if (langCode != null) {
                // Basic translation map
                switch (langCode) {
                    case "english":
                        return "it".equalsIgnoreCase(language) ? "Inglese" : "English";
                    case "italian":
                        return "it".equalsIgnoreCase(language) ? "Italiano" : "Italian";
                    case "french":
                        return "it".equalsIgnoreCase(language) ? "Francese" : "French";
                    case "spanish":
                        return "it".equalsIgnoreCase(language) ? "Spagnolo" : "Spanish";
                    default:
                        // Return original with capital first letter
                        return langCode.substring(0, 1).toUpperCase() + langCode.substring(1);
                }
            }
        }
        return "N/A";
    }
    
    private static String getGeoCoordinate(Address address, String coordinateType) {
        if (address.hasExtension()) {
            for (Extension ext : address.getExtension()) {
                if (ext.getUrl().contains("geolocation")) {
                    for (Extension geoExt : ext.getExtension()) {
                        if (geoExt.getUrl().equals(coordinateType)) {
                            return geoExt.getValue().primitiveValue(); // returns string
                        }
                    }
                }
            }
        }
        return "N/A";
    }
    
    private static String getAddressUseLabel(String useCode, String language) {
        Map<String, String> en = Map.of(
            "home", "Home Address",
            "work", "Work Address",
            "temp", "Temporary Address",
            "old", "Previous Address"
        );

        Map<String, String> it = Map.of(
            "home", "Residenza",
            "work", "Indirizzo lavorativo",
            "temp", "Domicilio temporaneo",
            "old", "Vecchio indirizzo"
        );

        return (language.equals("it") ? it : en).getOrDefault(useCode, useCode);
    }
	
}
