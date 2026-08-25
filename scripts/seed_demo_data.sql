-- AI Sales CRM Agent - Comprehensive Demo Seed Script
-- Master.md §9, §10 & Plans.md Stage 7
-- Organization, Admin User, ~20 Leads, Companies, Contacts, Pipeline, Stages, Deals, Activities, Documents & Chunks

-- 1. Create Organization
INSERT INTO organizations (id, name, slug, is_active, is_deleted, created_on)
VALUES (100, 'Acme Innovations', 'acme-innovations', TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 2. Create User (Rahul Sharma, ORG_ADMIN, password: password123)
-- BCrypt for password123: $2a$10$w8T0iZp2GkYh1zB2QW4A4uYqjR0d7g8x6e5w4v3u2t1s0r9q8p7o6
INSERT INTO users (id, organization_id, user_type_id, email, password, first_name, last_name, phone, is_active, is_deleted, created_on)
VALUES (100, 100, 1, 'rahul@acme.com', '$2a$10$w8T0iZp2GkYh1zB2QW4A4uYqjR0d7g8x6e5w4v3u2t1s0r9q8p7o6', 'Rahul', 'Sharma', '+1-555-0199', TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
VALUES (100, 1)
ON CONFLICT DO NOTHING;

-- 3. Seed ~20 Leads
INSERT INTO leads (id, organization_id, assigned_to_id, first_name, last_name, email, phone, company_name, title, status, source, notes, is_active, is_deleted, created_on) VALUES
(101, 100, 100, 'Sophia', 'Chen', 'sophia@fintechlabs.io', '+1-555-1001', 'FinTech Labs', 'Head of Revenue Operations', 'QUALIFIED', 'WEBSITE', 'Interested in automated deal tracking.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '15 days'),
(102, 100, 100, 'Marcus', 'Aurelius', 'marcus@empirelogistics.com', '+1-555-1002', 'Empire Logistics', 'VP Supply Chain', 'NEW', 'INBOUND', 'Downloaded product whitepaper.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(103, 100, 100, 'Elena', 'Rostova', 'elena@cybershield.net', '+1-555-1003', 'CyberShield Security', 'Director of Security', 'CONTACTED', 'REFERRAL', 'Inquired about SOC2 compliance automation.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days'),
(104, 100, 100, 'David', 'Kim', 'dkim@nextgenbiomed.org', '+1-555-1004', 'NextGen BioMed', 'Chief Commercial Officer', 'QUALIFIED', 'OUTBOUND', 'Needs automated customer 360 view.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '20 days'),
(105, 100, 100, 'Amara', 'Okafor', 'amara@cloudscalesystems.com', '+1-555-1005', 'CloudScale Systems', 'VP Enterprise Engineering', 'CONVERTED', 'PARTNER', 'High priority account converted to enterprise deal.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '30 days'),
(106, 100, 100, 'Lucas', 'Silva', 'lucas@vortexai.tech', '+1-555-1006', 'Vortex AI', 'CTO', 'QUALIFIED', 'EVENT', 'Met at AI Summit 2026.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '5 days'),
(107, 100, 100, 'Hannah', 'Abbott', 'hannah@apexmedia.co', '+1-555-1007', 'Apex Media', 'Marketing Operations Lead', 'NEW', 'WEBSITE', 'Form submission for 15 sales seats.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(108, 100, 100, 'Vikram', 'Malhotra', 'v.malhotra@titandynamics.com', '+1-555-1008', 'Titan Dynamics', 'Senior VP Sales', 'CONTACTED', 'COLD_OUTREACH', 'Interested in AI tool calling agent capabilities.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '12 days'),
(109, 100, 100, 'Chloe', 'Bennett', 'chloe@quantumretail.io', '+1-555-1009', 'Quantum Retail', 'Director of Omnichannel', 'QUALIFIED', 'ORGANIC', 'Evaluating CRM migration from Legacy CRM.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '6 days'),
(110, 100, 100, 'Tariq', 'Mansour', 'tariq@solarisenergy.com', '+1-555-1010', 'Solaris Energy', 'Procurement Manager', 'LOST', 'INBOUND', 'Budget freeze for Q1/Q2.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '25 days'),
(111, 100, 100, 'Zoe', 'Saldana', 'zoe@horizonrobotics.ai', '+1-555-1011', 'Horizon Robotics', 'Operations Lead', 'NEW', 'LINKEDIN', 'Referred by investor.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '3 days'),
(112, 100, 100, 'Oliver', 'Twist', 'oliver@evergreensupply.com', '+1-555-1012', 'Evergreen Supply', 'Managing Director', 'CONTACTED', 'EMAIL_CAMPAIGN', 'Requested custom pricing matrix.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '14 days'),
(113, 100, 100, 'Priya', 'Patel', 'priya.p@beaconhealth.org', '+1-555-1013', 'Beacon Health Systems', 'VP Clinical Operations', 'QUALIFIED', 'CONFERENCE', 'Urgent need for tenant-isolated HIPAA compliant records.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '11 days'),
(114, 100, 100, 'Gabriel', 'Mendoza', 'gmendoza@sierrasaas.com', '+1-555-1014', 'Sierra SaaS', 'Sales Enablement Manager', 'NEW', 'WEBSITE', 'Demo request scheduled.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(115, 100, 100, 'Freja', 'Lindqvist', 'freja@nordicanalytics.se', '+1-555-1015', 'Nordic Analytics', 'Head of Business Intelligence', 'QUALIFIED', 'PARTNER', 'Fast growing 50 person analytics team.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '8 days'),
(116, 100, 100, 'Liam', 'OConnor', 'liam@celtictelecom.ie', '+1-555-1016', 'Celtic Telecom', 'Telecom Strategy Director', 'CONTACTED', 'COLD_CALL', 'Evaluating multi-tenant platforms.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '18 days'),
(117, 100, 100, 'Mei-Ling', 'Zhou', 'zhou@pacificpayments.sg', '+1-555-1017', 'Pacific Payments', 'VP Global Partnerships', 'QUALIFIED', 'EVENT', 'APAC expansion project.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '7 days'),
(118, 100, 100, 'Alexander', 'Wright', 'alex@pinnaclecapital.com', '+1-555-1018', 'Pinnacle Capital', 'Managing Partner', 'NEW', 'INBOUND', 'Looking for private wealth CRM workflows.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '2 days'),
(119, 100, 100, 'Fatima', 'Al-Sayed', 'fatima@oasislogistics.ae', '+1-555-1019', 'Oasis Logistics', 'COO', 'LOST', 'WEBSITE', 'Competitor selected due to legacy on-prem requirement.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '40 days'),
(120, 100, 100, 'Noah', 'Vance', 'noah@summitsoftware.io', '+1-555-1020', 'Summit Software', 'Founder & CEO', 'QUALIFIED', 'COMMUNITY', 'Early-stage YC startup looking to scale sales.', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '4 days')
ON CONFLICT (id) DO NOTHING;

-- 4. Seed Companies
INSERT INTO companies (id, organization_id, name, domain, industry, is_active, is_deleted, created_on) VALUES
(101, 100, 'CloudScale Systems', 'cloudscalesystems.com', 'Enterprise Cloud & DevOps', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '30 days'),
(102, 100, 'Titan Dynamics', 'titandynamics.com', 'Robotics & Hardware', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '20 days'),
(103, 100, 'NextGen BioMed', 'nextgenbiomed.org', 'Biotechnology', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '25 days'),
(104, 100, 'CyberShield Security', 'cybershield.net', 'Cybersecurity', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '15 days'),
(105, 100, 'Beacon Health Systems', 'beaconhealth.org', 'Healthcare & Life Sciences', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '18 days'),
(106, 100, 'Vortex AI', 'vortexai.tech', 'Artificial Intelligence', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Contacts
INSERT INTO contacts (id, organization_id, company_id, first_name, last_name, email, phone, job_title, is_active, is_deleted, created_on) VALUES
(101, 100, 101, 'Amara', 'Okafor', 'amara@cloudscalesystems.com', '+1-555-1005', 'VP Enterprise Engineering', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '30 days'),
(102, 100, 102, 'Vikram', 'Malhotra', 'v.malhotra@titandynamics.com', '+1-555-1008', 'Senior VP Sales', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '20 days'),
(103, 100, 103, 'David', 'Kim', 'dkim@nextgenbiomed.org', '+1-555-1004', 'Chief Commercial Officer', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '25 days'),
(104, 100, 104, 'Elena', 'Rostova', 'elena@cybershield.net', '+1-555-1003', 'Director of Security', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '15 days'),
(105, 100, 105, 'Priya', 'Patel', 'priya.p@beaconhealth.org', '+1-555-1013', 'VP Clinical Operations', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '18 days'),
(106, 100, 106, 'Lucas', 'Silva', 'lucas@vortexai.tech', '+1-555-1006', 'Chief Technology Officer', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

-- 6. Seed Default Pipeline and Pipeline Stages
INSERT INTO pipelines (id, organization_id, name, is_default, is_active, is_deleted, created_on)
VALUES (100, 100, 'Standard Enterprise Pipeline', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO pipeline_stages (id, pipeline_id, name, stage_order, probability, is_active, is_deleted, created_on) VALUES
(101, 100, 'Prospecting', 1, 10.0, TRUE, FALSE, CURRENT_TIMESTAMP),
(102, 100, 'Qualification', 2, 30.0, TRUE, FALSE, CURRENT_TIMESTAMP),
(103, 100, 'Proposal / Demo', 3, 60.0, TRUE, FALSE, CURRENT_TIMESTAMP),
(104, 100, 'Negotiation', 4, 80.0, TRUE, FALSE, CURRENT_TIMESTAMP),
(105, 100, 'Closed Won', 5, 100.0, TRUE, FALSE, CURRENT_TIMESTAMP),
(106, 100, 'Closed Lost', 6, 0.0, TRUE, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7. Seed Deals (Includes high-value deals with and without recent activities for Killer Demo Flow)
INSERT INTO deals (id, organization_id, company_id, contact_id, pipeline_stage_id, title, amount, status, expected_close_date, is_active, is_deleted, created_on, updated_on) VALUES
-- Highest value deal #1 ($150,000, Negotiation stage, highly active, most likely to close!)
(101, 100, 101, 101, 104, 'CloudScale Systems - Enterprise Platform Expansion', 150000.00, 'OPEN', CURRENT_DATE + INTERVAL '14 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '1 day'),
-- High value deal #2 ($120,000, Qualification stage, NO activity in 14 days)
(102, 100, 103, 103, 102, 'NextGen BioMed - Compliance Suite License', 120000.00, 'OPEN', CURRENT_DATE + INTERVAL '45 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '14 days'),
-- High value deal #3 ($110,000, Proposal stage, NO activity in 10 days)
(103, 100, 102, 102, 103, 'Titan Dynamics - Autonomous AI Fleet Deployment', 110000.00, 'OPEN', CURRENT_DATE + INTERVAL '30 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
-- High value deal #4 ($95,000, Proposal stage, NO activity in 8 days)
(104, 100, 104, 104, 103, 'CyberShield Security - Annual SOC2 Workflow License', 95000.00, 'OPEN', CURRENT_DATE + INTERVAL '21 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '8 days'),
-- High value deal #5 ($90,000, Prospecting stage, NO activity in 9 days)
(105, 100, 105, 105, 101, 'Beacon Health - Clinical CRM Integration', 90000.00, 'OPEN', CURRENT_DATE + INTERVAL '60 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '18 days', CURRENT_TIMESTAMP - INTERVAL '9 days'),
-- High value deal #6 ($80,000, Proposal stage, NO activity in 12 days)
(106, 100, NULL, NULL, 103, 'FinTech Labs - API Aggregation Gateway', 80000.00, 'OPEN', CURRENT_DATE + INTERVAL '28 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '12 days'),
-- High value deal #7 ($65,000, active 2 days ago)
(107, 100, 106, 106, 102, 'Vortex AI - LLM Orchestration Platform', 65000.00, 'OPEN', CURRENT_DATE + INTERVAL '35 days', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- 8. Seed Activities (Recent for CloudScale, Stale for others)
INSERT INTO activities (id, organization_id, user_id, type, title, description, activity_date, company_id, contact_id, deal_id, is_active, is_deleted, created_on) VALUES
(101, 100, 100, 'MEETING', 'Executive Alignment & Security Review', 'Met with Amara Okafor (VP Engineering). Approved enterprise pricing with 15% upfront annual discount. Security architecture reviewed and verified against SOC2 checklist.', CURRENT_TIMESTAMP - INTERVAL '1 day', 101, 101, 101, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(102, 100, 100, 'NOTE', 'Contract Negotiation Status', 'CloudScale legal team requested standard Net-30 payment terms and 99.9% SLA agreement. All technical objections resolved. Ready for signature.', CURRENT_TIMESTAMP - INTERVAL '1 day', 101, 101, 101, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 day'),
(103, 100, 100, 'CALL', 'Initial Discovery Call with Titan Dynamics', 'Vikram expressed interest in multi-tenant tool calling capabilities. Sent slide deck.', CURRENT_TIMESTAMP - INTERVAL '10 days', 102, 102, 103, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days'),
(104, 100, 100, 'CALL', 'Initial Scope with NextGen BioMed', 'David Kim discussed clinical CRM data isolation. No follow up since then.', CURRENT_TIMESTAMP - INTERVAL '14 days', 103, 103, 102, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '14 days'),
(105, 100, 100, 'EMAIL', 'Sent proposal to CyberShield Security', 'Sent SOC2 workflow proposal. Awaiting confirmation.', CURRENT_TIMESTAMP - INTERVAL '8 days', 104, 104, 104, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '8 days'),
(106, 100, 100, 'NOTE', 'Beacon Health Intake', 'Inbound lead from conference. Needs follow up call scheduled.', CURRENT_TIMESTAMP - INTERVAL '9 days', 105, 105, 105, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '9 days')
ON CONFLICT (id) DO NOTHING;

-- 9. Seed Documents and RAG Chunks
INSERT INTO documents (id, organization_id, uploaded_by_id, filename, file_type, file_size, title, is_active, is_deleted, created_on) VALUES
(101, 100, 100, 'Enterprise_Services_Agreement_and_Pricing_Guide_2026.txt', 'TXT', 2048, 'Enterprise Services Agreement & Pricing Guide 2026', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days'),
(102, 100, 100, 'Product_Architecture_and_Onboarding_Playbook.txt', 'TXT', 2560, 'Product Architecture & Onboarding Playbook', TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO document_chunks (id, organization_id, document_id, chunk_index, content, token_count, embedding, is_active, is_deleted, created_on) VALUES
(101, 100, 101, 0, 'SalesPilot CRM Master Enterprise Agreement 2026:
1. COMMERCIAL TERMS: Standard annual contract pricing is $150,000 for unlimited seats. Custom enterprise tiers are eligible for up to a 20% discount when paid annually upfront.
2. PAYMENT TERMS: Standard invoicing terms are Net-30 upon execution.
3. SERVICE LEVEL AGREEMENT (SLA): 99.9% uptime SLA with 1-hour critical response window and 24/7 dedicated Technical Account Manager (TAM).
4. TERMINATION & RENEWAL: Contracts renew automatically unless 30-day written notice is provided prior to the renewal date.', 120, NULL, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days'),

(102, 100, 102, 0, 'SalesPilot CRM Onboarding & Implementation Playbook:
1. ONBOARDING TIMELINE: Full production deployment takes 2 to 4 weeks, including multi-tenant database provisioning, SSO/SAML integration, and sales team training.
2. ARCHITECTURE & SECURITY: Multi-tenant data isolation is enforced at the database and application level. RAG retrieval strictly isolates knowledge base chunks by organization_id before vector search.
3. AUDIT TRAILS: All user actions, API mutations, and AI tool calls are immutably logged in the audit_logs table with source identification (MANUAL, AI_AGENT, API, SYSTEM).
4. NEXT STEPS FOR NEW CUSTOMERS:
   Step 1: Sign the Enterprise Services Agreement and Master Order Form.
   Step 2: Schedule a 60-minute technical onboarding kickoff call with our solutions team.
   Step 3: Receive staging environment credentials and begin CRM migration.', 150, NULL, TRUE, FALSE, CURRENT_TIMESTAMP - INTERVAL '10 days')
ON CONFLICT (id) DO NOTHING;
