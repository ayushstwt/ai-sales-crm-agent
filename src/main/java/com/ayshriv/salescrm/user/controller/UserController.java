package com.ayshriv.salescrm.user.controller;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.user.dto.UserCreateRequest;
import com.ayshriv.salescrm.user.dto.UserSearchRequest;
import com.ayshriv.salescrm.user.dto.UserUpdateRequest;
import com.ayshriv.salescrm.user.service.UserService;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<MappingJacksonValue> listUsers(@ModelAttribute UserSearchRequest request) {
        ApiStatus status = userService.listUsers(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "users", "total"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> viewUser(@PathVariable Long id) {
        ApiStatus status = userService.viewUser(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "user"));
    }

    @PostMapping
    public ResponseEntity<MappingJacksonValue> createUser(@Valid @RequestBody UserCreateRequest request) {
        ApiStatus status = userService.createUser(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "user"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> editUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        ApiStatus status = userService.editUser(id, request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "user"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MappingJacksonValue> deleteUser(@PathVariable Long id) {
        ApiStatus status = userService.deleteUser(id);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
