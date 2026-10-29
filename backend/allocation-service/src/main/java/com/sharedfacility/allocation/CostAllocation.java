
package com.sharedfacility.allocation;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cost_allocations")
public class CostAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "common_area_percent")
    private BigDecimal commonAreaPercent;

    @Column(name = "common_area_cost")
    private BigDecimal commonAreaCost;

    @Column(name = "department_cost")
    private BigDecimal departmentCost;

    @Column(name = "engineering_cost")
    private BigDecimal engineeringCost;

    @Column(name = "hr_cost")
    private BigDecimal hrCost;

    @Column(name = "operations_cost")
    private BigDecimal operationsCost;

    @Column(name = "allocated_total")
    private BigDecimal allocatedTotal;

    private boolean reconciled;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CostAllocation() {
    }

    public Long getId() { return id; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getCommonAreaPercent() { return commonAreaPercent; }
    public BigDecimal getCommonAreaCost() { return commonAreaCost; }
    public BigDecimal getDepartmentCost() { return departmentCost; }
    public BigDecimal getEngineeringCost() { return engineeringCost; }
    public BigDecimal getHrCost() { return hrCost; }
    public BigDecimal getOperationsCost() { return operationsCost; }
    public BigDecimal getAllocatedTotal() { return allocatedTotal; }
    public boolean isReconciled() { return reconciled; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTotalAmount(BigDecimal value) { totalAmount = value; }
    public void setCommonAreaPercent(BigDecimal value) { commonAreaPercent = value; }
    public void setCommonAreaCost(BigDecimal value) { commonAreaCost = value; }
    public void setDepartmentCost(BigDecimal value) { departmentCost = value; }
    public void setEngineeringCost(BigDecimal value) { engineeringCost = value; }
    public void setHrCost(BigDecimal value) { hrCost = value; }
    public void setOperationsCost(BigDecimal value) { operationsCost = value; }
    public void setAllocatedTotal(BigDecimal value) { allocatedTotal = value; }
    public void setReconciled(boolean value) { reconciled = value; }
}
