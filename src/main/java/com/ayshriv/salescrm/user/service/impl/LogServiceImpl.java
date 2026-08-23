package com.ayshriv.salescrm.user.service.impl;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserLog;
import com.ayshriv.salescrm.user.repository.UserLogRepository;
import com.ayshriv.salescrm.user.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LogServiceImpl implements LogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    private final UserLogRepository userLogRepository;
    private final TenantContextService tenantContextService;

    public LogServiceImpl(UserLogRepository userLogRepository, TenantContextService tenantContextService) {
        this.userLogRepository = userLogRepository;
        this.tenantContextService = tenantContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus logs(BaseSearchRequest request) {
        LOGGER.info("LogService >> logs called!");
        try {
            request = Resources.getDefaultRequest(request);
            TenantContext context = tenantContextService.getCurrentContext();

            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getOrderDir()) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
            String orderBy = (request.getOrderBy() != null && !request.getOrderBy().isBlank()) 
                    ? request.getOrderBy() 
                    : "createdOn";

            Pageable pageable = PageRequest.of(
                    Math.max(0, request.getPageNumber() - 1),
                    request.getPageSize(),
                    Sort.by(direction, orderBy)
            );

            Page<UserLog> page;
            if (context != null && context.getUserId() != null) {
                page = userLogRepository.findByUserIdAndIsDeletedFalse(context.getUserId(), pageable);
            } else {
                page = userLogRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.LOG);
            status.setLogs(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("LogService >> logs exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewLog(Long id) {
        LOGGER.info("LogService >> viewLog called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            Optional<UserLog> logOptional = userLogRepository.findByIdAndIsDeletedFalse(id);
            if (logOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.LOG);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.LOG);
            status.setLog(logOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("LogService >> viewLog exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus addLog(UserLog log) {
        LOGGER.info("LogService >> addLog called!");
        try {
            if (log == null || log.getUser() == null || log.getAction() == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "user or action", null);
            }

            log.setIsActive(true);
            log.setIsDeleted(false);
            if (log.getCreatedOn() == null) {
                log.setCreatedOn(LocalDateTime.now());
            }
            UserLog savedLog = userLogRepository.save(log);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.LOG);
            status.setLog(savedLog);
            return status;

        } catch (Exception e) {
            LOGGER.error("LogService >> addLog exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editLog(Long id, UserLog log) {
        LOGGER.info("LogService >> editLog called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            Optional<UserLog> existingOptional = userLogRepository.findByIdAndIsDeletedFalse(id);
            if (existingOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.LOG);
            }

            UserLog existing = existingOptional.get();
            if (log.getAction() != null) {
                existing.setAction(log.getAction());
            }
            if (log.getSubAction() != null) {
                existing.setSubAction(log.getSubAction());
            }
            if (log.getIsActive() != null) {
                existing.setIsActive(log.getIsActive());
            }
            existing.setUpdatedOn(LocalDateTime.now());

            UserLog updatedLog = userLogRepository.save(existing);

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.LOG);
            status.setLog(updatedLog);
            return status;

        } catch (Exception e) {
            LOGGER.error("LogService >> editLog exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteLog(Long id) {
        LOGGER.info("LogService >> deleteLog called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            Optional<UserLog> existingOptional = userLogRepository.findByIdAndIsDeletedFalse(id);
            if (existingOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.LOG);
            }

            UserLog existing = existingOptional.get();
            existing.setIsDeleted(true);
            existing.setUpdatedOn(LocalDateTime.now());
            userLogRepository.save(existing);

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.LOG);

        } catch (Exception e) {
            LOGGER.error("LogService >> deleteLog exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public UserLog createLog(User user, String action, String subAction, LocalDateTime createdOn, LocalDateTime updatedOn) {
        try {
            if (user == null || action == null) {
                LOGGER.warn("LogService >> createLog skipped: user or action is null");
                return null;
            }
            UserLog log = new UserLog();
            log.setUser(user);
            log.setAction(action);
            log.setSubAction(subAction);
            log.setIsActive(true);
            log.setIsDeleted(false);
            log.setCreatedOn(createdOn != null ? createdOn : LocalDateTime.now());
            log.setUpdatedOn(updatedOn);
            return userLogRepository.save(log);
        } catch (Exception e) {
            LOGGER.error("LogService >> createLog failed to save log: {}", e.getMessage(), e);
            return null;
        }
    }
}
