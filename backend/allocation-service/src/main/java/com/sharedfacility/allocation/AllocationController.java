
package com.sharedfacility.allocation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    private final CostAllocationRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AllocationController(
            CostAllocationRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/calculate")
    public Map<String, Object> calculate(
            @RequestBody Map<String, Object> request) {

        Object totalValue = request.get("totalAmount");
        Object percentValue = request.get("commonAreaPercent");

        if (!(totalValue instanceof Number)
                || !(percentValue instanceof Number)) {
            throw new IllegalArgumentException(
                    "totalAmount and commonAreaPercent are required");
        }

        double totalAmount = ((Number) totalValue).doubleValue();
        double commonAreaPercent =
                ((Number) percentValue).doubleValue();

        if (!Double.isFinite(totalAmount)
                || !Double.isFinite(commonAreaPercent)
                || totalAmount < 0
                || commonAreaPercent < 0
                || commonAreaPercent > 100) {
            throw new IllegalArgumentException(
                    "Invalid amount or common area percentage");
        }

        Object invoiceValue = request.get("invoiceId");
        String invoiceId = invoiceValue == null
                ? null : invoiceValue.toString().trim();

        if (invoiceId != null && invoiceId.isEmpty()) {
            invoiceId = null;
        }

        double commonAreaCost =
                totalAmount * commonAreaPercent / 100.0;
        double departmentCost = totalAmount - commonAreaCost;

        double totalUsage = 420 + 180 + 400;

        double engineeringCost =
                departmentCost * 420 / totalUsage;
        double hrCost =
                departmentCost * 180 / totalUsage;
        double operationsCost =
                departmentCost - engineeringCost - hrCost;

        double allocatedTotal = commonAreaCost
                + engineeringCost
                + hrCost
                + operationsCost;

        CostAllocation allocation = new CostAllocation();
        allocation.setTotalAmount(decimal(totalAmount));
        allocation.setCommonAreaPercent(decimal(commonAreaPercent));
        allocation.setCommonAreaCost(decimal(commonAreaCost));
        allocation.setDepartmentCost(decimal(departmentCost));
        allocation.setEngineeringCost(decimal(engineeringCost));
        allocation.setHrCost(decimal(hrCost));
        allocation.setOperationsCost(decimal(operationsCost));
        allocation.setAllocatedTotal(decimal(allocatedTotal));
        allocation.setReconciled(
                Math.abs(totalAmount - allocatedTotal) < 0.01);

        CostAllocation saved = repository.save(allocation);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", saved.getId());
        result.put("invoiceId", invoiceId);
        result.put("totalAmount", totalAmount);
        result.put("commonAreaPercent", commonAreaPercent);
        result.put("commonAreaCost", decimal(commonAreaCost));
        result.put("departmentCost", decimal(departmentCost));

        Map<String, BigDecimal> departments = new LinkedHashMap<>();
        departments.put("Engineering", decimal(engineeringCost));
        departments.put("HR", decimal(hrCost));
        departments.put("Operations", decimal(operationsCost));

        result.put("departmentAllocations", departments);
        result.put("allocatedTotal", decimal(allocatedTotal));
        result.put("reconciled", saved.isReconciled());
        result.put("savedToDatabase", true);

        if (saved.isReconciled()) {
            Map<String, Object> event = new LinkedHashMap<>(result);
            event.put("eventType", "ALLOCATION_COMPLETED");

            try {
                String message =
                        objectMapper.writeValueAsString(event);

                kafkaTemplate.send(
                        "allocation-completed",
                        String.valueOf(saved.getId()),
                        message
                ).get();

            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        "Could not serialize allocation event", e);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Could not publish allocation event to Kafka", e);
            }
        }

        return result;
    }

    @GetMapping
    public List<CostAllocation> getAllAllocations() {
        return repository.findAll();
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
