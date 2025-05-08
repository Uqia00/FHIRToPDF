package com.example.fhirpdf.utils;

import java.util.Locale;
import java.util.Map;

public class PdfLabelUtil {
    public static Map<String, String> getLabels(String lang) {
        return lang.equals("it") ? getItalianLabels() : getEnglishLabels();
    }

    private static Map<String, String> getEnglishLabels() {
        return Map.ofEntries(
            Map.entry("hospital", "Fictitious University Hospital"),
            Map.entry("department", "Department of Internal Medicine"),
            Map.entry("professor", "Prof. Malala Miller"),
            Map.entry("reference", "Re: Medical Record of Patient"),
            Map.entry("sincerely", "Sincerely,"),
            Map.entry("firstName", "First Name:"),
            Map.entry("lastName", "Last Name:"),
            Map.entry("gender", "Gender:"),
            Map.entry("birthDate", "Birth Date:"),
            Map.entry("taxCode", "Tax Code:"),
            Map.entry("phone", "Phone:"),
            Map.entry("latitude", "Latitude:"),
            Map.entry("longitude", "Longitude:"),
            Map.entry("street", "Street:"),
            Map.entry("city", "City:"),
            Map.entry("state", "State:"),
            Map.entry("postalCode", "Postal Code:"),
            Map.entry("country", "Country:"),
            Map.entry("motherMaidenName", "Mother's Maiden Name:"),
            Map.entry("birthPlaceCity", "Birth Place City:"),
            Map.entry("birthPlaceState", "Birth Place State:"),
            Map.entry("birthPlaceCountry", "Birth Place Country:"),
                Map.entry("encounterId", "Encounter ID:"),
                Map.entry("encounterDate", "Encounter Date:"),
                Map.entry("time", "Time:"),
                Map.entry("from", "from"),
                Map.entry("to", "to"),
                Map.entry("encounterStatus", "Encounter Status:"),
                Map.entry("encounterType", "Encounter Type:"),
                Map.entry("class", "Class:"),
                Map.entry("patientName", "Patient Name:"),
                Map.entry("patientId", "Patient ID:"),
                Map.entry("doctorName", "Doctor Name:"),
                Map.entry("role", "Role:"),
                Map.entry("visitPeriod", "Visit Period:"),
                Map.entry("facility", "Facility:"),
                Map.entry("facilityId", "Facility ID:")
        );
    }

    private static Map<String, String> getItalianLabels() {
        return Map.ofEntries(
            Map.entry("hospital", "Ospedale Universitario Fittizio"),
            Map.entry("department", "Dipartimento di Medicina Interna"),
            Map.entry("professor", "Prof.ssa Malala Miller"),
            Map.entry("reference", "Oggetto: Cartella clinica del paziente"),
            Map.entry("sincerely", "Cordiali saluti,"),
            Map.entry("firstName", "Nome:"),
            Map.entry("lastName", "Cognome:"),
            Map.entry("gender", "Sesso:"),
            Map.entry("birthDate", "Data di nascita:"),
            Map.entry("taxCode", "Codice Fiscale:"),
            Map.entry("phone", "Telefono:"),
            Map.entry("latitude", "Latitudine:"),
            Map.entry("longitude", "Longitudine:"),
            Map.entry("street", "Indirizzo:"),
            Map.entry("city", "Città:"),
            Map.entry("state", "Provincia:"),
            Map.entry("postalCode", "CAP:"),
            Map.entry("country", "Paese:"),
            Map.entry("motherMaidenName", "Nome e Cognome da nubile della madre:"),
            Map.entry("birthPlaceCity", "Città di nascita:"),
            Map.entry("birthPlaceState", "Provincia di nascita:"),
            Map.entry("birthPlaceCountry", "Paese di nascita:"),
            Map.entry("encounterId", "Identificativo Incontro:"),
                Map.entry("encounterDate", "Data Incontro:"),
                Map.entry("time", "Orario:"),
                Map.entry("from", "dalle"),
                Map.entry("to", "alle"),
                Map.entry("encounterStatus", "Stato Incontro:"),
                Map.entry("encounterType", "Tipo Incontro:"),
                Map.entry("class", "Classe:"),
                Map.entry("patientName", "Nome Paziente:"),
                Map.entry("patientId", "ID Paziente:"),
                Map.entry("doctorName", "Nome Medico:"),
                Map.entry("role", "Ruolo:"),
                Map.entry("visitPeriod", "Periodo Visita:"),
                Map.entry("facility", "Struttura:"),
                Map.entry("facilityId", "Identificativo Struttura:")
        );
    }


}
