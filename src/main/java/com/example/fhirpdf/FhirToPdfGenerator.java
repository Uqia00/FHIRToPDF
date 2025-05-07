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
            System.out.println("Usage: java -jar FhirToCompletePdf.jar <input.json> <language: en/it> <type: patient/encounter>");
            return;
        }

        String basePath = "C:\\GitHub\\FHIRToPDF\\Input\\";
        String inputPath = basePath + args[0];
        String language = args[1].toLowerCase(Locale.ROOT);
        String mode = args[2].toLowerCase(Locale.ROOT);
        
        boolean hasPatient = false;
        boolean hasEncounter = false;

        if (!language.equals("en") && !language.equals("it")) {
            System.out.println("Supported languages are 'en' (English) and 'it' (Italian).\n");
            return;
        }

        String baseName = new File(inputPath).getName().replaceFirst("\\.json$", "");
        String outputPdfPath = Paths.get("output", baseName + "_" + language + ".pdf").toString();

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        InputStream inputStream = new FileInputStream(inputPath);
        Bundle bundle = parser.parseResource(Bundle.class, inputStream);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputPdfPath));
        document.open();

        // Add Logo
        Image logo = Image.getInstance("logo.png");
        logo.scaleToFit(100, 100);
        logo.setAlignment(Image.ALIGN_RIGHT);
        document.add(logo);

        // Choose which generator to use
        if (mode.equals("patient")) {
            new PatientPdfGenerator().generate(bundle, document, language);
            hasPatient = true;
        } else if (mode.equals("encounter")) {
            new EncounterPdfGenerator().generate(bundle, document, language);
            hasEncounter = true;
        } else if (mode.equals("both")) {
            new PatientPdfGenerator().generate(bundle, document, language);
            hasPatient = true;
            document.newPage();
            new EncounterPdfGenerator().generate(bundle, document, language);
            hasEncounter = true;
        } else {
            System.out.println("Unsupported type: " + mode);
        }

        document.close();
        String finalPath = PdfFhirUtils.appendSuffixToFilename(outputPdfPath, hasPatient, hasEncounter);
        new File(outputPdfPath).renameTo(new File(finalPath));

        System.out.println("PDF generated at: " + finalPath);
        System.out.println("PDF generated at: " + outputPdfPath);
    }
} 
