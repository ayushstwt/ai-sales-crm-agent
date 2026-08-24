package com.ayshriv.salescrm.audit.controller;

import com.ayshriv.salescrm.audit.dto.AuditLogSearchRequest;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listAuditLogs(@ModelAttribute AuditLogSearchRequest request) {
        ApiStatus status = auditLogService.listAuditLogs(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "auditLogs", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewAuditLog(@PathVariable Long id) {
        ApiStatus status = auditLogService.viewAuditLog(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "auditLog"));
    }
}
