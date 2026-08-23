package com.ayshriv.salescrm.user.service;

import com.ayshriv.salescrm.common.dto.BaseSearchRequest;
import com.ayshriv.salescrm.common.resources.ApiStatus;
import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
import com.ayshriv.salescrm.user.entity.EUserType;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserLog;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.UserLogRepository;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LogServiceTest {

    @Autowired
    private LogService logService;

    @Autowired
    private UserLogRepository userLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userLogRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        Organization org = new Organization("Test Org", "test-org");
        org = organizationRepository.save(org);

        UserType userType = userTypeRepository.findByName(EUserType.SALES_REP).orElseGet(() -> {
            UserType ut = new UserType(EUserType.SALES_REP, "Sales Rep");
            return userTypeRepository.save(ut);
        });

        testUser = new User();
        testUser.setOrganization(org);
        testUser.setUserType(userType);
        testUser.setEmail("rep@test.com");
        testUser.setPassword("password");
        testUser.setFirstName("Jane");
        testUser.setLastName("Doe");
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateLogAndList() {
        UserLog created = logService.createLog(testUser, "LEAD", "ADD", LocalDateTime.now(), null);
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUserName()).isEqualTo("Jane Doe");
        assertThat(created.getUserType()).isEqualTo("SALES_REP");

        BaseSearchRequest request = new BaseSearchRequest();
        request.setPageNumber(1);
        request.setPageSize(10);

        ApiStatus status = logService.logs(request);
        assertThat(status.getStatusType()).isEqualTo(Constants.SUCCESS);
        assertThat(status.getLogs()).isNotEmpty();
        assertThat(status.getTotal()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void testViewLog() {
        UserLog created = logService.createLog(testUser, "DEAL", "EDIT", LocalDateTime.now(), null);

        ApiStatus status = logService.viewLog(created.getId());
        assertThat(status.getStatusType()).isEqualTo(Constants.SUCCESS);
        assertThat(status.getLog()).isNotNull();
        assertThat(status.getLog().getAction()).isEqualTo("DEAL");
        assertThat(status.getLog().getSubAction()).isEqualTo("EDIT");
    }

    @Test
    void testEditLog() {
        UserLog created = logService.createLog(testUser, "NOTE", "ADD", LocalDateTime.now(), null);

        UserLog updatePayload = new UserLog();
        updatePayload.setSubAction("ADD_NOTE_UPDATED");

        ApiStatus status = logService.editLog(created.getId(), updatePayload);
        assertThat(status.getStatusType()).isEqualTo(Constants.SUCCESS);
        assertThat(status.getLog().getSubAction()).isEqualTo("ADD_NOTE_UPDATED");
    }

    @Test
    void testDeleteLogSoftDelete() {
        UserLog created = logService.createLog(testUser, "TASK", "DELETE", LocalDateTime.now(), null);

        ApiStatus status = logService.deleteLog(created.getId());
        assertThat(status.getStatusType()).isEqualTo(Constants.SUCCESS);

        // Confirm row still exists in DB but is_deleted is true
        UserLog fromDb = userLogRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getIsDeleted()).isTrue();

        // Confirm viewLog and search filters out deleted log
        ApiStatus viewStatus = logService.viewLog(created.getId());
        assertThat(viewStatus.getStatusType()).isEqualTo(Constants.FAILURE);
    }
}
