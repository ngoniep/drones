package com.musala.drones.repository;

import com.musala.drones.model.BatteryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface BatteryAuditLogRepository extends JpaRepository<BatteryAuditLog,Long> {
    @Query(value = "select A.* from Battery_Audit_Log A " +
            " JOIN DRONE B ON A.drone_id=b.id WHERE b.serial_number=?1 ORDER BY created_time asc",nativeQuery = true)
    List<Map<String, Object>> getAuditForDrone(String droneSerialNumber);
}
