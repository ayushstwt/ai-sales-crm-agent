package com.ayshriv.salescrm.contact.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.contact.dto.ContactCreateRequest;
import com.ayshriv.salescrm.contact.dto.ContactSearchRequest;
import com.ayshriv.salescrm.contact.dto.ContactUpdateRequest;
import com.ayshriv.salescrm.contact.service.ContactService;
import jakarta.validation.Valid;
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
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listContacts(@ModelAttribute ContactSearchRequest request) {
        ApiStatus status = contactService.listContacts(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "contacts", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewContact(@PathVariable Long id) {
        ApiStatus status = contactService.viewContact(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "contact"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createContact(@Valid @RequestBody ContactCreateRequest request) {
        ApiStatus status = contactService.createContact(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "contact"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editContact(@PathVariable Long id, @RequestBody ContactUpdateRequest request) {
        ApiStatus status = contactService.editContact(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "contact"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteContact(@PathVariable Long id) {
        ApiStatus status = contactService.deleteContact(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
