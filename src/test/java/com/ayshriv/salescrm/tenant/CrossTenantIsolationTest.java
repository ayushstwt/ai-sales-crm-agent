package com.ayshriv.salescrm.tenant;

import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.lead.dto.LeadConvertRequest;
import com.ayshriv.salescrm.lead.entity.Lead;
import com.ayshriv.salescrm.lead.entity.LeadStatus;
import com.ayshriv.salescrm.lead.repository.LeadRepository;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.pipeline.entity.Pipeline;
import com.ayshriv.salescrm.pipeline.entity.PipelineStage;
import com.ayshriv.salescrm.pipeline.repository.PipelineRepository;
import com.ayshriv.salescrm.pipeline.repository.PipelineStageRepository;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CrossTenantIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private LeadRepository leadRepository;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineStageRepository pipelineStageRepository;

    @Autowired
    private DealRepository dealRepository;

    private Organization orgA;
    private Organization orgB;
    private User userA;
    private User userB;

    private Company companyB;
    private Contact contactB;
    private Lead leadB;
    private Pipeline pipelineB;
    private Deal dealB;

    @BeforeEach
    void setUp() {
        // Clean up
        dealRepository.deleteAll();
        leadRepository.deleteAll();
        contactRepository.deleteAll();
        pipelineStageRepository.deleteAll();
        pipelineRepository.deleteAll();
        companyRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // 1. Create Org A and Org B
        orgA = organizationRepository.save(new Organization("Organization A", "org-a"));
        orgB = organizationRepository.save(new Organization("Organization B", "org-b"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        userA = new User();
        userA.setOrganization(orgA);
        userA.setUserType(adminType);
        userA.setEmail("admin@orga.com");
        userA.setPassword("password");
        userA.setFirstName("Admin");
        userA.setLastName("A");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setOrganization(orgB);
        userB.setUserType(adminType);
        userB.setEmail("admin@orgb.com");
        userB.setPassword("password");
        userB.setFirstName("Admin");
        userB.setLastName("B");
        userB = userRepository.save(userB);

        // 2. Create sample CRM data in Org B
        companyB = new Company();
        companyB.setOrganization(orgB);
        companyB.setName("OrgB Exclusive Company");
        companyB.setDomain("orgb.com");
        companyB.setIndustry("Finance");
        companyB = companyRepository.save(companyB);

        contactB = new Contact();
        contactB.setOrganization(orgB);
        contactB.setCompany(companyB);
        contactB.setFirstName("Bob");
        contactB.setLastName("Builder");
        contactB.setEmail("bob@orgb.com");
        contactB = contactRepository.save(contactB);

        leadB = new Lead();
        leadB.setOrganization(orgB);
        leadB.setFirstName("Alice");
        leadB.setLastName("Prospect");
        leadB.setEmail("alice@prospectb.com");
        leadB.setCompanyName("Prospect B Corp");
        leadB.setStatus(LeadStatus.NEW);
        leadB = leadRepository.save(leadB);

        pipelineB = new Pipeline();
        pipelineB.setOrganization(orgB);
        pipelineB.setName("Org B Pipeline");
        pipelineB.setIsDefault(true);
        pipelineB = pipelineRepository.save(pipelineB);

        PipelineStage stageB = new PipelineStage(pipelineB, "Stage 1", 1, 20.0);
        stageB = pipelineStageRepository.save(stageB);

        dealB = new Deal();
        dealB.setOrganization(orgB);
        dealB.setCompany(companyB);
        dealB.setContact(contactB);
        dealB.setPipelineStage(stageB);
        dealB.setTitle("Org B Secret Deal");
        dealB.setAmount(new BigDecimal("100000.00"));
        dealB.setStatus(DealStatus.OPEN);
        dealB = dealRepository.save(dealB);
    }

    private void authenticateAsOrgA() {
        UserPrincipal principal = new UserPrincipal(
                userA.getId(),
                orgA.getId(),
                userA.getEmail(),
                userA.getPassword(),
                ERole.ROLE_ORG_ADMIN.name()
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_ORG_ADMIN.name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Org A cannot fetch Org B's Company by ID")
    void testOrgACannotFetchOrgBCompany() throws Exception {
        authenticateAsOrgA();

        mockMvc.perform(get("/companies/" + companyB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.company").doesNotExist());
    }

    @Test
    @DisplayName("Org A cannot fetch Org B's Contact by ID")
    void testOrgACannotFetchOrgBContact() throws Exception {
        authenticateAsOrgA();

        mockMvc.perform(get("/contacts/" + contactB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.contact").doesNotExist());
    }

    @Test
    @DisplayName("Org A cannot fetch Org B's Lead by ID")
    void testOrgACannotFetchOrgBLead() throws Exception {
        authenticateAsOrgA();

        mockMvc.perform(get("/leads/" + leadB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.lead").doesNotExist());
    }

    @Test
    @DisplayName("Org A cannot fetch Org B's Deal by ID")
    void testOrgACannotFetchOrgBDeal() throws Exception {
        authenticateAsOrgA();

        mockMvc.perform(get("/deals/" + dealB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.deal").doesNotExist());
    }

    @Test
    @DisplayName("Org A cannot fetch Org B's Pipeline by ID")
    void testOrgACannotFetchOrgBPipeline() throws Exception {
        authenticateAsOrgA();

        mockMvc.perform(get("/pipelines/" + pipelineB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.pipeline").doesNotExist());
    }

    @Test
    @DisplayName("Org A's paginated lists do not leak Org B's records")
    void testOrgAListDoesNotContainOrgBData() throws Exception {
        authenticateAsOrgA();

        // List Companies for Org A (expect 0)
        mockMvc.perform(get("/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.companies").isEmpty());

        // List Contacts for Org A (expect 0)
        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.contacts").isEmpty());

        // List Leads for Org A (expect 0)
        mockMvc.perform(get("/leads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.leads").isEmpty());

        // List Deals for Org A (expect 0)
        mockMvc.perform(get("/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.deals").isEmpty());
    }

    @Test
    @DisplayName("Org A cannot update or delete Org B's entities")
    void testOrgACannotMutateOrgBData() throws Exception {
        authenticateAsOrgA();

        // Attempt update company B
        mockMvc.perform(put("/companies/" + companyB.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Hacked Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));

        Company unmodifiedCompany = companyRepository.findById(companyB.getId()).orElseThrow();
        assertThat(unmodifiedCompany.getName()).isEqualTo("OrgB Exclusive Company");

        // Attempt delete company B
        mockMvc.perform(delete("/companies/" + companyB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));

        Company stillAliveCompany = companyRepository.findById(companyB.getId()).orElseThrow();
        assertThat(stillAliveCompany.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("Org A cannot convert Org B's Lead")
    void testOrgACannotConvertOrgBLead() throws Exception {
        authenticateAsOrgA();

        LeadConvertRequest convertRequest = new LeadConvertRequest();
        convertRequest.setCreateDeal(true);
        convertRequest.setDealTitle("Illegal Conversion");

        mockMvc.perform(post("/leads/" + leadB.getId() + "/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convertRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));

        Lead unmodifiedLead = leadRepository.findById(leadB.getId()).orElseThrow();
        assertThat(unmodifiedLead.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(unmodifiedLead.getConvertedContact()).isNull();
    }
}
