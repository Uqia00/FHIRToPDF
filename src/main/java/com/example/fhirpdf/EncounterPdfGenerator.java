package com.example.fhirpdf;

import java.util.Date;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import com.example.fhirpdf.utils.PdfFhirUtils;
import com.example.fhirpdf.utils.PdfLabelUtil;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

public class EncounterPdfGenerator {

    public void generate(Bundle bundle, Document document, String language) throws Exception {
        Map<String, String> labels = PdfLabelUtil.getLabels(language);

        document.add(new Paragraph(labels.get("reference"), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph(" "));

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Resource resource = entry.getResource();
            if (resource instanceof Encounter encounter) {

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                String encounterId = encounter.getIdElement().getIdPart();
                String status = PdfFhirUtils.translateEncounterStatus(encounter.getStatus().toCode(), language);

                Date startDate = encounter.getPeriod().getStart();
                Date endDate = encounter.getPeriod().getEnd();

                String startDateStr = PdfFhirUtils.formatDateLocalized(startDate, language);
                String startTimeStr = PdfFhirUtils.formatTime(PdfFhirUtils.toLocalDateTime(startDate));
                String endTimeStr = PdfFhirUtils.formatTime(PdfFhirUtils.toLocalDateTime(endDate));

                String classCode = encounter.getClass_().getCode();
                String classDisplay = classCode.equals("AMB")
                        ? (language.equals("it") ? "Ambulatoriale" : "Ambulatory")
                        : classCode;

                String typeDisplay = encounter.hasType() && !encounter.getType().isEmpty()
                        ? encounter.getTypeFirstRep().getText()
                        : "N/A";

                Reference subject = encounter.getSubject();
                String patientName = subject.hasDisplay() ? subject.getDisplay() : "N/A";
                String patientId = subject.getReference();

                EncounterParticipantComponent performer = encounter.getParticipantFirstRep();
                String doctorName = performer.getIndividual().getDisplay();
                String role = performer.getTypeFirstRep().getText();

                String performerStart = PdfFhirUtils.formatTime(
                        PdfFhirUtils.toLocalDateTime(performer.getPeriod().getStart()));
                String performerEnd = PdfFhirUtils.formatTime(
                        PdfFhirUtils.toLocalDateTime(performer.getPeriod().getEnd()));

                String serviceProvider = encounter.getServiceProvider().getDisplay();
                String serviceProviderRef = encounter.getServiceProvider().getReference();

                PdfFhirUtils.addTableRow(table, labels.get("encounterId"), encounterId);
                PdfFhirUtils.addTableRow(table, labels.get("encounterDate"), startDateStr);
                PdfFhirUtils.addTableRow(table, labels.get("time"), labels.get("from") + " " + startTimeStr + " " + labels.get("to") + " " + endTimeStr);
                PdfFhirUtils.addTableRow(table, labels.get("encounterStatus"), status);
                PdfFhirUtils.addTableRow(table, labels.get("encounterType"), typeDisplay);
                PdfFhirUtils.addTableRow(table, labels.get("class"), classDisplay);
                PdfFhirUtils.addTableRow(table, labels.get("patientName"), patientName);
                PdfFhirUtils.addTableRow(table, labels.get("patientId"), patientId);
                PdfFhirUtils.addTableRow(table, labels.get("doctorName"), doctorName);
                PdfFhirUtils.addTableRow(table, labels.get("role"), role);
                PdfFhirUtils.addTableRow(table, labels.get("visitPeriod"), labels.get("from") + " " + performerStart + " " + labels.get("to") + " " + performerEnd);
                PdfFhirUtils.addTableRow(table, labels.get("facility"), serviceProvider);
                PdfFhirUtils.addTableRow(table, labels.get("facilityId"), serviceProviderRef);

                document.add(table);
                document.add(new Paragraph(labels.get("sincerely"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
                document.add(new Paragraph(labels.get("professor"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
                break;
            }
        }


    }
}

