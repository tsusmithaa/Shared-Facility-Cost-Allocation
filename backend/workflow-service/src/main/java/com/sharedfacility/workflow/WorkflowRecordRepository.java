
package com.sharedfacility.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRecordRepository
        extends JpaRepository<WorkflowRecord, Long> {
}
