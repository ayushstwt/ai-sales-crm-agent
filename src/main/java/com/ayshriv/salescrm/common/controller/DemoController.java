package com.ayshriv.salescrm.common.controller;

import com.ayshriv.salescrm.common.service.DemoDataSeeder;
import com.ayshriv.salescrm.organization.entity.Organization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoDataSeeder demoDataSeeder;

    public DemoController(DemoDataSeeder demoDataSeeder) {
        this.demoDataSeeder = demoDataSeeder;
    }

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedDemoData() {
        Organization org = demoDataSeeder.seedDemoData();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Demo dataset seeded successfully for Killer Demo Flow (master.md §10).",
                "organizationId", org.getId(),
                "organizationName", org.getName(),
                "adminEmail", "rahul@acme.com",
                "adminPassword", "password123"
        ));
    }
}
