package com.ayshriv.salescrm.customer.service.impl;

import com.ayshriv.salescrm.activity.dto.TimelineItemDto;
import com.ayshriv.salescrm.activity.entity.Activity;
import com.ayshriv.salescrm.activity.entity.ActivityType;
import com.ayshriv.salescrm.activity.repository.ActivityRepository;
import com.ayshriv.salescrm.activity.service.ActivityService;
import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.ayshriv.salescrm.ai.service.impl.AiChatServiceImpl;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.resources.LogConstants;
import com.ayshriv.salescrm.common.resources.Resources;
import com.ayshriv.salescrm.common.security.TenantContext;
import com.ayshriv.salescrm.common.security.TenantContextService;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.customer.dto.Customer360Dto;
import com.ayshriv.salescrm.customer.dto.Customer360SummaryDto;
import com.ayshriv.salescrm.customer.service.Customer360Service;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.task.entity.Task;
import com.ayshriv.salescrm.task.entity.TaskStatus;
import com.ayshriv.salescrm.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Customer360ServiceImpl implements Customer360Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Customer360ServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final LeadRepository leadRepository;
    private final DealRepository dealRepository;
    private final TaskRepository taskRepository;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    private final TenantContextService tenantContextService;
    private final LLMProvider llmProvider;

    public Customer360ServiceImpl(
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            LeadRepository leadRepository,
            DealRepository dealRepository,
            TaskRepository taskRepository,
            ActivityRepository activityRepository,
            ActivityService activityService,
            TenantContextService tenantContextService,
            LLMProvider llmProvider
    ) {
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.leadRepository = leadRepository;
        this.dealRepository = dealRepository;
        this.taskRepository = taskRepository;
        this.activityRepository = activityRepository;
        this.activityService = activityService;
        this.tenantContextService = tenantContextService;
        this.llmProvider = llmProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiStatus getCustomer360(Long customerId) {
        LOGGER.info("Customer360Service >> getCustomer360 called for customerId: {}", customerId);
        try {
            if (customerId == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.PARAMETER_MISSING + "customerId", LogConstants.CUSTOMER);
            }

            Customer360Dto dto = aggregateCustomer360(customerId);
            if (dto == null || dto.getCompany() == null) {
                return Resources.setStatus(Constants.FAILURE, Constants.DETAIL_FAILURE, LogConstants.CUSTOMER);
            }

            ApiStatus status = Resources.setStatus(Constants.SUCCESS, Constants.DETAIL_SUCCESS, LogConstants.CUSTOMER_360);
            status.setCustomer360(dto);
            status.setCompany(dto.getCompany());
            status.setContacts(dto.getContacts());
            status.setDeals(dto.getDeals());
            status.setLeads(dto.getLeads());
            status.setTasks(dto.getTasks());
            status.setActivities(dto.getActivities());
            return status;

        } catch (Exception e) {
            LOGGER.error("Customer360Service >> getCustomer360 execution error: {}", e.getMessage(), e);
            return Resources.setStatus(Constants.ERROR, Constants.EXECUTION_ERROR + e.getMessage(), LogConstants.CUSTOMER);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Customer360Dto aggregateCustomer360(Long customerId) {
        if (customerId == null) {
            return null;
        }

        TenantContext context = tenantContextService.getCurrentContext();
        Long orgId = context != null ? context.getOrganizationId() : null;

        Company company;
        if (orgId != null) {
            company = companyRepository.findByIdAndOrganizationIdAndIsDeletedFalse(customerId, orgId).orElse(null);
        } else {
            company = companyRepository.findByIdAndIsDeletedFalse(customerId).orElse(null);
        }

        if (company == null) {
            LOGGER.warn("Customer360Service >> Company not found for ID: {} (OrgId: {})", customerId, orgId);
            return null;
        }

        Long finalOrgId = orgId != null ? orgId : company.getOrganization().getId();

        // 1. Contacts
        List<Contact> contacts = contactRepository.findByCompanyIdAndIsDeletedFalse(company.getId());

        // 2. Deals
        List<Deal> deals = dealRepository.findByCompanyIdAndIsDeletedFalse(company.getId());

        // 3. Leads (matched by company name)
        List<Lead> leads = new ArrayList<>();
        if (company.getName() != null && !company.getName().isBlank()) {
            leads = leadRepository.findAll().stream()
                    .filter(l -> !Boolean.TRUE.equals(l.getIsDeleted()))
                    .filter(l -> l.getOrganization() != null && l.getOrganization().getId().equals(finalOrgId))
                    .filter(l -> l.getCompanyName() != null && l.getCompanyName().trim().equalsIgnoreCase(company.getName().trim()))
                    .collect(Collectors.toList());
        }

        // 4. Tasks (related to company or related to company deals/contacts/leads)
        Set<Long> dealIds = deals.stream().map(Deal::getId).collect(Collectors.toSet());
        Set<Long> contactIds = contacts.stream().map(Contact::getId).collect(Collectors.toSet());
        Set<Long> leadIds = leads.stream().map(Lead::getId).collect(Collectors.toSet());

        List<Task> allOrgTasks = taskRepository.findByOrganizationIdAndIsDeletedFalse(finalOrgId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        List<Task> companyTasks = allOrgTasks.stream()
                .filter(t -> {
                    if ("COMPANY".equalsIgnoreCase(t.getRelatedType()) && company.getId().equals(t.getRelatedId())) {
                        return true;
                    }
                    if ("DEAL".equalsIgnoreCase(t.getRelatedType()) && t.getRelatedId() != null && dealIds.contains(t.getRelatedId())) {
                        return true;
                    }
                    if ("CONTACT".equalsIgnoreCase(t.getRelatedType()) && t.getRelatedId() != null && contactIds.contains(t.getRelatedId())) {
                        return true;
                    }
                    if ("LEAD".equalsIgnoreCase(t.getRelatedType()) && t.getRelatedId() != null && leadIds.contains(t.getRelatedId())) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        // 5. Activities & Notes
        List<Activity> companyActivities = activityRepository.findByOrganizationIdAndCompanyIdAndIsDeletedFalseOrderByActivityDateDesc(finalOrgId, company.getId());
        Set<Long> existingActivityIds = companyActivities.stream().map(Activity::getId).collect(Collectors.toSet());

        List<Activity> allActivities = new ArrayList<>(companyActivities);
        for (Long dealId : dealIds) {
            List<Activity> dealActivities = activityRepository.findByOrganizationIdAndDealIdAndIsDeletedFalseOrderByActivityDateDesc(finalOrgId, dealId);
            for (Activity act : dealActivities) {
                if (existingActivityIds.add(act.getId())) {
                    allActivities.add(act);
                }
            }
        }
        for (Long contactId : contactIds) {
            List<Activity> contactActivities = activityRepository.findByOrganizationIdAndContactIdAndIsDeletedFalseOrderByActivityDateDesc(finalOrgId, contactId);
            for (Activity act : contactActivities) {
                if (existingActivityIds.add(act.getId())) {
                    allActivities.add(act);
                }
            }
        }

        List<Activity> notes = allActivities.stream()
                .filter(a -> a.getType() == ActivityType.NOTE)
                .collect(Collectors.toList());

        // 6. Timeline
        List<TimelineItemDto> timeline = activityService.getCustomerTimeline(null, null, company.getId(), null);

        // 7. Calculate Aggregated Metrics
        BigDecimal totalPipeline = BigDecimal.ZERO;
        BigDecimal totalWon = BigDecimal.ZERO;
        for (Deal deal : deals) {
            if (deal.getAmount() != null) {
                totalPipeline = totalPipeline.add(deal.getAmount());
                if (deal.getStatus() == DealStatus.WON) {
                    totalWon = totalWon.add(deal.getAmount());
                }
            }
        }

        int openTasksCount = (int) companyTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING || t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();

        Customer360Dto dto = new Customer360Dto();
        dto.setCompany(company);
        dto.setContacts(contacts);
        dto.setDeals(deals);
        dto.setLeads(leads);
        dto.setTasks(companyTasks);
        dto.setActivities(allActivities);
        dto.setNotes(notes);
        dto.setTimeline(timeline != null ? timeline : new ArrayList<>());
        dto.setTotalContacts(contacts.size());
        dto.setTotalLeads(leads.size());
        dto.setTotalDeals(deals.size());
        dto.setTotalPipelineValue(totalPipeline);
        dto.setTotalWonValue(totalWon);
        dto.setOpenTasksCount(openTasksCount);
        dto.setTotalActivitiesCount(allActivities.size());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer360SummaryDto getCustomer360AiSummary(Long customerId) {
        LOGGER.info("Customer360Service >> getCustomer360AiSummary called for customerId: {}", customerId);

        Customer360Dto aggregation = aggregateCustomer360(customerId);
        if (aggregation == null || aggregation.getCompany() == null) {
            throw new IllegalArgumentException("Customer/Company not found with id: " + customerId);
        }

        Company company = aggregation.getCompany();

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Customer 360 CRM Data for Company: ").append(company.getName()).append("\n");
        if (company.getIndustry() != null) promptBuilder.append("Industry: ").append(company.getIndustry()).append("\n");
        if (company.getDomain() != null) promptBuilder.append("Domain: ").append(company.getDomain()).append("\n");
        if (company.getWebsite() != null) promptBuilder.append("Website: ").append(company.getWebsite()).append("\n");

        promptBuilder.append("\nMetrics Overview:\n");
        promptBuilder.append("- Total Contacts: ").append(aggregation.getTotalContacts()).append("\n");
        promptBuilder.append("- Total Deals: ").append(aggregation.getTotalDeals()).append("\n");
        promptBuilder.append("- Total Pipeline Value: $").append(aggregation.getTotalPipelineValue()).append("\n");
        promptBuilder.append("- Total Won Deals Value: $").append(aggregation.getTotalWonValue()).append("\n");
        promptBuilder.append("- Open Tasks: ").append(aggregation.getOpenTasksCount()).append("\n");
        promptBuilder.append("- Total Activities Logged: ").append(aggregation.getTotalActivitiesCount()).append("\n");

        promptBuilder.append("\nKey Contacts:\n");
        for (Contact contact : aggregation.getContacts()) {
            promptBuilder.append("- ").append(contact.getFirstName()).append(" ").append(contact.getLastName())
                    .append(" (").append(contact.getEmail()).append("), Title: ").append(contact.getJobTitle()).append("\n");
        }

        promptBuilder.append("\nDeals:\n");
        for (Deal deal : aggregation.getDeals()) {
            promptBuilder.append("- Deal: '").append(deal.getTitle()).append("', Value: $").append(deal.getAmount())
                    .append(", Status: ").append(deal.getStatus())
                    .append(", Stage: ").append(deal.getPipelineStage() != null ? deal.getPipelineStage().getName() : "N/A").append("\n");
        }

        promptBuilder.append("\nRecent Notes & Activities:\n");
        for (Activity act : aggregation.getActivities().stream().limit(10).collect(Collectors.toList())) {
            promptBuilder.append("- [").append(act.getType()).append("] ").append(act.getTitle())
                    .append(": ").append(act.getDescription()).append("\n");
        }

        promptBuilder.append("\nOpen Tasks:\n");
        for (Task task : aggregation.getTasks().stream().filter(t -> t.getStatus() != TaskStatus.COMPLETED).collect(Collectors.toList())) {
            promptBuilder.append("- Task: '").append(task.getTitle()).append("', Priority: ").append(task.getPriority())
                    .append(", Due: ").append(task.getDueDate()).append("\n");
        }

        String userPrompt = "Please generate a professional Customer 360 relationship summary based on the above CRM data. " +
                "Include: (1) Account Overview & Health, (2) Key Relationship Highlights, and (3) Strategic Next Steps / Follow-ups.";

        String aiResponse = llmProvider.generateText(AiChatServiceImpl.SYSTEM_PROMPT, promptBuilder.toString() + "\n\n" + userPrompt);

        Customer360SummaryDto summaryDto = new Customer360SummaryDto(
                company.getId(),
                company.getName(),
                aiResponse != null ? aiResponse : "Summary generation complete.",
                aggregation
        );
        summaryDto.setDomain(company.getDomain());
        summaryDto.setIndustry(company.getIndustry());

        return summaryDto;
    }
}
