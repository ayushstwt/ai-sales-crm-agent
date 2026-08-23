package com.ayshriv.salescrm.user.service;

import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.user.dto.UserCreateRequest;
import com.ayshriv.salescrm.user.dto.UserSearchRequest;
import com.ayshriv.salescrm.user.dto.UserUpdateRequest;

public interface UserService {

    ApiStatus listUsers(UserSearchRequest request);

    ApiStatus viewUser(Long id);

    ApiStatus createUser(UserCreateRequest request);

    ApiStatus editUser(Long id, UserUpdateRequest request);

    ApiStatus deleteUser(Long id);
}
