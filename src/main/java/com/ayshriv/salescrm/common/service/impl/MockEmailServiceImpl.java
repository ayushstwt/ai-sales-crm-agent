package com.ayshriv.salescrm.common.service.impl;

import com.ayshriv.salescrm.common.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockEmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        LOGGER.info("""
                ==========================================================
                [EMAIL NOTIFICATION - VERIFICATION]
                To: {}
                Subject: Verify Your Email Address
                Body:
                Thank you for registering. Please use the following token to verify your email address:
                Verification Token: {}
                ==========================================================""", toEmail, token);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        LOGGER.info("""
                ==========================================================
                [EMAIL NOTIFICATION - PASSWORD RESET]
                To: {}
                Subject: Reset Your Password
                Body:
                We received a request to reset your password. Use the following token:
                Reset Token: {}
                ==========================================================""", toEmail, token);
    }
}
