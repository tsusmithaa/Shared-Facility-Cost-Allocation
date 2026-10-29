
package com.sharedfacility.workflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowRecordRepository repository;

    public WorkflowController(WorkflowRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "workflow-service");
        result.put("status", "RUNNING");
        result.put("message", "Workflow service is ready");
        return result;
    }

    @PostMapping("/update")
    public WorkflowRecord updateStatus(
            @RequestBody Map<String, String> request) {

        WorkflowRecord record = new WorkflowRecord();
        record.setEventType(
                request.getOrDefault("eventType", "WORKFLOW_UPDATE"));
        record.setInvoiceId(request.get("invoiceId"));
        record.setWorkflowStatus(
                request.getOrDefault("status", "PENDING"));
        record.setDetails(request.getOrDefault("details", ""));

        return repository.save(record);
    }

    @GetMapping
    public List<WorkflowRecord> getAllWorkflows() {
        return repository.findAll();
    }
}
