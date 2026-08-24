package com.ayshriv.salescrm.pipeline.service.impl;

import com.ayshriv.salescrm.audit.entity.AuditSource;
import com.ayshriv.salescrm.audit.service.AuditLogService;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.pipeline.dto.PipelineCreateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineSearchRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineStageCreateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineStageUpdateRequest;
import com.ayshriv.salescrm.pipeline.dto.PipelineUpdateRequest;
import com.ayshriv.salescrm.pipeline.entity.Pipeline;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineRepository;
import com.ayshriv.salescrm.pipeline.repository.PipelineStageRepository;
import com.ayshriv.salescrm.pipeline.service.PipelineService;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PipelineServiceImpl implements PipelineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final LogService logService;
    private final AuditLogService auditLogService;

    public PipelineServiceImpl(
            PipelineRepository pipelineRepository,
            PipelineStageRepository pipelineStageRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            TenantContextService tenantContextService,
            LogService logService,
            AuditLogService auditLogService
    ) {
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.tenantContextService = tenantContextService;
        this.logService = logService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus listPipelines(PipelineSearchRequest request) {
        LOGGER.info("PipelineService >> listPipelines called!");
        try {
            request = (PipelineSearchRequest) Resources.getDefaultRequest(request);
            TenantContext context = tenantContextService.getCurrentContext();

            Sort.Direction direction = "DESC".equalsIgnoreCase(request.getOrderDir())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            String orderBy = (request.getOrderBy() != null && !request.getOrderBy().isBlank())
                    ? request.getOrderBy()
                    : "id";

            Pageable pageable = PageRequest.of(
                    Math.max(0, request.getPageNumber() - 1),
                    request.getPageSize(),
                    Sort.by(direction, orderBy)
            );

            Long targetOrgId = request.getOrganizationId();
            if (targetOrgId == null && context != null) {
                targetOrgId = context.getOrganizationId();
            }

            Page<Pipeline> page;
            if (targetOrgId != null) {
                page = pipelineRepository.findByOrganizationIdAndIsDeletedFalse(targetOrgId, pageable);
            } else {
                page = pipelineRepository.findByIsDeletedFalse(pageable);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.LIST_SUCCESS, LogConstants.PIPELINE);
            status.setPipelines(page.getContent());
            status.setTotal(page.getTotalElements());
            return status;

        } catch (Exception e) {
            LOGGER.error("PipelineService >> listPipelines exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus viewPipeline(Long id) {
        LOGGER.info("PipelineService >> viewPipeline called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Pipeline> pipelineOptional;
            if (context != null && context.getOrganizationId() != null) {
                pipelineOptional = pipelineRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                pipelineOptional = pipelineRepository.findByIdAndIsDeletedFalse(id);
            }

            if (pipelineOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.PIPELINE);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.PIPELINE);
            status.setPipeline(pipelineOptional.get());
            return status;

        } catch (Exception e) {
            LOGGER.error("PipelineService >> viewPipeline exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus createPipeline(PipelineCreateRequest request) {
        LOGGER.info("PipelineService >> createPipeline called for name: {}", request.getName());
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "name", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Long orgId = request.getOrganizationId();
            if (orgId == null && context != null) {
                orgId = context.getOrganizationId();
            }

            if (orgId == null) {
                return Resources.setStatus(Constants.FAILURE, "Organization ID is required.", null);
            }

            Optional<Organization> orgOptional = organizationRepository.findByIdAndIsDeletedFalse(orgId);
            if (orgOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Organization not found.", null);
            }

            Pipeline pipeline = new Pipeline();
            pipeline.setOrganization(orgOptional.get());
            pipeline.setName(request.getName().trim());
            pipeline.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
            pipeline.setIsActive(true);
            pipeline.setIsDeleted(false);

            List<PipelineStage> stages = new ArrayList<>();
            if (request.getStages() != null && !request.getStages().isEmpty()) {
                for (PipelineStageCreateRequest stageReq : request.getStages()) {
                    PipelineStage stage = new PipelineStage();
                    stage.setPipeline(pipeline);
                    stage.setName(stageReq.getName().trim());
                    stage.setOrderIndex(stageReq.getOrderIndex() != null ? stageReq.getOrderIndex() : 0);
                    stage.setProbability(stageReq.getProbability() != null ? stageReq.getProbability() : 0.0);
                    stage.setIsActive(true);
                    stage.setIsDeleted(false);
                    stages.add(stage);
                }
            } else {
                // Seed default stages
                stages.add(new PipelineStage(pipeline, "Prospecting", 1, 10.0));
                stages.add(new PipelineStage(pipeline, "Qualified", 2, 30.0));
                stages.add(new PipelineStage(pipeline, "Proposal", 3, 60.0));
                stages.add(new PipelineStage(pipeline, "Negotiation", 4, 80.0));
                stages.add(new PipelineStage(pipeline, "Closed Won", 5, 100.0));
                stages.add(new PipelineStage(pipeline, "Closed Lost", 6, 0.0));
            }
            pipeline.setStages(stages);

            Pipeline savedPipeline = pipelineRepository.save(pipeline);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.PIPELINE, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(savedPipeline.getOrganization(), currentUser, LogConstants.PIPELINE, savedPipeline.getId(), LogConstants.ADD, AuditSource.API, "Created pipeline: " + savedPipeline.getName());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.PIPELINE);
            status.setPipeline(savedPipeline);
            return status;

        } catch (Exception e) {
            LOGGER.error("PipelineService >> createPipeline exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editPipeline(Long id, PipelineUpdateRequest request) {
        LOGGER.info("PipelineService >> editPipeline called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Pipeline> pipelineOptional;
            if (context != null && context.getOrganizationId() != null) {
                pipelineOptional = pipelineRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                pipelineOptional = pipelineRepository.findByIdAndIsDeletedFalse(id);
            }

            if (pipelineOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.PIPELINE);
            }

            Pipeline pipeline = pipelineOptional.get();

            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                pipeline.setName(request.getName().trim());
            }
            if (request.getIsDefault() != null) {
                pipeline.setIsDefault(request.getIsDefault());
            }
            if (request.getIsActive() != null) {
                pipeline.setIsActive(request.getIsActive());
            }

            pipeline.setUpdatedOn(LocalDateTime.now());
            Pipeline updatedPipeline = pipelineRepository.save(pipeline);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.PIPELINE, LogConstants.EDIT, LocalDateTime.now(), null);
            auditLogService.logAction(updatedPipeline.getOrganization(), currentUser, LogConstants.PIPELINE, updatedPipeline.getId(), LogConstants.EDIT, AuditSource.API, "Updated pipeline: " + updatedPipeline.getName());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.PIPELINE);
            status.setPipeline(updatedPipeline);
            return status;

        } catch (Exception e) {
            LOGGER.error("PipelineService >> editPipeline exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deletePipeline(Long id) {
        LOGGER.info("PipelineService >> deletePipeline called for id: {}", id);
        try {
            if (id == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "id", null);
            }

            TenantContext context = tenantContextService.getCurrentContext();
            Optional<Pipeline> pipelineOptional;
            if (context != null && context.getOrganizationId() != null) {
                pipelineOptional = pipelineRepository.findByIdAndOrganizationIdAndIsDeletedFalse(id, context.getOrganizationId());
            } else {
                pipelineOptional = pipelineRepository.findByIdAndIsDeletedFalse(id);
            }

            if (pipelineOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.PIPELINE);
            }

            Pipeline pipeline = pipelineOptional.get();
            pipeline.setIsDeleted(true);
            pipeline.setUpdatedOn(LocalDateTime.now());
            pipelineRepository.save(pipeline);

            User currentUser = resolveCurrentUser(context);
            logService.createLog(currentUser, LogConstants.PIPELINE, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(pipeline.getOrganization(), currentUser, LogConstants.PIPELINE, pipeline.getId(), LogConstants.DELETE, AuditSource.API, "Soft-deleted pipeline: " + pipeline.getName());

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.PIPELINE);

        } catch (Exception e) {
            LOGGER.error("PipelineService >> deletePipeline exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus addStage(Long pipelineId, PipelineStageCreateRequest request) {
        LOGGER.info("PipelineService >> addStage called for pipelineId: {}", pipelineId);
        try {
            if (pipelineId == null || request.getName() == null || request.getName().trim().isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "pipelineId or name", null);
            }

            Optional<Pipeline> pipelineOptional = pipelineRepository.findByIdAndIsDeletedFalse(pipelineId);
            if (pipelineOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, "Pipeline not found.", null);
            }

            Pipeline pipeline = pipelineOptional.get();
            PipelineStage stage = new PipelineStage();
            stage.setPipeline(pipeline);
            stage.setName(request.getName().trim());
            stage.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
            stage.setProbability(request.getProbability() != null ? request.getProbability() : 0.0);
            stage.setIsActive(true);
            stage.setIsDeleted(false);

            PipelineStage savedStage = pipelineStageRepository.save(stage);

            User currentUser = resolveCurrentUser(tenantContextService.getCurrentContext());
            logService.createLog(currentUser, LogConstants.PIPELINE_STAGE, LogConstants.ADD, LocalDateTime.now(), null);
            auditLogService.logAction(pipeline.getOrganization(), currentUser, LogConstants.PIPELINE_STAGE, savedStage.getId(), LogConstants.ADD, AuditSource.API, "Added stage " + savedStage.getName() + " to pipeline " + pipeline.getName());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.SAVE_SUCCESS, LogConstants.PIPELINE_STAGE);
            status.setPipelineStage(savedStage);
            return status;

        } catch (Exception e) {
            LOGGER.error("PipelineService >> addStage exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus editStage(Long stageId, PipelineStageUpdateRequest request) {
        LOGGER.info("PipelineService >> editStage called for stageId: {}", stageId);
        try {
            if (stageId == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "stageId", null);
            }

            Optional<PipelineStage> stageOptional = pipelineStageRepository.findByIdAndIsDeletedFalse(stageId);
            if (stageOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.UPDATE_FAILURE, LogConstants.PIPELINE_STAGE);
            }

            PipelineStage stage = stageOptional.get();
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                stage.setName(request.getName().trim());
            }
            if (request.getOrderIndex() != null) {
                stage.setOrderIndex(request.getOrderIndex());
            }
            if (request.getProbability() != null) {
                stage.setProbability(request.getProbability());
            }
            if (request.getIsActive() != null) {
                stage.setIsActive(request.getIsActive());
            }
            stage.setUpdatedOn(LocalDateTime.now());

            PipelineStage updatedStage = pipelineStageRepository.save(stage);

            User currentUser = resolveCurrentUser(tenantContextService.getCurrentContext());
            logService.createLog(currentUser, LogConstants.PIPELINE_STAGE, LogConstants.EDIT, LocalDateTime.now(), null);
            auditLogService.logAction(stage.getPipeline().getOrganization(), currentUser, LogConstants.PIPELINE_STAGE, updatedStage.getId(), LogConstants.EDIT, AuditSource.API, "Updated stage: " + updatedStage.getName());

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.UPDATE_SUCCESS, LogConstants.PIPELINE_STAGE);
            status.setPipelineStage(updatedStage);
            return status;

        } catch (Exception e) {
            LOGGER.error("PipelineService >> editStage exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiStatus deleteStage(Long stageId) {
        LOGGER.info("PipelineService >> deleteStage called for stageId: {}", stageId);
        try {
            if (stageId == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "stageId", null);
            }

            Optional<PipelineStage> stageOptional = pipelineStageRepository.findByIdAndIsDeletedFalse(stageId);
            if (stageOptional.isEmpty()) {
                return Resources.setStatus(Constants.FAILURE, Constants.DELETE_FAILURE, LogConstants.PIPELINE_STAGE);
            }

            PipelineStage stage = stageOptional.get();
            stage.setIsDeleted(true);
            stage.setUpdatedOn(LocalDateTime.now());
            pipelineStageRepository.save(stage);

            User currentUser = resolveCurrentUser(tenantContextService.getCurrentContext());
            logService.createLog(currentUser, LogConstants.PIPELINE_STAGE, LogConstants.DELETE, LocalDateTime.now(), null);
            auditLogService.logAction(stage.getPipeline().getOrganization(), currentUser, LogConstants.PIPELINE_STAGE, stage.getId(), LogConstants.DELETE, AuditSource.API, "Soft-deleted stage: " + stage.getName());

            return Resources.setStatus(Constants.SUCCESS, Constants.DELETE_SUCCESS, LogConstants.PIPELINE_STAGE);

        } catch (Exception e) {
            LOGGER.error("PipelineService >> deleteStage exception: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), null);
        }
    }

    private User resolveCurrentUser(TenantContext context) {
        if (context != null && context.getUserId() != null) {
            return userRepository.findById(context.getUserId()).orElse(null);
        }
        return null;
    }
}
