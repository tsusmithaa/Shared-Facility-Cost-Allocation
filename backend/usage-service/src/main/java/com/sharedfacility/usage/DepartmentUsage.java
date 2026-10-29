
package com.sharedfacility.usage;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "department_usage")
public class DepartmentUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String department;

    private Integer usageUnits;

    public DepartmentUsage() {
    }

    public Long getId() {
        return id;
    }

    public String getDepartment() {
        return department;
    }

    public Integer getUsageUnits() {
        return usageUnits;
    }
}
