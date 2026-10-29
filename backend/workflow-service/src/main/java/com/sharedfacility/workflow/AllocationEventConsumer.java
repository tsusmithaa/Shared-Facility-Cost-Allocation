
package com.sharedfacility.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AllocationEventConsumer {

    private final WorkflowRecordRepository repository;
    private final ObjectMapper objectMapper;

    public AllocationEventConsumer(
            WorkflowRecordRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "allocation-completed",
        groupId = "workflow-service"
    )
    public void consumeAllocationEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);

            WorkflowRecord record = new WorkflowRecord();
            record.setEventType(
                    event.path("eventType").asText("ALLOCATION_COMPLETED"));

            record.setInvoiceId(
                    event.hasNonNull("invoiceId")
                            ? event.get("invoiceId").asText()
                            : null);

            record.setWorkflowStatus(
                    event.path("reconciled").asBoolean(false)
                            ? "COMPLETED"
                            : "PENDING");

            record.setDetails(message);

            repository.save(record);

            System.out.println(
                    "Kafka allocation event saved to workflow database.");

        } catch (Exception e) {
            System.err.println(
                    "Failed to process allocation event: " + e.getMessage());
            throw new IllegalStateException(
                    "Could not process allocation event", e);
        }
    }
}
