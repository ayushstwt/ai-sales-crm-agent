package com.ayshriv.salescrm.auth.service;

import com.ayshriv.salescrm.auth.dto.LoginRequest;
import com.ayshriv.salescrm.auth.dto.RegisterRequest;
import com.ayshriv.salescrm.common.resources.ApiStatus;

public interface AuthService {

    ApiStatus register(RegisterRequest request);

    ApiStatus login(LoginRequest request);
}
