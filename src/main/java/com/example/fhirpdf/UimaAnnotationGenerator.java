package com.example.fhirpdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r4.model.Patient;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UimaAnnotationGenerator {

    public static void generateUimaCasJson(Patient patient, String outputJsonPath, String sofaText) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();

        // UIMA type system
        root.put("%TYPES", getUimaTypes());

        // Sofa text
        root.put("_referenced_fss", Map.of("_InitialView", sofaText));

        // Annotations
        List<Map<String, Object>> featureStructures = new ArrayList<>();

        int idCounter = 1;
        int currentPos = 0;

        currentPos = addSpan("firstname", patient.getNameFirstRep().getGivenAsSingleString(), featureStructures, currentPos, idCounter++);
        currentPos = addSpan("lastname", patient.getNameFirstRep().getFamily(), featureStructures, currentPos, idCounter++);
        currentPos = addSpan("dateofbirth", patient.hasBirthDate() ? patient.getBirthDate().toString() : "N/A", featureStructures, currentPos, idCounter++);
        currentPos = addSpan("phonenumber", patient.hasTelecom() ? patient.getTelecomFirstRep().getValue() : "N/A", featureStructures, currentPos, idCounter++);
        currentPos = addSpan("ssn", getIdentifier(patient, "SS"), featureStructures, currentPos, idCounter++);
        currentPos = addSpan("driverlicense", getIdentifier(patient, "DL"), featureStructures, currentPos, idCounter++);
        currentPos = addSpan("taxcode", getIdentifier(patient, "PPN"), featureStructures, currentPos, idCounter++);

        // Dummy PdfPage annotation
        Map<String, Object> pdfPage = new LinkedHashMap<>();
        pdfPage.put("%ID", idCounter++);
        pdfPage.put("%TYPE", "org.dkpro.core.api.pdf.type.PdfPage");
        pdfPage.put("@sofa", 1);
        pdfPage.put("begin", 0);
        pdfPage.put("end", sofaText.length());
        pdfPage.put("pageNumber", 1);
        pdfPage.put("width", 595.0);
        pdfPage.put("height", 842.0);
        featureStructures.add(pdfPage);

        // Sofa definition
        Map<String, Object> sofa = new LinkedHashMap<>();
        sofa.put("%ID", 1);
        sofa.put("%TYPE", "uima.cas.Sofa");
        sofa.put("sofaNum", 1);
        sofa.put("sofaID", "_InitialView");
        sofa.put("mimeType", "text/plain");
        sofa.put("sofaString", sofaText);
        featureStructures.add(sofa);

        root.put("%FEATURE_STRUCTURES", featureStructures);

        // Views map (required by CAS format)
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("sofa", "_InitialView");
        List<Integer> fsIds = new ArrayList<>();
        for (Map<String, Object> fs : featureStructures) {
            if (fs.containsKey("%ID")) {
                fsIds.add((Integer) fs.get("%ID"));
            }
        }
        view.put("members", fsIds);
        root.put("_views", Map.of("_InitialView", view));

        // Write JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputJsonPath), root);
    }

    private static int addSpan(String label, String value, List<Map<String, Object>> annotations, int start, int id) {
        if (value == null || value.equals("N/A")) return start;
        int end = start + value.length();
        Map<String, Object> ann = new LinkedHashMap<>();
        ann.put("%ID", id);
        ann.put("%TYPE", "custom.Span");
        ann.put("@sofa", 1);
        ann.put("begin", start);
        ann.put("end", end);
        ann.put("label", label);
        annotations.add(ann);
        return end + 1;
    }

    private static String getIdentifier(Patient patient, String code) {
        return patient.getIdentifier().stream()
                .filter(id -> id.hasType() && id.getType().hasCoding() &&
                        id.getType().getCoding().stream().anyMatch(c -> c.getCode().equals(code)))
                .map(id -> id.getValue())
                .findFirst()
                .orElse("N/A");
    }

    private static Map<String, Object> getUimaTypes() {
        Map<String, Object> types = new LinkedHashMap<>();

        types.put("custom.Span", Map.of(
                "%NAME", "custom.Span",
                "%SUPER_TYPE", "uima.tcas.Annotation",
                "label", Map.of("%NAME", "label", "%RANGE", "uima.cas.String")
        ));

        types.put("org.dkpro.core.api.pdf.type.PdfPage", Map.of(
                "%NAME", "org.dkpro.core.api.pdf.type.PdfPage",
                "%SUPER_TYPE", "uima.tcas.Annotation",
                "width", Map.of("%NAME", "width", "%RANGE", "uima.cas.Float"),
                "height", Map.of("%NAME", "height", "%RANGE", "uima.cas.Float"),
                "pageNumber", Map.of("%NAME", "pageNumber", "%RANGE", "uima.cas.Integer")
        ));

        types.put("uima.cas.Sofa", Map.of(
                "%NAME", "uima.cas.Sofa",
                "%SUPER_TYPE", "uima.cas.TOP",
                "sofaNum", Map.of("%NAME", "sofaNum", "%RANGE", "uima.cas.Integer"),
                "sofaID", Map.of("%NAME", "sofaID", "%RANGE", "uima.cas.String"),
                "mimeType", Map.of("%NAME", "mimeType", "%RANGE", "uima.cas.String"),
                "sofaString", Map.of("%NAME", "sofaString", "%RANGE", "uima.cas.String")
        ));

        return types;
    }
}
