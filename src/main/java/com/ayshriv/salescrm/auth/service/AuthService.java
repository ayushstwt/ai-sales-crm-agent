package com.ayshriv.salescrm.auth.service;

import com.ayshriv.salescrm.auth.dto.ForgotPasswordRequest;
import com.ayshriv.salescrm.auth.dto.LoginRequest;
import com.ayshriv.salescrm.auth.dto.RegisterRequest;
import com.ayshriv.salescrm.auth.dto.ResendVerificationRequest;
import com.ayshriv.salescrm.auth.dto.ResetPasswordRequest;
import com.ayshriv.salescrm.auth.dto.VerifyEmailRequest;
import com.ayshriv.salescrm.common.resources.ApiStatus;

public interface AuthService {

    ApiStatus register(RegisterRequest request);

    ApiStatus login(LoginRequest request);

    ApiStatus verifyEmail(VerifyEmailRequest request);

    ApiStatus resendVerification(ResendVerificationRequest request);

    ApiStatus forgotPassword(ForgotPasswordRequest request);

    ApiStatus resetPassword(ResetPasswordRequest request);
}
