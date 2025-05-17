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
        root.put("_documentText", sofaText);

        // Views with annotations
        Map<String, Object> initialView = new LinkedHashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();
        int idCounter = 1;

        Map<String, String> sensitiveFields = new LinkedHashMap<>();
        sensitiveFields.put("firstname", patient.getNameFirstRep().getGivenAsSingleString());
        sensitiveFields.put("lastname", patient.getNameFirstRep().getFamily());
        sensitiveFields.put("dateofbirth", patient.hasBirthDate() ? patient.getBirthDate().toString() : null);
        sensitiveFields.put("phonenumber", patient.hasTelecom() ? patient.getTelecomFirstRep().getValue() : null);
        sensitiveFields.put("ssn", getIdentifier(patient, "SS"));
        sensitiveFields.put("driverlicense", getIdentifier(patient, "DL"));
        sensitiveFields.put("taxcode", getIdentifier(patient, "PPN"));

        for (Map.Entry<String, String> entry : sensitiveFields.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.equals("N/A")) continue;

            int begin = sofaText.indexOf(value);
            if (begin == -1) continue;

            int end = begin + value.length();

            Map<String, Object> annotation = new LinkedHashMap<>();
            annotation.put("%ID", idCounter++);
            annotation.put("%TYPE", "custom.Span");
            annotation.put("@sofa", 1);
            annotation.put("begin", begin);
            annotation.put("end", end);
            annotation.put("label", label);
            entities.add(annotation);
        }

        // Add PdfPage span
        Map<String, Object> pdfPage = new LinkedHashMap<>();
        pdfPage.put("%ID", idCounter++);
        pdfPage.put("%TYPE", "org.dkpro.core.api.pdf.type.PdfPage");
        pdfPage.put("@sofa", 1);
        pdfPage.put("begin", 0);
        pdfPage.put("end", sofaText.length());
        pdfPage.put("pageNumber", 1);
        pdfPage.put("width", 595.0);
        pdfPage.put("height", 842.0);
        entities.add(pdfPage);

        // Sofa 1
        Map<String, Object> sofa1 = new LinkedHashMap<>();
        sofa1.put("%ID", 1);
        sofa1.put("%TYPE", "uima.cas.Sofa");
        sofa1.put("sofaNum", 1);
        sofa1.put("sofaID", "_InitialView");
        sofa1.put("mimeType", "text/plain");
        sofa1.put("sofaString", sofaText);
        entities.add(sofa1);

        // Simulated XML Sofa 2 and structure
        String xmlText = "<body>" + sofaText + "</body>";

        Map<String, Object> sofa2 = new LinkedHashMap<>();
        sofa2.put("%ID", idCounter);
        sofa2.put("%TYPE", "uima.cas.Sofa");
        sofa2.put("sofaNum", 2);
        sofa2.put("sofaID", "_XMLView");
        sofa2.put("mimeType", "application/xml");
        sofa2.put("sofaString", xmlText);
        entities.add(sofa2);
        int sofa2Id = idCounter++;

        int rootElemId = idCounter++;

        Map<String, Object> xmlElement = new LinkedHashMap<>();
        xmlElement.put("%ID", rootElemId);
        xmlElement.put("%TYPE", "org.dkpro.core.api.xml.type.XmlElement");
        xmlElement.put("@sofa", 2);
        xmlElement.put("begin", 0);
        xmlElement.put("end", xmlText.length());
        xmlElement.put("qName", "body");
        xmlElement.put("localName", "body");
        xmlElement.put("uri", "");
        entities.add(xmlElement);

        Map<String, Object> xmlDoc = new LinkedHashMap<>();
        xmlDoc.put("%ID", idCounter++);
        xmlDoc.put("%TYPE", "org.dkpro.core.api.xml.type.XmlDocument");
        xmlDoc.put("@sofa", 2);
        xmlDoc.put("begin", 0);
        xmlDoc.put("end", xmlText.length());
        xmlDoc.put("root", rootElemId);
        entities.add(xmlDoc);

        Map<String, Object> xmlTextNode = new LinkedHashMap<>();
        xmlTextNode.put("%ID", idCounter++);
        xmlTextNode.put("%TYPE", "org.dkpro.core.api.xml.type.XmlTextNode");
        xmlTextNode.put("@sofa", 2);
        xmlTextNode.put("begin", 6);
        xmlTextNode.put("end", xmlText.length() - 7);
        xmlTextNode.put("@parent", rootElemId);
        xmlTextNode.put("captured", true);
        entities.add(xmlTextNode);

        // Feature structures
        root.put("%FEATURE_STRUCTURES", entities);

        // Views
        Map<String, Object> view1 = new LinkedHashMap<>();
        view1.put("sofa", "_InitialView");
        view1.put("members", getIdsForSofa(entities, 1));

        Map<String, Object> view2 = new LinkedHashMap<>();
        view2.put("sofa", "_XMLView");
        view2.put("members", getIdsForSofa(entities, 2));

        Map<String, Object> views = new LinkedHashMap<>();
        views.put("_InitialView", view1);
        views.put("_XMLView", view2);

        root.put("_views", views);

        // Write JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputJsonPath), root);
        System.out.println("✅ Annotazione UIMA con XML simulato salvata: " + outputJsonPath);
    }

    private static List<Integer> getIdsForSofa(List<Map<String, Object>> annotations, int sofaNum) {
        List<Integer> ids = new ArrayList<>();
        for (Map<String, Object> ann : annotations) {
            if (ann.containsKey("%ID") && Objects.equals(ann.get("@sofa"), sofaNum)) {
                ids.add((Integer) ann.get("%ID"));
            }
        }
        return ids;
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

        types.put("org.dkpro.core.api.xml.type.XmlElement", Map.of(
                "%NAME", "org.dkpro.core.api.xml.type.XmlElement",
                "%SUPER_TYPE", "org.dkpro.core.api.xml.type.XmlNode",
                "qName", Map.of("%NAME", "qName", "%RANGE", "uima.cas.String"),
                "localName", Map.of("%NAME", "localName", "%RANGE", "uima.cas.String"),
                "uri", Map.of("%NAME", "uri", "%RANGE", "uima.cas.String")
        ));

        types.put("org.dkpro.core.api.xml.type.XmlDocument", Map.of(
                "%NAME", "org.dkpro.core.api.xml.type.XmlDocument",
                "%SUPER_TYPE", "uima.tcas.Annotation",
                "root", Map.of("%NAME", "root", "%RANGE", "org.dkpro.core.api.xml.type.XmlElement")
        ));

        types.put("org.dkpro.core.api.xml.type.XmlTextNode", Map.of(
                "%NAME", "org.dkpro.core.api.xml.type.XmlTextNode",
                "%SUPER_TYPE", "org.dkpro.core.api.xml.type.XmlNode",
                "captured", Map.of("%NAME", "captured", "%RANGE", "uima.cas.Boolean")
        ));

        types.put("org.dkpro.core.api.xml.type.XmlNode", Map.of(
                "%NAME", "org.dkpro.core.api.xml.type.XmlNode",
                "%SUPER_TYPE", "uima.tcas.Annotation",
                "parent", Map.of("%NAME", "parent", "%RANGE", "org.dkpro.core.api.xml.type.XmlElement")
        ));

        return types;
    }
}