package com.example.fhirpdf;

import java.time.LocalDate;
import java.util.Map;

public class TaxCodeGeneratorItalian {

    private static final Map<String, String> COMUNE_CODICI = Map.of(
            "ROMA", "H501",
            "MILANO", "F205",
            "NAPOLI", "F839",
            "TORINO", "L219",
            "PALERMO", "G273"
    );
    private static final char[] CONTROL_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int[] ODD_POSITION_VALUES = {
            1,0,5,7,9,13,15,17,19,21,2,4,18,20,11,3,6,8,12,14,16,10,22,25,24,23
    };
    private static final int[] EVEN_POSITION_VALUES = {
            0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25
    };

    public static String generate(String nome, String cognome, LocalDate dataNascita, String sesso, String comune) {
        String codice = "";

        codice += getSurnameCode(cognome);
        codice += getNameCode(nome);
        codice += getDateCode(dataNascita, sesso);
        codice += getComuneCode(comune);
        codice += getControlChar(codice);

        return codice;
    }

    private static String getSurnameCode(String surname) {
        return getConsonantVowelCode(surname, 3);
    }

    private static String getNameCode(String name) {
        String consonants = extractConsonants(name);
        if (consonants.length() >= 4) {
            // prendi la 1a, 3a e 4a consonante
            return "" + consonants.charAt(0) + consonants.charAt(2) + consonants.charAt(3);
        }
        return getConsonantVowelCode(name, 3);
    }

    private static String getConsonantVowelCode(String input, int length) {
        String consonants = extractConsonants(input);
        String vowels = extractVowels(input);
        String result = (consonants + vowels + "XXX").toUpperCase();
        return result.substring(0, length);
    }

    private static String extractConsonants(String input) {
        return input.toUpperCase().replaceAll("[^BCDFGHJKLMNPQRSTVWXYZ]", "");
    }

    private static String extractVowels(String input) {
        return input.toUpperCase().replaceAll("[^AEIOU]", "");
    }

    private static String getDateCode(LocalDate date, String gender) {
        String year = String.valueOf(date.getYear()).substring(2);
        String month = "ABCDEHLMPRST".charAt(date.getMonthValue() - 1) + "";
        int day = date.getDayOfMonth();
        if (gender.equalsIgnoreCase("F")) {
            day += 40;
        }
        return year + month + String.format("%02d", day);
    }

    private static String getComuneCode(String comune) {
        String code = COMUNE_CODICI.get(comune.toUpperCase());
        if (code == null) {
            System.err.println("⚠️ Comune non riconosciuto: " + comune + " " +
                    "— uso codice predefinito Z999");
            return "Z999"; // placeholder for unknown
        }
        return code;
    }

    private static String getControlChar(String partialCode) {
        int sum = 0;
        for (int i = 0; i < partialCode.length(); i++) {
            char c = partialCode.charAt(i);
            int index = c - 'A';
            if (Character.isDigit(c)) {
                index = c - '0' + 26;
            }

            if ((i + 1) % 2 == 0) {
                sum += EVEN_POSITION_VALUES[index];
            } else {
                sum += ODD_POSITION_VALUES[index];
            }
        }
        return "" + CONTROL_CODE_CHARS[sum % 26];
    }




}
