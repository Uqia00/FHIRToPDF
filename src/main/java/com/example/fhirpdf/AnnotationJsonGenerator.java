package com.example.fhirpdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnnotationJsonGenerator {

    private final StringBuilder documentText;
    private final List<AnnotationEntity> entities;

    public AnnotationJsonGenerator() {
        this.documentText = new StringBuilder();
        this.entities = new ArrayList<>();
    }

    public int addText(String label, String text) {
        int start = documentText.length();
        documentText.append(label).append(" ").append(text).append("\n");
        int end = start + label.length() + 1 + text.length();
        return end;
    }

    public void addEntity(int start, int end, String entityLabel) {
        entities.add(new AnnotationEntity(start, end, entityLabel));
    }

    public void saveToFile(String outputJsonPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        root.put("_documentText", documentText.toString().trim());

        ObjectNode views = mapper.createObjectNode();
        ObjectNode initialView = mapper.createObjectNode();
        ArrayNode entitiesArray = mapper.createArrayNode();

        for (AnnotationEntity entity : entities) {
            ObjectNode entityNode = mapper.createObjectNode();
            entityNode.put("begin", entity.getStart());
            entityNode.put("end", entity.getEnd());
            entityNode.put("label", entity.getLabel());
            entitiesArray.add(entityNode);
        }

        initialView.set("entities", entitiesArray);
        views.set("_InitialView", initialView);
        root.set("_views", views);

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputJsonPath), root);
    }

    private static class AnnotationEntity {
        private final int start;
        private final int end;
        private final String label;

        public AnnotationEntity(int start, int end, String label) {
            this.start = start;
            this.end = end;
            this.label = label;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getLabel() {
            return label;
        }
    }
}
