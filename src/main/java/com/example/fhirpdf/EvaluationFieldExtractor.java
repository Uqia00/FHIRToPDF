package com.example.fhirpdf;

import com.example.fhirpdf.utils.PdfFhirUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

public class EvaluationFieldExtractor {

    public static void generateFieldValueJson(Map<String, String> fieldMap, String jsonFilename, Boolean isPatient, Boolean isEncounter) throws Exception {
        if (fieldMap == null || fieldMap.isEmpty()) {
            System.out.println("No field values provided to write.");
            return;
        }

        if (!jsonFilename.endsWith(".json")) {
            jsonFilename += ".json";
        }

        File outputDir = new File("evaluationfield");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String finalPath = PdfFhirUtils.appendSuffixToFilename(jsonFilename, isPatient, isEncounter);
        File outputFile = new File(outputDir, finalPath);
        ObjectMapper mapper = new ObjectMapper();

        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(outputFile), fieldMap);

        System.out.println("Generated sensitive field JSON: " + outputFile.getAbsolutePath());
    }

    public static void generateWhitelistJson(Map<String, String> whitelistMap, String jsonFilename, Boolean isPatient, Boolean isEncounter) throws Exception {
        if (whitelistMap == null || whitelistMap.isEmpty()) {
            System.out.println("No whitelist field values provided to write.");
            return;
        }

        if (!jsonFilename.endsWith(".json")) {
            jsonFilename += ".json";
        }

        File outputDir = new File("whitelistfield");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String finalPath = PdfFhirUtils.appendSuffixToFilename(jsonFilename, isPatient, isEncounter);
        File outputFile = new File(outputDir, finalPath);
        ObjectMapper mapper = new ObjectMapper();

        mapper.writerWithDefaultPrettyPrinter().writeValue(new FileWriter(outputFile), whitelistMap);

        System.out.println("Generated whitelist field JSON: " + outputFile.getAbsolutePath());
    }

}
