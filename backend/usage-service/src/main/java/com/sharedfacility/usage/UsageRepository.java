
package com.sharedfacility.usage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageRepository
        extends JpaRepository<DepartmentUsage, Long> {
}
