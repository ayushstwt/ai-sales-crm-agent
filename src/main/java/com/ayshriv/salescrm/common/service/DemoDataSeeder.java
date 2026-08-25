package com.ayshriv.salescrm.common.service;

import com.ayshriv.salescrm.activity.entity.Activity;
import com.ayshriv.salescrm.activity.entity.ActivityType;
import com.ayshriv.salescrm.activity.repository.ActivityRepository;
import com.ayshriv.salescrm.company.entity.Company;
import com.ayshriv.salescrm.company.repository.CompanyRepository;
import com.ayshriv.salescrm.contact.entity.Contact;
import com.ayshriv.salescrm.contact.repository.ContactRepository;
import com.ayshriv.salescrm.deal.entity.Deal;
import com.ayshriv.salescrm.deal.entity.DealStatus;
import com.ayshriv.salescrm.deal.repository.DealRepository;
import com.ayshriv.salescrm.document.entity.Document;
import com.ayshriv.salescrm.document.entity.DocumentChunk;
import com.ayshriv.salescrm.document.repository.DocumentChunkRepository;
import com.ayshriv.salescrm.document.repository.DocumentRepository;
import com.ayshriv.salescrm.document.service.DocumentEmbeddingService;
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
import com.ayshriv.salescrm.user.entity.Role;
import com.ayshriv.salescrm.user.entity.User;
import com.ayshriv.salescrm.user.entity.UserType;
import com.ayshriv.salescrm.user.repository.RoleRepository;
import com.ayshriv.salescrm.user.repository.UserRepository;
import com.ayshriv.salescrm.user.repository.UserTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class DemoDataSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LeadRepository leadRepository;
    private final CompanyRepository companyRepository;
    private final ContactRepository contactRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final DealRepository dealRepository;
    private final ActivityRepository activityRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentEmbeddingService embeddingService;

    public DemoDataSeeder(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            UserTypeRepository userTypeRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            LeadRepository leadRepository,
            CompanyRepository companyRepository,
            ContactRepository contactRepository,
            PipelineRepository pipelineRepository,
            PipelineStageRepository pipelineStageRepository,
            DealRepository dealRepository,
            ActivityRepository activityRepository,
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            DocumentEmbeddingService embeddingService
    ) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.leadRepository = leadRepository;
        this.companyRepository = companyRepository;
        this.contactRepository = contactRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.dealRepository = dealRepository;
        this.activityRepository = activityRepository;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public Organization seedDemoData() {
        LOGGER.info("DemoDataSeeder >> Starting full demo data seeding...");

        // 1. Create Organization
        Organization org = organizationRepository.findBySlugAndIsDeletedFalse("acme-innovations")
                .orElseGet(() -> organizationRepository.save(new Organization("Acme Innovations", "acme-innovations")));

        // 2. UserTypes and Roles
        UserType adminType = userTypeRepository.findByName(EUserType.ORG_ADMIN)
                .orElseGet(() -> userTypeRepository.save(new UserType(EUserType.ORG_ADMIN, "Org Admin")));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ORG_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ORG_ADMIN, "Role Org Admin")));

        // 3. Create Admin User (Rahul)
        User rahul = userRepository.findByEmail("rahul@acme.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setOrganization(org);
                    u.setUserType(adminType);
                    u.setEmail("rahul@acme.com");
                    u.setPassword(passwordEncoder.encode("password123"));
                    u.setFirstName("Rahul");
                    u.setLastName("Sharma");
                    u.setPhone("+1-555-0199");
                    u.setRoles(new HashSet<>(Collections.singletonList(adminRole)));
                    return userRepository.save(u);
                });

        // 4. Seed Companies
        Company cloudScale = seedCompany(org, "CloudScale Systems", "cloudscalesystems.com", "Enterprise Cloud & DevOps");
        Company titan = seedCompany(org, "Titan Dynamics", "titandynamics.com", "Robotics & Hardware");
        Company nextGen = seedCompany(org, "NextGen BioMed", "nextgenbiomed.org", "Biotechnology");
        Company cyberShield = seedCompany(org, "CyberShield Security", "cybershield.net", "Cybersecurity");
        Company beacon = seedCompany(org, "Beacon Health Systems", "beaconhealth.org", "Healthcare & Life Sciences");
        Company vortex = seedCompany(org, "Vortex AI", "vortexai.tech", "Artificial Intelligence");

        // 5. Seed Contacts
        Contact amara = seedContact(org, cloudScale, "Amara", "Okafor", "amara@cloudscalesystems.com", "+1-555-1005", "VP Enterprise Engineering");
        Contact vikram = seedContact(org, titan, "Vikram", "Malhotra", "v.malhotra@titandynamics.com", "+1-555-1008", "Senior VP Sales");
        Contact david = seedContact(org, nextGen, "David", "Kim", "dkim@nextgenbiomed.org", "+1-555-1004", "Chief Commercial Officer");
        Contact elena = seedContact(org, cyberShield, "Elena", "Rostova", "elena@cybershield.net", "+1-555-1003", "Director of Security");
        Contact priya = seedContact(org, beacon, "Priya", "Patel", "priya.p@beaconhealth.org", "+1-555-1013", "VP Clinical Operations");
        Contact lucas = seedContact(org, vortex, "Lucas", "Silva", "lucas@vortexai.tech", "+1-555-1006", "Chief Technology Officer");

        // 6. Seed Pipeline and Stages
        Pipeline pipeline = pipelineRepository.findByOrganizationIdAndIsDefaultTrueAndIsDeletedFalse(org.getId())
                .orElseGet(() -> {
                    Pipeline p = new Pipeline();
                    p.setOrganization(org);
                    p.setName("Standard Enterprise Pipeline");
                    p.setIsDefault(true);
                    return pipelineRepository.save(p);
                });

        PipelineStage stageProspecting = seedStage(pipeline, "Prospecting", 1, 10.0);
        PipelineStage stageQual = seedStage(pipeline, "Qualification", 2, 30.0);
        PipelineStage stageProposal = seedStage(pipeline, "Proposal / Demo", 3, 60.0);
        PipelineStage stageNeg = seedStage(pipeline, "Negotiation", 4, 80.0);
        PipelineStage stageWon = seedStage(pipeline, "Closed Won", 5, 100.0);
        PipelineStage stageLost = seedStage(pipeline, "Closed Lost", 6, 0.0);

        // 7. Seed 20 Leads
        seedLead(org, rahul, "Sophia", "Chen", "sophia@fintechlabs.io", "+1-555-1001", "FinTech Labs", "Head of Revenue Operations", LeadStatus.QUALIFIED, "Interested in automated deal tracking.");
        seedLead(org, rahul, "Marcus", "Aurelius", "marcus@empirelogistics.com", "+1-555-1002", "Empire Logistics", "VP Supply Chain", LeadStatus.NEW, "Downloaded product whitepaper.");
        seedLead(org, rahul, "Elena", "Rostova", "elena@cybershield.net", "+1-555-1003", "CyberShield Security", "Director of Security", LeadStatus.CONTACTED, "Inquired about SOC2 compliance automation.");
        seedLead(org, rahul, "David", "Kim", "dkim@nextgenbiomed.org", "+1-555-1004", "NextGen BioMed", "Chief Commercial Officer", LeadStatus.QUALIFIED, "Needs automated customer 360 view.");
        seedLead(org, rahul, "Amara", "Okafor", "amara@cloudscalesystems.com", "+1-555-1005", "CloudScale Systems", "VP Enterprise Engineering", LeadStatus.CONVERTED, "High priority account converted to enterprise deal.");
        seedLead(org, rahul, "Lucas", "Silva", "lucas@vortexai.tech", "+1-555-1006", "Vortex AI", "CTO", LeadStatus.QUALIFIED, "Met at AI Summit 2026.");
        seedLead(org, rahul, "Hannah", "Abbott", "hannah@apexmedia.co", "+1-555-1007", "Apex Media", "Marketing Operations Lead", LeadStatus.NEW, "Form submission for 15 sales seats.");
        seedLead(org, rahul, "Vikram", "Malhotra", "v.malhotra@titandynamics.com", "+1-555-1008", "Titan Dynamics", "Senior VP Sales", LeadStatus.CONTACTED, "Interested in AI tool calling agent capabilities.");
        seedLead(org, rahul, "Chloe", "Bennett", "chloe@quantumretail.io", "+1-555-1009", "Quantum Retail", "Director of Omnichannel", LeadStatus.QUALIFIED, "Evaluating CRM migration from Legacy CRM.");
        seedLead(org, rahul, "Tariq", "Mansour", "tariq@solarisenergy.com", "+1-555-1010", "Solaris Energy", "Procurement Manager", LeadStatus.LOST, "Budget freeze for Q1/Q2.");
        seedLead(org, rahul, "Zoe", "Saldana", "zoe@horizonrobotics.ai", "+1-555-1011", "Horizon Robotics", "Operations Lead", LeadStatus.NEW, "Referred by investor.");
        seedLead(org, rahul, "Oliver", "Twist", "oliver@evergreensupply.com", "+1-555-1012", "Evergreen Supply", "Managing Director", LeadStatus.CONTACTED, "Requested custom pricing matrix.");
        seedLead(org, rahul, "Priya", "Patel", "priya.p@beaconhealth.org", "+1-555-1013", "Beacon Health Systems", "VP Clinical Operations", LeadStatus.QUALIFIED, "Urgent need for tenant-isolated HIPAA compliant records.");
        seedLead(org, rahul, "Gabriel", "Mendoza", "gmendoza@sierrasaas.com", "+1-555-1014", "Sierra SaaS", "Sales Enablement Manager", LeadStatus.NEW, "Demo request scheduled.");
        seedLead(org, rahul, "Freja", "Lindqvist", "freja@nordicanalytics.se", "+1-555-1015", "Nordic Analytics", "Head of Business Intelligence", LeadStatus.QUALIFIED, "Fast growing 50 person analytics team.");
        seedLead(org, rahul, "Liam", "OConnor", "liam@celtictelecom.ie", "+1-555-1016", "Celtic Telecom", "Telecom Strategy Director", LeadStatus.CONTACTED, "Evaluating multi-tenant platforms.");
        seedLead(org, rahul, "Mei-Ling", "Zhou", "zhou@pacificpayments.sg", "+1-555-1017", "Pacific Payments", "VP Global Partnerships", LeadStatus.QUALIFIED, "APAC expansion project.");
        seedLead(org, rahul, "Alexander", "Wright", "alex@pinnaclecapital.com", "+1-555-1018", "Pinnacle Capital", "Managing Partner", LeadStatus.NEW, "Looking for private wealth CRM workflows.");
        seedLead(org, rahul, "Fatima", "Al-Sayed", "fatima@oasislogistics.ae", "+1-555-1019", "Oasis Logistics", "COO", LeadStatus.LOST, "Competitor selected due to legacy on-prem requirement.");
        seedLead(org, rahul, "Noah", "Vance", "noah@summitsoftware.io", "+1-555-1020", "Summit Software", "Founder & CEO", LeadStatus.QUALIFIED, "Early-stage YC startup looking to scale sales.");

        // 8. Seed Deals
        Deal d1 = seedDeal(org, cloudScale, amara, stageNeg, "CloudScale Systems - Enterprise Platform Expansion", new BigDecimal("150000.00"), LocalDateTime.now().plusDays(14), LocalDateTime.now().minusDays(1));
        Deal d2 = seedDeal(org, nextGen, david, stageQual, "NextGen BioMed - Compliance Suite License", new BigDecimal("120000.00"), LocalDateTime.now().plusDays(45), LocalDateTime.now().minusDays(14));
        Deal d3 = seedDeal(org, titan, vikram, stageProposal, "Titan Dynamics - Autonomous AI Fleet Deployment", new BigDecimal("110000.00"), LocalDateTime.now().plusDays(30), LocalDateTime.now().minusDays(10));
        Deal d4 = seedDeal(org, cyberShield, elena, stageProposal, "CyberShield Security - Annual SOC2 Workflow License", new BigDecimal("95000.00"), LocalDateTime.now().plusDays(21), LocalDateTime.now().minusDays(8));
        Deal d5 = seedDeal(org, beacon, priya, stageProspecting, "Beacon Health - Clinical CRM Integration", new BigDecimal("90000.00"), LocalDateTime.now().plusDays(60), LocalDateTime.now().minusDays(9));
        Deal d6 = seedDeal(org, null, null, stageProposal, "FinTech Labs - API Aggregation Gateway", new BigDecimal("80000.00"), LocalDateTime.now().plusDays(28), LocalDateTime.now().minusDays(12));
        Deal d7 = seedDeal(org, vortex, lucas, stageQual, "Vortex AI - LLM Orchestration Platform", new BigDecimal("65000.00"), LocalDateTime.now().plusDays(35), LocalDateTime.now().minusDays(2));

        // 9. Seed Activities
        seedActivity(org, rahul, cloudScale, amara, d1, ActivityType.MEETING, "Executive Alignment & Security Review", "Met with Amara Okafor (VP Engineering). Approved enterprise pricing with 15% upfront annual discount. Security architecture reviewed and verified against SOC2 checklist.", LocalDateTime.now().minusDays(1));
        seedActivity(org, rahul, cloudScale, amara, d1, ActivityType.NOTE, "Contract Negotiation Status", "CloudScale legal team requested standard Net-30 payment terms and 99.9% SLA agreement. All technical objections resolved. Ready for signature.", LocalDateTime.now().minusDays(1));
        seedActivity(org, rahul, titan, vikram, d3, ActivityType.CALL, "Initial Discovery Call with Titan Dynamics", "Vikram expressed interest in multi-tenant tool calling capabilities. Sent slide deck.", LocalDateTime.now().minusDays(10));
        seedActivity(org, rahul, nextGen, david, d2, ActivityType.CALL, "Initial Scope with NextGen BioMed", "David Kim discussed clinical CRM data isolation. No follow up since then.", LocalDateTime.now().minusDays(14));
        seedActivity(org, rahul, cyberShield, elena, d4, ActivityType.EMAIL, "Sent proposal to CyberShield Security", "Sent SOC2 workflow proposal. Awaiting confirmation.", LocalDateTime.now().minusDays(8));
        seedActivity(org, rahul, beacon, priya, d5, ActivityType.NOTE, "Beacon Health Intake", "Inbound lead from conference. Needs follow up call scheduled.", LocalDateTime.now().minusDays(9));

        // 10. Seed Documents & Chunks
        seedDocument(org, rahul, "Enterprise_Services_Agreement_and_Pricing_Guide_2026.txt", "Enterprise Services Agreement & Pricing Guide 2026",
                """
                SalesPilot CRM Master Enterprise Agreement 2026:
                1. COMMERCIAL TERMS: Standard annual contract pricing is $150,000 for unlimited seats. Custom enterprise tiers are eligible for up to a 20% discount when paid annually upfront.
                2. PAYMENT TERMS: Standard invoicing terms are Net-30 upon execution.
                3. SERVICE LEVEL AGREEMENT (SLA): 99.9% uptime SLA with 1-hour critical response window and 24/7 dedicated Technical Account Manager (TAM).
                4. TERMINATION & RENEWAL: Contracts renew automatically unless 30-day written notice is provided prior to the renewal date.
                """);

        seedDocument(org, rahul, "Product_Architecture_and_Onboarding_Playbook.txt", "Product Architecture & Onboarding Playbook",
                """
                SalesPilot CRM Onboarding & Implementation Playbook:
                1. ONBOARDING TIMELINE: Full production deployment takes 2 to 4 weeks, including multi-tenant database provisioning, SSO/SAML integration, and sales team training.
                2. ARCHITECTURE & SECURITY: Multi-tenant data isolation is code-enforced at the database and application level. RAG retrieval strictly isolates knowledge base chunks by organization_id before vector similarity search.
                3. AUDIT TRAILS: All user actions, API mutations, and AI tool calls are immutably logged in the audit_logs table with source identification (MANUAL, AI_AGENT, API, SYSTEM).
                4. NEXT STEPS FOR NEW CUSTOMERS:
                   Step 1: Sign the Enterprise Services Agreement and Master Order Form.
                   Step 2: Schedule a 60-minute technical onboarding kickoff call with our solutions team.
                   Step 3: Receive staging environment credentials and begin CRM data migration.
                """);

        LOGGER.info("DemoDataSeeder >> Demo data seeding completed successfully for organization: {}", org.getName());
        return org;
    }

    private Company seedCompany(Organization org, String name, String domain, String industry) {
        return companyRepository.findAll().stream()
                .filter(c -> c.getName().equals(name) && c.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(c.getIsDeleted()))
                .findFirst()
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setOrganization(org);
                    c.setName(name);
                    c.setDomain(domain);
                    c.setIndustry(industry);
                    return companyRepository.save(c);
                });
    }

    private Contact seedContact(Organization org, Company company, String first, String last, String email, String phone, String title) {
        return contactRepository.findAll().stream()
                .filter(c -> c.getEmail().equals(email) && c.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(c.getIsDeleted()))
                .findFirst()
                .orElseGet(() -> {
                    Contact c = new Contact();
                    c.setOrganization(org);
                    c.setCompany(company);
                    c.setFirstName(first);
                    c.setLastName(last);
                    c.setEmail(email);
                    c.setPhone(phone);
                    c.setJobTitle(title);
                    return contactRepository.save(c);
                });
    }

    private PipelineStage seedStage(Pipeline pipeline, String name, int order, double probability) {
        return pipelineStageRepository.findByPipelineIdAndIsDeletedFalseOrderByOrderIndexAsc(pipeline.getId()).stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    PipelineStage s = new PipelineStage(pipeline, name, order, probability);
                    return pipelineStageRepository.save(s);
                });
    }

    private Lead seedLead(Organization org, User rep, String first, String last, String email, String phone, String company, String title, LeadStatus status, String notes) {
        return leadRepository.findAll().stream()
                .filter(l -> l.getEmail().equals(email) && l.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(l.getIsDeleted()))
                .findFirst()
                .orElseGet(() -> {
                    Lead l = new Lead();
                    l.setOrganization(org);
                    l.setAssignedTo(rep);
                    l.setFirstName(first);
                    l.setLastName(last);
                    l.setEmail(email);
                    l.setPhone(phone);
                    l.setCompanyName(company);
                    l.setJobTitle(title);
                    l.setStatus(status);
                    l.setNotes(notes);
                    return leadRepository.save(l);
                });
    }

    private Deal seedDeal(Organization org, Company company, Contact contact, PipelineStage stage, String title, BigDecimal amount, LocalDateTime closeDate, LocalDateTime updatedOn) {
        return dealRepository.findAll().stream()
                .filter(d -> d.getTitle().equals(title) && d.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(d.getIsDeleted()))
                .findFirst()
                .orElseGet(() -> {
                    Deal d = new Deal();
                    d.setOrganization(org);
                    d.setCompany(company);
                    d.setContact(contact);
                    d.setPipelineStage(stage);
                    d.setTitle(title);
                    d.setAmount(amount);
                    d.setStatus(DealStatus.OPEN);
                    d.setExpectedCloseDate(closeDate);
                    d.setUpdatedOn(updatedOn);
                    return dealRepository.save(d);
                });
    }

    private void seedActivity(Organization org, User user, Company company, Contact contact, Deal deal, ActivityType type, String title, String description, LocalDateTime date) {
        Activity a = new Activity();
        a.setOrganization(org);
        a.setUser(user);
        a.setCompany(company);
        a.setContact(contact);
        a.setDeal(deal);
        a.setType(type);
        a.setTitle(title);
        a.setDescription(description);
        a.setActivityDate(date);
        activityRepository.save(a);
    }

    private void seedDocument(Organization org, User user, String filename, String title, String content) {
        Document doc = documentRepository.findAll().stream()
                .filter(d -> d.getFilename().equals(filename) && d.getOrganization().getId().equals(org.getId()) && !Boolean.TRUE.equals(d.getIsDeleted()))
                .findFirst()
                .orElseGet(() -> {
                    Document d = new Document(org, user, filename, "TXT", (long) content.length(), title);
                    return documentRepository.save(d);
                });

        if (documentChunkRepository.findByDocumentIdAndIsDeletedFalseOrderByChunkIndexAsc(doc.getId()).isEmpty()) {
            DocumentChunk chunk = new DocumentChunk(org, doc, 0, content.trim(), 150);
            List<Double> vector = embeddingService.generateEmbedding(content.trim());
            chunk.setEmbeddingVector(vector);
            documentChunkRepository.save(chunk);
        }
    }
}
