package com.ayshriv.salescrm.auth.controller;

import com.ayshriv.salescrm.auth.dto.LoginRequest;
import com.ayshriv.salescrm.auth.dto.RegisterRequest;
import com.ayshriv.salescrm.auth.service.AuthService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Resources;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MappingJacksonValue> register(@Valid @RequestBody RegisterRequest request) {
        ApiStatus status = authService.register(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "token", "user", "organization"));
    }

    @PostMapping("/login")
    public ResponseEntity<MappingJacksonValue> login(@Valid @RequestBody LoginRequest request) {
        ApiStatus status = authService.login(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "token", "user", "organization"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MappingJacksonValue> verifyEmail(@Valid @RequestBody com.ayshriv.salescrm.auth.dto.VerifyEmailRequest request) {
        ApiStatus status = authService.verifyEmail(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "user"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MappingJacksonValue> resendVerification(@Valid @RequestBody com.ayshriv.salescrm.auth.dto.ResendVerificationRequest request) {
        ApiStatus status = authService.resendVerification(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "token"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MappingJacksonValue> forgotPassword(@Valid @RequestBody com.ayshriv.salescrm.auth.dto.ForgotPasswordRequest request) {
        ApiStatus status = authService.forgotPassword(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text", "token"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MappingJacksonValue> resetPassword(@Valid @RequestBody com.ayshriv.salescrm.auth.dto.ResetPasswordRequest request) {
        ApiStatus status = authService.resetPassword(request);
        return ResponseEntity.ok(Resources.formatedResponse(status, "statusType", "text"));
    }
}
