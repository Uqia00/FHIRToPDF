package com.example.fhirpdf;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.hl7.fhir.r4.model.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class FhirToPdfGenerator {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java -jar FhirToPdfGenerator.jar <input.json> <output.pdf>");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        InputStream inputStream = new FileInputStream(inputPath);
        Bundle bundle = parser.parseResource(Bundle.class, inputStream);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        document.add(new Paragraph("📋 Cartella Clinica Completa", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(Chunk.NEWLINE);

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();

            if (resource instanceof Patient) {
                Patient patient = (Patient) resource;
                document.add(new Paragraph("👤 Paziente"));
                document.add(new Paragraph("Nome: " + patient.getNameFirstRep().getNameAsSingleString()));
                document.add(new Paragraph("Sesso: " + patient.getGender()));
                document.add(new Paragraph("Data di nascita: " + patient.getBirthDate()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Encounter) {
                Encounter encounter = (Encounter) resource;
                document.add(new Paragraph("🏥 Visita"));
                document.add(new Paragraph("Data: " + (encounter.hasPeriod() ? encounter.getPeriod().getStart() : "N/A")));
                document.add(new Paragraph("Tipo: " + encounter.getTypeFirstRep().getText()));
                document.add(new Paragraph("Medico: " + (encounter.hasParticipant() ? encounter.getParticipantFirstRep().getIndividual().getDisplay() : "N/A")));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Condition) {
                Condition condition = (Condition) resource;
                document.add(new Paragraph("🩺 Diagnosi"));
                document.add(new Paragraph("Condizione: " + condition.getCode().getText()));
                document.add(new Paragraph("Data diagnosi: " + condition.getRecordedDate()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Observation) {
                Observation observation = (Observation) resource;
                document.add(new Paragraph("📊 Osservazione"));
                document.add(new Paragraph("Tipo: " + observation.getCode().getText()));
                if (observation.hasValueQuantity()) {
                    document.add(new Paragraph("Valore: " + observation.getValueQuantity().getValue() + " " + observation.getValueQuantity().getUnit()));
                } else if (observation.hasValueStringType()) {
                    document.add(new Paragraph("Valore: " + observation.getValueStringType().getValue()));
                }
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof MedicationRequest) {
                MedicationRequest medRequest = (MedicationRequest) resource;
                document.add(new Paragraph("💊 Prescrizione"));
                document.add(new Paragraph("Farmaco: " + medRequest.getMedicationCodeableConcept().getText()));
                document.add(new Paragraph("Stato: " + medRequest.getStatus()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Immunization) {
                Immunization immunization = (Immunization) resource;
                document.add(new Paragraph("💉 Vaccinazione"));
                document.add(new Paragraph("Vaccino: " + immunization.getVaccineCode().getText()));
                document.add(new Paragraph("Data somministrazione: " + immunization.getOccurrenceDateTimeType().getValue()));
                document.add(new Paragraph("Stato: " + immunization.getStatus()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Procedure) {
                Procedure procedure = (Procedure) resource;
                document.add(new Paragraph("🔧 Procedura"));
                document.add(new Paragraph("Tipo: " + procedure.getCode().getText()));
                document.add(new Paragraph("Data: " + (procedure.hasPerformedDateTimeType() ? procedure.getPerformedDateTimeType().getValue() : "N/A")));
                document.add(new Paragraph("Stato: " + procedure.getStatus()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Device) {
                Device device = (Device) resource;
                document.add(new Paragraph("📟 Dispositivo"));
                document.add(new Paragraph("Tipo: " + device.getType().getText()));
                document.add(new Paragraph("Stato: " + device.getStatus()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof CareTeam) {
                CareTeam team = (CareTeam) resource;
                document.add(new Paragraph("👥 Team di cura"));
                document.add(new Paragraph("Nome: " + team.getName()));
                document.add(new Paragraph("Stato: " + team.getStatus()));
                document.add(new Paragraph("Componenti:"));
                for (CareTeam.CareTeamParticipantComponent member : team.getParticipant()) {
                    document.add(new Paragraph(" - " + member.getMember().getDisplay()));
                }
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof CarePlan) {
                CarePlan plan = (CarePlan) resource;
                document.add(new Paragraph("📘 Piano di cura"));
                document.add(new Paragraph("Titolo: " + plan.getTitle()));
                document.add(new Paragraph("Stato: " + plan.getStatus()));
                document.add(new Paragraph("Intento: " + plan.getIntent()));
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof Claim) {
                Claim claim = (Claim) resource;
                document.add(new Paragraph("💸 Claim"));
                document.add(new Paragraph("Tipo: " + claim.getType().getCodingFirstRep().getDisplay()));
                if (claim.hasTotal()) {
                    document.add(new Paragraph("Totale: " + claim.getTotal().getValue() + " " + claim.getTotal().getCurrency()));
                }
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof ExplanationOfBenefit) {
                ExplanationOfBenefit eob = (ExplanationOfBenefit) resource;
                document.add(new Paragraph("📑 EOB"));
                if (eob.hasTotal()) {
                    for (ExplanationOfBenefit.TotalComponent total : eob.getTotal()) {
                        document.add(new Paragraph("Totale " + total.getCategory().getText() + ": " +
                                total.getAmount().getValue() + " " + total.getAmount().getCurrency()));
                    }
                }
                document.add(Chunk.NEWLINE);
            } else if (resource instanceof SupplyDelivery) {
                SupplyDelivery delivery = (SupplyDelivery) resource;
                document.add(new Paragraph("📦 Consegna di materiali (SupplyDelivery)"));
                document.add(new Paragraph("Stato: " + delivery.getStatus()));

                if (delivery.hasType()) {
                    if (delivery.getType().hasText()) {
                        document.add(new Paragraph("Tipo: " + delivery.getType().getText()));
                    } else if (delivery.getType().hasCoding()) {
                        document.add(new Paragraph("Tipo: " + delivery.getType().getCodingFirstRep().getDisplay()));
                    }
                }

                if (delivery.hasSuppliedItem()) {
                    SupplyDelivery.SupplyDeliverySuppliedItemComponent item = delivery.getSuppliedItem();
                    if (item.hasItemCodeableConcept()) {
                        String itemName = item.getItemCodeableConcept().getText();
                        if (itemName == null || itemName.isEmpty()) {
                            itemName = item.getItemCodeableConcept().getCodingFirstRep().getDisplay();
                        }
                        document.add(new Paragraph("Oggetto fornito: " + itemName));
                    }
                    if (item.hasQuantity()) {
                        document.add(new Paragraph("Quantità: " + item.getQuantity().getValue()));
                    }
                }

                if (delivery.hasOccurrenceDateTimeType()) {
                    document.add(new Paragraph("Data consegna: " + delivery.getOccurrenceDateTimeType().getValue()));
                }

                document.add(Chunk.NEWLINE);
            }
                       
            else {
                document.add(new Paragraph("🔹 Risorsa non gestita: " + resource.getResourceType()));
                document.add(Chunk.NEWLINE);
            }
        }

        document.close();
        System.out.println("PDF generato in: " + outputPath);
    }
}
