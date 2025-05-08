package com.example.fhirpdf;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import com.example.fhirpdf.utils.PdfFhirUtils;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;
import org.hl7.fhir.r4.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FhirToPdfGenerator {
    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("Usage: java -jar FhirToCompletePdf.jar <input_folder> <language: en/it> <type: patient/encounter/both>");
            return;
        }

        String inputDirPath = args[0];
        String language = args[1].toLowerCase(Locale.ROOT);
        String mode = args[2].toLowerCase(Locale.ROOT);

        File inputDir = new File(inputDirPath);
        if (!inputDir.isDirectory()) {
            System.out.println("The input path must be a directory containing JSON files.");
            return;
        }

        if (!language.equals("en") && !language.equals("it")) {
            System.out.println("Supported languages are 'en' (English) and 'it' (Italian).");
            return;
        }

        File[] jsonFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No JSON files found in the directory: " + inputDirPath);
            return;
        }

        for (File jsonFile : jsonFiles) {
            String inputPath = jsonFile.getAbsolutePath();
            String baseName = jsonFile.getName().replaceFirst("\\.json$", "");
            String outputPdfPath = Paths.get("output", baseName + "_" + language + ".pdf").toString();

            System.out.println("Processing: " + jsonFile.getName());

            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();
            InputStream inputStream = new FileInputStream(jsonFile);
            Bundle bundle = parser.parseResource(Bundle.class, inputStream);

            boolean hasPatient = false;
            boolean hasEncounter = false;


            // Choose PDF generator
            if (mode.equals("patient")) {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(outputPdfPath));
                document.open();

                // Add Logo
                Image logo = Image.getInstance("logo.png");
                logo.scaleToFit(100, 100);
                logo.setAlignment(Image.ALIGN_RIGHT);
                document.add(logo);
                new PatientPdfGenerator().generate(bundle, document, language, baseName);
                hasPatient = true;
                document.close();

            } else if (mode.equals("encounter")) {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(outputPdfPath));
                document.open();

                // Add Logo
                Image logo = Image.getInstance("logo.png");
                logo.scaleToFit(100, 100);
                logo.setAlignment(Image.ALIGN_RIGHT);
                document.add(logo);
                new EncounterPdfGenerator().generate(bundle, document, language);
                hasEncounter = true;
                document.close();
            } else if (mode.equals("both")) {
                // Create separate files
                String patientPdfPath = Paths.get("output", baseName + "_patient_" + language + ".pdf").toString();
                String encounterPdfPath = Paths.get("output", baseName + "_encounter_" + language + ".pdf").toString();

                // --- PATIENT PDF ---
                Document patientDoc = new Document();
                PdfWriter.getInstance(patientDoc, new FileOutputStream(patientPdfPath));
                patientDoc.open();
                Image logo = Image.getInstance("logo.png");
                logo = Image.getInstance("logo.png");
                logo.scaleToFit(100, 100);
                logo.setAlignment(Image.ALIGN_RIGHT);
                patientDoc.add(logo);
                new PatientPdfGenerator().generate(bundle, patientDoc, language, baseName);
                patientDoc.close();
                hasPatient = true;

                // --- ENCOUNTER PDF ---
                Document encounterDoc = new Document();
                PdfWriter.getInstance(encounterDoc, new FileOutputStream(encounterPdfPath));
                encounterDoc.open();
                encounterDoc.add(logo); // riusa il logo
                new EncounterPdfGenerator().generate(bundle, encounterDoc, language);
                encounterDoc.close();
                hasEncounter = true;

                System.out.println("Generated patient PDF: " + patientPdfPath);
                System.out.println("Generated encounter PDF: " + encounterPdfPath);
                // NON FARE ALTRO
                continue; // <-- questo salta le righe sotto

            } else {
                System.out.println("Unsupported type: " + mode + ". Skipping " + jsonFile.getName());
                continue;
            }

            String finalPath = PdfFhirUtils.appendSuffixToFilename(outputPdfPath, hasPatient, hasEncounter);
            new File(outputPdfPath).renameTo(new File(finalPath));

            System.out.println("PDF generated at: " + finalPath);
        }

        System.out.println("Batch processing complete.");
    }
}
