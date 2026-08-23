package com.ayshriv.salescrm.user.controller;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.user.entity.UserLog;
import com.ayshriv.salescrm.user.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> logs(@ModelAttribute BaseSearchRequest request) {
        ApiStatus status = logService.logs(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "logs", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewLog(@PathVariable Long id) {
        ApiStatus status = logService.viewLog(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "log"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> addLog(@RequestBody UserLog log) {
        ApiStatus status = logService.addLog(log);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "log"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editLog(@PathVariable Long id, @RequestBody UserLog log) {
        ApiStatus status = logService.editLog(id, log);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "log"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteLog(@PathVariable Long id) {
        ApiStatus status = logService.deleteLog(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
