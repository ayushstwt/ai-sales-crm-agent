package com.ayshriv.salescrm.user.service;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserLog;

import java.time.LocalDateTime;

public interface LogService {

    ApiStatus logs(BaseSearchRequest request);

    ApiStatus viewLog(Long id);

    ApiStatus addLog(UserLog log);

    ApiStatus editLog(Long id, UserLog log);

    ApiStatus deleteLog(Long id);

    UserLog createLog(User user, String action, String subAction, LocalDateTime createdOn, LocalDateTime updatedOn);
}
