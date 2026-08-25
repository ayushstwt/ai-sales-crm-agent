# Bruno API Testing Guide — End-to-End Workflow

This document provides a step-by-step, phase-by-phase guide for testing all REST endpoints and AI Agent flows in the **AI Sales CRM Agent** project using the [Bruno](https://www.usebruno.com/) API client.

---

## Phase 0: Prerequisites & Environment Setup

### 1. Application Setup
Ensure the PostgreSQL database is running and start the Spring Boot application:

```bash
# Verify database connection and build the project
mvn clean package

# Run the application
mvn spring-boot:run
```
The application will launch on `http://localhost:8080` and run Flyway migrations automatically (`V1` through `V10`).

### 2. Open Collection in Bruno
1. Open Bruno.
2. Click **Open Collection** and choose the `bruno` directory from the root of this repository.
3. Select the `local` environment from the top-right environment selector.

### 3. Environment Variables
The collection uses `local.bru` (`environments/local.bru`):
- `baseUrl`: `http://localhost:8080`
- `token`: (Auto-populated upon successful registration or login)

---

## Phase 1: Authentication & Organization Setup

### Step 1.1: Register Organization and Admin
- **File**: `auth/Register.bru`
- **Method**: `POST {{baseUrl}}/auth/register`
- **Payload**:
  ```json
  {
    "organizationName": "Acme Corp",
    "organizationSlug": "acme-corp",
    "email": "admin@acme.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1-555-0100"
  }
  ```
- **Expected Status**: `200 OK` or `201 Created`
- **Action**: The post-response script automatically saves the JWT token into the `token` environment variable.

### Step 1.2: Authenticate Admin (Login)
- **File**: `auth/Login.bru`
- **Method**: `POST {{baseUrl}}/auth/login`
- **Payload**:
  ```json
  {
    "email": "admin@acme.com",
    "password": "password123"
  }
  ```
- **Expected Status**: `200 OK`
- **Expected Response**: Returns `token`, `refreshToken`, user details, and assigned roles (`ORG_ADMIN`).

### Step 1.3: Create Additional Team Members
- **File**: `users/Create User.bru`
- **Method**: `POST {{baseUrl}}/users`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "email": "rep1@acme.com",
    "password": "password123",
    "firstName": "Alice",
    "lastName": "Smith",
    "userType": "SALES_REP"
  }
  ```
- **Expected Status**: `200 OK` / `201 Created`

### Step 1.4: Verify User Directory
- **File**: `users/List Users.bru`
- **Method**: `GET {{baseUrl}}/users`
- **Expected Response**: List containing registered admin and newly created users.

---

## Phase 2: CRM Core Data — Companies & Contacts

### Step 2.1: Create a Company
- **File**: `companies/Create Company.bru`
- **Method**: `POST {{baseUrl}}/companies`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "name": "Stark Industries",
    "domain": "starkindustries.com",
    "industry": "Defense & Technology",
    "phone": "+1-555-0199",
    "website": "https://starkindustries.com",
    "address": "10880 Wilshire Blvd, Los Angeles, CA"
  }
  ```
- **Expected Status**: `200 OK` / `201 Created` (Note the returned `id`, e.g., `1`).

### Step 2.2: List and View Companies
- **File**: `companies/List Companies.bru` -> `GET {{baseUrl}}/companies`
- **File**: `companies/View Company.bru` -> `GET {{baseUrl}}/companies/1`

### Step 2.3: Create a Contact Linked to Company
- **File**: `contacts/Create Contact.bru`
- **Method**: `POST {{baseUrl}}/contacts`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "companyId": 1,
    "firstName": "Tony",
    "lastName": "Stark",
    "email": "tony@starkindustries.com",
    "phone": "+1-555-3000",
    "jobTitle": "Chief Technology Officer"
  }
  ```
- **Expected Status**: `200 OK` / `201 Created` (Note the returned `id`, e.g., `1`).

### Step 2.4: List and View Contacts
- **File**: `contacts/List Contacts.bru` -> `GET {{baseUrl}}/contacts`
- **File**: `contacts/View Contact.bru` -> `GET {{baseUrl}}/contacts/1`

---

## Phase 3: Lead Lifecycle & Conversion

### Step 3.1: Create an Inbound Lead
- **File**: `leads/Create Lead.bru`
- **Method**: `POST {{baseUrl}}/leads`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "firstName": "Sarah",
    "lastName": "Connor",
    "email": "sarah.connor@cyberdyne.com",
    "phone": "+1-555-0899",
    "companyName": "Cyberdyne Systems",
    "jobTitle": "Security Director",
    "status": "NEW",
    "source": "WEBSITE",
    "notes": "Interested in AI security audit tools."
  }
  ```
- **Expected Status**: `200 OK` / `201 Created` (Note the returned `id`, e.g., `1`).

### Step 3.2: Update / Qualify Lead
- **File**: `leads/Edit Lead.bru`
- **Method**: `PUT {{baseUrl}}/leads/1`
- **Payload**:
  ```json
  {
    "status": "QUALIFIED",
    "notes": "Budget confirmed ($50,000). Ready for demo."
  }
  ```
- **Expected Status**: `200 OK`

### Step 3.3: Convert Lead to Contact/Account/Deal
- **File**: `leads/Convert Lead.bru`
- **Method**: `POST {{baseUrl}}/leads/1/convert`
- **Payload**:
  ```json
  {
    "createDeal": true,
    "dealName": "Cyberdyne AI Security Suite",
    "dealValue": 50000.00
  }
  ```
- **Expected Status**: `200 OK` (Lead status becomes `CONVERTED`).

---

## Phase 4: Sales Pipeline & Deal Management

### Step 4.1: Create / Verify Sales Pipeline
- **File**: `pipelines/Create Pipeline.bru`
- **Method**: `POST {{baseUrl}}/pipelines`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "name": "Standard Sales Pipeline",
    "isDefault": true,
    "stages": [
      { "name": "Prospecting", "orderIndex": 1, "probability": 10.0 },
      { "name": "Qualified", "orderIndex": 2, "probability": 30.0 },
      { "name": "Proposal", "orderIndex": 3, "probability": 60.0 },
      { "name": "Negotiation", "orderIndex": 4, "probability": 80.0 },
      { "name": "Closed Won", "orderIndex": 5, "probability": 100.0 },
      { "name": "Closed Lost", "orderIndex": 6, "probability": 0.0 }
    ]
  }
  ```
- **Expected Status**: `200 OK` / `201 Created`

### Step 4.2: Create a Deal
- **File**: `deals/Create Deal.bru`
- **Method**: `POST {{baseUrl}}/deals`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "companyId": 1,
    "contactId": 1,
    "pipelineStageId": 1,
    "title": "Stark Industries Cloud Migration",
    "value": 120000.00,
    "currency": "USD",
    "expectedCloseDate": "2026-12-31",
    "status": "OPEN"
  }
  ```
- **Expected Status**: `200 OK` / `201 Created` (Note deal `id`, e.g., `1`).

### Step 4.3: Move Deal Stage
- **File**: `deals/Move Deal Stage.bru`
- **Method**: `POST {{baseUrl}}/deals/1/move-stage`
- **Payload**:
  ```json
  {
    "pipelineStageId": 3,
    "status": "OPEN"
  }
  ```
- **Expected Status**: `200 OK` (Stage updated to `Proposal`).

### Step 4.4: List Deals & Overview
- **File**: `deals/List Deals.bru` -> `GET {{baseUrl}}/deals`

---

## Phase 5: Engagement, Tasks & Timeline

### Step 5.1: Create a Follow-up Task
- **File**: `tasks/Create Task.bru`
- **Method**: `POST {{baseUrl}}/tasks`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "title": "Send proposal deck to Tony Stark",
    "description": "Include security whitepaper and architectural diagram.",
    "dueDate": "2026-09-01T15:00:00",
    "priority": "HIGH",
    "status": "PENDING",
    "assignedToUserId": 1,
    "contactId": 1,
    "dealId": 1
  }
  ```
- **Expected Status**: `200 OK` / `201 Created`

### Step 5.2: Log an Activity (Call / Meeting)
- **File**: `activities/Create Activity.bru`
- **Method**: `POST {{baseUrl}}/activities`
- **Payload**:
  ```json
  {
    "organizationId": 1,
    "activityType": "CALL",
    "subject": "Discovery Call with CTO",
    "notes": "Discussed scalability, security constraints, and Q4 roadmap.",
    "contactId": 1,
    "dealId": 1,
    "performedAt": "2026-08-25T10:00:00"
  }
  ```
- **Expected Status**: `200 OK` / `201 Created`

### Step 5.3: View Customer 360 Timeline
- **File**: `activities/Customer Timeline.bru`
- **Method**: `GET {{baseUrl}}/activities/timeline?contactId=1`
- **Expected Response**: Chronological timeline displaying logged calls, tasks, status changes, and deal movements for Contact `1`.

---

## Phase 6: AI Sales Agent & Tool Execution

### Step 6.1: Ask AI to Search / Retrieve CRM Data
- **File**: `ai/Chat.bru`
- **Method**: `POST {{baseUrl}}/ai/chat`
- **Payload**:
  ```json
  {
    "conversationId": null,
    "message": "Which deals are currently open for Stark Industries and what is the next follow-up task?"
  }
  ```
- **Expected Response**:
  - `conversationId`: Auto-generated UUID.
  - `reply`: Summary of the Stark Industries deal ($120,000 value, Proposal stage) and the pending task.
  - `toolsUsed`: Shows execution of tool functions such as `getDealDetails` or `listTasksForContact`.

### Step 6.2: Multi-Turn Conversation
- **File**: `ai/Chat.bru`
- **Method**: `POST {{baseUrl}}/ai/chat`
- **Payload**:
  ```json
  {
    "conversationId": "<CONVERSATION_UUID_FROM_STEP_6.1>",
    "message": "Please log a meeting activity for this deal noting that the client agreed to review the proposal next Tuesday."
  }
  ```
- **Expected Response**: Confirmation that the activity was created via tool execution.

### Step 6.3: Two-Step Destructive Action Confirmation Flow
- **File**: `ai/Chat.bru`
- **Method**: `POST {{baseUrl}}/ai/chat`
- **Payload**:
  ```json
  {
    "conversationId": "<CONVERSATION_UUID>",
    "message": "Delete deal with ID 1"
  }
  ```
- **Expected Behavior**: The AI agent responds with a safety confirmation request containing a pending action payload rather than executing the deletion immediately.
- **Confirm Deletion**:
  - **File**: `ai/Chat.bru`
  - **Payload**:
    ```json
    {
      "conversationId": "<CONVERSATION_UUID>",
      "message": "CONFIRM"
    }
    ```
  - **Expected Status**: The deletion is executed only after explicit confirmation.

### Step 6.4: Retrieve Conversation Message History
- **File**: `ai/List Conversation Messages.bru`
- **Method**: `GET {{baseUrl}}/ai/chat/history/<CONVERSATION_UUID>`
- **Expected Response**: Complete history of user inputs, assistant responses, and tool invocation metadata.

---

## Phase 7: Audit Logging & Security Verification

### Step 7.1: Inspect System Mutation Logs
- **File**: `audit/List Audit Logs.bru`
- **Method**: `GET {{baseUrl}}/audit-logs`
- **Expected Response**: Paginated log entries capturing mutations across entities:
  - Source tracking: `MANUAL`, `AI`, `API`, `SYSTEM`
  - Target entity: `LEAD`, `DEAL`, `CONTACT`, `ACTIVITY`, `TASK`
  - Timestamp, user ID, and before/after state diffs.

### Step 7.2: View Specific Audit Log Record
- **File**: `audit/View Audit Log.bru`
- **Method**: `GET {{baseUrl}}/audit-logs/1`

---

## Phase 8: Multi-Tenancy Isolation Verification

### Step 8.1: Register a Second Organization
- **File**: `auth/Register.bru`
- **Payload**:
  ```json
  {
    "organizationName": "Wayne Enterprises",
    "organizationSlug": "wayne-ent",
    "email": "bruce@wayne.com",
    "password": "password123",
    "firstName": "Bruce",
    "lastName": "Wayne"
  }
  ```

### Step 8.2: Verify Data Segregation
- **File**: `leads/List Leads.bru`
- **File**: `deals/List Deals.bru`
- **Expected Response**: Empty array `[]` (Ensure none of Acme Corp's leads, contacts, deals, or activities are accessible to Wayne Enterprises).

---

## Summary Testing Checklist

- [ ] Phase 1: Register Org Admin, Login, Token generation, User management
- [ ] Phase 2: Create Companies and Contacts with relational linking
- [ ] Phase 3: Create Lead, Qualify Lead, Convert Lead to Deal & Contact
- [ ] Phase 4: Create Pipeline Stages, Create Deal, Move Deal Stage
- [ ] Phase 5: Create Tasks, Log Activities, Query Customer 360 Timeline
- [ ] Phase 6: AI Chat Tool Execution, Multi-turn context, Destructive Action Confirmation
- [ ] Phase 7: Audit Logs verification for MANUAL and AI operations
- [ ] Phase 8: Cross-Tenant Isolation validation
