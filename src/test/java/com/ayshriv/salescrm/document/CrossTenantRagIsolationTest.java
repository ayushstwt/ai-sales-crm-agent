package com.ayshriv.salescrm.document;

import com.ayshriv.salescrm.common.resources.Constants;
import com.ayshriv.salescrm.common.security.UserPrincipal;
import com.ayshriv.salescrm.document.dto.DocumentChunkDto;
import com.ayshriv.salescrm.document.dto.DocumentRetrievalResult;
import com.ayshriv.salescrm.document.dto.DocumentUploadResponse;
import com.ayshriv.salescrm.document.entity.Document;
import com.ayshriv.salescrm.document.entity.DocumentChunk;
import com.ayshriv.salescrm.document.repository.DocumentChunkRepository;
import com.ayshriv.salescrm.document.repository.DocumentRepository;
import com.ayshriv.salescrm.document.service.DocumentService;
import com.ayshriv.salescrm.organization.entity.Organization;
import com.ayshriv.salescrm.organization.repository.OrganizationRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CrossTenantRagIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DocumentService documentService;

    private Organization orgA;
    private Organization orgB;
    private User userA;
    private User userB;

    private Document docA;
    private Document docB;

    @BeforeEach
    void setUp() {
        documentChunkRepository.deleteAll();
        documentRepository.deleteAll();
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        // 1. Create Org A and Org B
        orgA = organizationRepository.save(new Organization("Alpha Corp", "alpha-corp"));
        orgB = organizationRepository.save(new Organization("Beta Industries", "beta-industries"));

        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Admin")));

        userA = new User();
        userA.setOrganization(orgA);
        userA.setUserType(adminType);
        userA.setEmail("admin@alphacorp.com");
        userA.setPassword("password");
        userA.setFirstName("Alice");
        userA.setLastName("Alpha");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setOrganization(orgB);
        userB.setUserType(adminType);
        userB.setEmail("admin@betaindustries.com");
        userB.setPassword("password");
        userB.setFirstName("Bob");
        userB.setLastName("Beta");
        userB = userRepository.save(userB);

        // 2. Upload Document into Org B (Confidential M&A Strategy)
        authenticateAs(userB, orgB);
        MockMultipartFile fileB = new MockMultipartFile(
                "file",
                "org_b_secret_strategy.txt",
                "text/plain",
                ("CONFIDENTIAL BETA INDUSTRIES M&A STRATEGY 2026\n" +
                 "Beta Industries is acquiring Competitor Omega for $25,000,000 in Q3.\n" +
                 "Special enterprise contract pricing is capped at 50% discount for strategic accounts.").getBytes(StandardCharsets.UTF_8)
        );
        DocumentUploadResponse uploadResponseB = documentService.uploadDocument(fileB, "Beta Confidential M&A Strategy");
        docB = documentRepository.findById(uploadResponseB.getDocumentId()).orElseThrow();

        // 3. Upload Document into Org A (Public Feature Guide)
        authenticateAs(userA, orgA);
        MockMultipartFile fileA = new MockMultipartFile(
                "file",
                "org_a_features.txt",
                "text/plain",
                ("Alpha Corp Standard Feature Guide\n" +
                 "Alpha Corp CRM provides lead capture, automated task assignments, and activity timelines.").getBytes(StandardCharsets.UTF_8)
        );
        DocumentUploadResponse uploadResponseA = documentService.uploadDocument(fileA, "Alpha Features Guide");
        docA = documentRepository.findById(uploadResponseA.getDocumentId()).orElseThrow();
    }

    private void authenticateAs(User user, Organization org) {
        UserPrincipal principal = new UserPrincipal(
                user.getId(),
                org.getId(),
                user.getEmail(),
                user.getPassword(),
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
    @DisplayName("Stage 6.5 - Cross-Tenant Isolation: Org A NEVER retrieves Org B chunks even with highly similar query")
    void testCrossTenantRetrievalIsolation() {
        authenticateAs(userA, orgA);

        // Query tailored specifically to match Org B's confidential content
        String targetedQuery = "What is the confidential M&A strategy, acquisition of Competitor Omega for $25,000,000 and 50% discount?";

        DocumentRetrievalResult result = documentService.retrieveSimilarChunks(targetedQuery, 10);

        assertThat(result).isNotNull();
        assertThat(result.getOrganizationId()).isEqualTo(orgA.getId());

        // CRITICAL CHECK: Org A must NEVER get any chunks belonging to Org B
        for (DocumentChunkDto match : result.getMatches()) {
            assertThat(match.getDocumentId()).isNotEqualTo(docB.getId());
            assertThat(match.getContent()).doesNotContain("Beta Industries");
            assertThat(match.getContent()).doesNotContain("Competitor Omega");
            assertThat(match.getContent()).doesNotContain("$25,000,000");
        }

        // Verify that all chunks found in the database for Org B have organization_id = orgB.id
        List<DocumentChunk> allOrgBChunks = documentChunkRepository.findByOrganizationIdAndIsDeletedFalse(orgB.getId());
        assertThat(allOrgBChunks).isNotEmpty();
        for (DocumentChunk bChunk : allOrgBChunks) {
            assertThat(bChunk.getOrganization().getId()).isEqualTo(orgB.getId());
        }
    }

    @Test
    @DisplayName("Stage 6.5 - Org B successfully retrieves its own chunks for targeted query")
    void testOrgBRetrievesItsOwnChunks() {
        authenticateAs(userB, orgB);

        String targetedQuery = "What is the confidential M&A strategy and acquisition of Competitor Omega?";
        DocumentRetrievalResult result = documentService.retrieveSimilarChunks(targetedQuery, 5);

        assertThat(result).isNotNull();
        assertThat(result.getOrganizationId()).isEqualTo(orgB.getId());
        assertThat(result.getMatches()).isNotEmpty();
        assertThat(result.getMatches().get(0).getContent()).contains("Beta Industries");
        assertThat(result.getMatches().get(0).getDocumentTitle()).isEqualTo("Beta Confidential M&A Strategy");
    }

    @Test
    @DisplayName("Stage 6.5 - Org A cannot view, list, or delete Org B's documents via REST API")
    void testOrgACannotAccessOrgBDocumentsViaRest() throws Exception {
        authenticateAs(userA, orgA);

        // 1. Org A cannot fetch Org B document by ID
        mockMvc.perform(get("/documents/" + docB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE))
                .andExpect(jsonPath("$.document").doesNotExist());

        // 2. Org A cannot fetch Org B document chunks by ID
        mockMvc.perform(get("/documents/" + docB.getId() + "/chunks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));

        // 3. Org A listing documents only returns Org A's document
        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.SUCCESS))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.documents[0].filename").value("org_a_features.txt"));

        // 4. Org A cannot delete Org B's document
        mockMvc.perform(delete("/documents/" + docB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value(Constants.FAILURE));

        Document stillIntactDocB = documentRepository.findById(docB.getId()).orElseThrow();
        assertThat(stillIntactDocB.getIsDeleted()).isFalse();
    }
}
