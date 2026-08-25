package com.ayshriv.salescrm.customer.controller;

import com.ayshriv.salescrm.activity.entity.Activity;
import com.ayshriv.salescrm.activity.entity.ActivityType;
import com.ayshriv.salescrm.activity.repository.ActivityRepository;
import com.ayshriv.salescrm.ai.service.LLMProvider;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.task.entity.Task;
import com.ayshriv.salescrm.task.entity.TaskPriority;
import com.ayshriv.salescrm.task.entity.TaskStatus;
import com.ayshriv.salescrm.task.repository.TaskRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class Customer360ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @MockBean
    private LLMProvider llmProvider;

    private Organization testOrg;
    private User testUser;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        taskRepository.deleteAll();
        dealRepository.deleteAll();
        contactRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrg = organizationRepository.save(new Organization("Sales 360 Org", "sales-360-org"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        testUser = new User();
        testUser.setOrganization(testOrg);
        testUser.setUserType(adminType);
        testUser.setEmail("admin@sales360.com");
        testUser.setPassword("password");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);

        UserPrincipal principal = new UserPrincipal(
                testUser.getId(),
                testOrg.getId(),
                testUser.getEmail(),
                testUser.getPassword(),
                ERole.ROLE_ORG_ADMIN.name()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_ORG_ADMIN.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Seed Company
        testCompany = new Company();
        testCompany.setOrganization(testOrg);
        testCompany.setName("Globex Corporation");
        testCompany.setDomain("globex.com");
        testCompany.setIndustry("Manufacturing");
        testCompany = companyRepository.save(testCompany);

        // Seed Contact
        Contact contact = new Contact();
        contact.setOrganization(testOrg);
        contact.setCompany(testCompany);
        contact.setFirstName("Hank");
        contact.setLastName("Scorpio");
        contact.setEmail("hank@globex.com");
        contact.setJobTitle("CEO");
        contactRepository.save(contact);

        // Seed Deal
        Deal deal = new Deal();
        deal.setOrganization(testOrg);
        deal.setCompany(testCompany);
        deal.setContact(contact);
        deal.setTitle("Globex Expansion Deal");
        deal.setAmount(new BigDecimal("120000.00"));
        deal.setStatus(DealStatus.OPEN);
        dealRepository.save(deal);

        // Seed Task
        Task task = new Task();
        task.setOrganization(testOrg);
        task.setAssignedTo(testUser);
        task.setTitle("Quarterly account review");
        task.setRelatedType("COMPANY");
        task.setRelatedId(testCompany.getId());
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(TaskPriority.HIGH);
        taskRepository.save(task);

        // Seed Activity (Note)
        Activity activity = new Activity();
        activity.setOrganization(testOrg);
        activity.setUser(testUser);
        activity.setCompany(testCompany);
        activity.setType(ActivityType.NOTE);
        activity.setTitle("Executive briefing");
        activity.setDescription("Hank approved next phase evaluation.");
        activity.setActivityDate(LocalDateTime.now().minusDays(1));
        activityRepository.save(activity);
    }

    @Test
    @DisplayName("Stage 4 (Step 4.1): GET /customers/{id}/360 aggregates company, contacts, deals, tasks, activities into one payload")
    void testGetCustomer360Aggregation() throws Exception {
        mockMvc.perform(get("/customers/" + testCompany.getId() + "/360"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value("SUCCESS"))
                .andExpect(jsonPath("$.customer360").exists())
                .andExpect(jsonPath("$.customer360.company.name").value("Globex Corporation"))
                .andExpect(jsonPath("$.customer360.totalContacts").value(1))
                .andExpect(jsonPath("$.customer360.totalDeals").value(1))
                .andExpect(jsonPath("$.customer360.totalPipelineValue").value(120000.0))
                .andExpect(jsonPath("$.customer360.openTasksCount").value(1))
                .andExpect(jsonPath("$.customer360.totalActivitiesCount").value(1))
                .andExpect(jsonPath("$.customer360.contacts[0].firstName").value("Hank"))
                .andExpect(jsonPath("$.customer360.deals[0].title").value("Globex Expansion Deal"))
                .andExpect(jsonPath("$.customer360.tasks[0].title").value("Quarterly account review"))
                .andExpect(jsonPath("$.customer360.notes[0].title").value("Executive briefing"));
    }

    @Test
    @DisplayName("Stage 5 (Step 5.10): GET /customers/{id}/360/summary generates natural language AI summary")
    void testGetCustomer360AiSummaryEndpoint() throws Exception {
        when(llmProvider.generateText(anyString(), anyString())).thenReturn(
                "Globex Corporation is an enterprise account in Manufacturing. Key contact is Hank Scorpio (CEO). " +
                "Active pipeline includes 'Globex Expansion Deal' (,000). Account relationship is strong with pending quarterly review."
        );

        mockMvc.perform(get("/customers/" + testCompany.getId() + "/360/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(testCompany.getId()))
                .andExpect(jsonPath("$.companyName").value("Globex Corporation"))
                .andExpect(jsonPath("$.summary").value(org.hamcrest.Matchers.containsString("Globex Corporation is an enterprise account")))
                .andExpect(jsonPath("$.aggregation").exists())
                .andExpect(jsonPath("$.aggregation.totalDeals").value(1));
    }
}