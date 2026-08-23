package com.ayshriv.salescrm.user.controller;

import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.dto.UserCreateRequest;
import com.ayshriv.salescrm.user.dto.UserUpdateRequest;
import com.ayshriv.salescrm.user.entity.ERole;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.UserLogRepository;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private UserLogRepository userLogRepository;

    private Organization testOrg;

    @BeforeEach
    void setUp() {
        userLogRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        testOrg = new Organization("Test Org", "test-org");
        testOrg = organizationRepository.save(testOrg);
    }

    @Test
    void testCreateAndListUsers() throws Exception {
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setEmail("rep@test.com");
        createRequest.setPassword("password123");
        createRequest.setFirstName("Sarah");
        createRequest.setLastName("Connor");
        createRequest.setPhone("+15551234");
        createRequest.setUserType(EUserType.SALES_REP.name());
        createRequest.setRole(ERole.ROLE_SALES_REP.name());
        createRequest.setOrganizationId(testOrg.getId());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.user.email").value("rep@test.com"));

        // List Users
        mockMvc.perform(get("/users")
                        .param("organizationId", testOrg.getId().toString())
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void testCreateSuperAdmin() throws Exception {
        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setEmail("superadmin@platform.com");
        createRequest.setPassword("adminpass123");
        createRequest.setFirstName("Super");
        createRequest.setLastName("Admin");
        createRequest.setUserType(EUserType.SUPER_ADMIN.name());
        createRequest.setRole(ERole.ROLE_SUPER_ADMIN.name());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.user.email").value("superadmin@platform.com"));

        User superUser = userRepository.findByEmail("superadmin@platform.com").orElseThrow();
        assertThat(superUser.getUserType().getName()).isEqualTo(EUserType.SUPER_ADMIN);
        assertThat(superUser.getRoles()).anyMatch(r -> r.getName() == ERole.ROLE_SUPER_ADMIN);
    }

    @Test
    void testViewEditAndDeleteUser() throws Exception {
        UserType repType = userTypeRepository.findByName(EUserType.SALES_REP).orElseGet(() ->
                userTypeRepository.save(new UserType(EUserType.SALES_REP, "Rep")));

        User user = new User();
        user.setOrganization(testOrg);
        user.setUserType(repType);
        user.setEmail("user1@test.com");
        user.setPassword("pass");
        user.setFirstName("User");
        user.setLastName("One");
        user = userRepository.save(user);

        // View
        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.user.email").value("user1@test.com"));

        // Edit
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("UpdatedFirstName");
        updateRequest.setUserType(EUserType.SALES_MANAGER.name());
        updateRequest.setRole(ERole.ROLE_SALES_MANAGER.name());

        mockMvc.perform(put("/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.user.firstName").value("UpdatedFirstName"));

        // Delete (Soft-Delete)
        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS));

        // Confirm row still exists in DB with is_deleted=true
        User deletedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(deletedUser.getIsDeleted()).isTrue();

        // Confirm filtered from view and list
        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));
    }
}
