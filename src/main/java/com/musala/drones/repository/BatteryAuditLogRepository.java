package com.musala.drones.repository;

import com.musala.drones.model.BatteryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryAuditLogRepository extends JpaRepository<BatteryAuditLog,Long> {
}
