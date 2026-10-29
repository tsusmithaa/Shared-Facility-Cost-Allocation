
package com.sharedfacility.workflow;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_events")
public class WorkflowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "invoice_id")
    private String invoiceId;

    @Column(name = "workflow_status", nullable = false)
    private String workflowStatus;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public WorkflowRecord() {
    }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getInvoiceId() { return invoiceId; }
    public String getWorkflowStatus() { return workflowStatus; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setEventType(String value) { eventType = value; }
    public void setInvoiceId(String value) { invoiceId = value; }
    public void setWorkflowStatus(String value) { workflowStatus = value; }
    public void setDetails(String value) { details = value; }
}
