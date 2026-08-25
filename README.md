# AI Sales CRM Agent

A multi-tenant backend for an AI-powered Sales CRM featuring Spring AI tool calling, tenant isolation, role-based access control (RBAC), and automated audit logging.

---

## Overview

AI Sales CRM Agent bridges modern CRM workflows with AI agent capabilities. It provides full multi-tenant CRM management (leads, contacts, companies, pipelines, deals, tasks, and activities) alongside an AI chat agent capable of performing contextual tool calling, generating Customer 360 summaries, and executing CRM actions safely with multi-step confirmation for destructive changes.

---

## Key Features

- Multi-Tenancy: Organization-level tenant isolation enforced at the service and data layer.
- Authentication and RBAC: JWT authentication (access and refresh tokens) with role-based access controls across Admin, Manager, and Rep roles.
- Core CRM Modules:
  - Leads: Lifecycle management, status tracking, conversion pipelines.
  - Contacts and Companies: Account mapping, contact associations.
  - Pipelines and Deals: Multi-stage pipeline tracking, deal valuations, win/loss tracking.
  - Tasks and Activities: Task scheduling, call/meeting logs, historical timeline items.
  - Customer 360: Aggregated profile views across contacts, deals, activities, and AI insights.
- AI Agent with Tool Calling:
  - Spring AI integration with OpenAI GPT models.
  - Real tool calling directly against application service layers.
  - Tools for lead search/lookup, deal analysis, task creation, activity logging, and Customer 360 extraction.
- AI Safety and Confirmation:
  - Two-step confirmation flow for destructive or critical actions before mutating data.
  - System prompt safeguards treating user-provided data strictly as context, preventing prompt injection.
- Audit Logging:
  - Granular mutation logging tracking changes from MANUAL, AI, API, and SYSTEM sources.
- Database Migrations:
  - Managed version-controlled migrations using Flyway for PostgreSQL.

---

## Tech Stack

- Language: Java 21
- Framework: Spring Boot 3.3.5
- AI Framework: Spring AI (1.0.0-M1) with OpenAI integration
- Security: Spring Security, JJWT (io.jsonwebtoken 0.12.6)
- Persistence: Spring Data JPA, Hibernate, PostgreSQL Driver
- Database: PostgreSQL 16 (with pgvector support), Flyway Migrations, H2 (test/runtime fallback)
- Build Tool: Maven

---

## Architecture Principles

1. Tenant Isolation: Every query and mutation is scoped to the authenticated user's organization.
2. Layered Tool Execution: AI tool calls flow through Application Services and Security layers rather than invoking repositories directly.
3. Two-Step Destructive Action Confirmation: Critical state mutations initiated by AI generate a pending action payload requiring explicit user confirmation.
4. Mutation Auditing: Any create, update, or delete operation writes an entry to the audit log with source attribution.
5. Standard Response Pattern: Uniform REST response structure wrapping data, statuses, and error codes.

---

## Project Structure

```
ai-sales-crm-agent/
├── bruno/                      # Bruno API request collections
├── docs/                       # Project documentation and architectural specs
├── src/
│   ├── main/
│   │   ├── java/com/ayshriv/salescrm/
│   │   │   ├── activity/       # Activity tracking and timeline
│   │   │   ├── ai/             # AI chat controller, tools, context, and services
│   │   │   ├── audit/          # Audit logging
│   │   │   ├── auth/           # Authentication, JWT tokens, user credentials
│   │   │   ├── common/         # BaseEntity, DTOs, exceptions, response wrappers
│   │   │   ├── company/        # Company management
│   │   │   ├── contact/        # Contact management
│   │   │   ├── customer/       # Customer 360 aggregation
│   │   │   ├── deal/           # Deals and pipeline stages
│   │   │   ├── lead/           # Lead qualification and lifecycle
│   │   │   ├── pipeline/       # Sales pipeline stages
│   │   │   ├── task/           # Task tracking and assignment
│   │   │   └── user/           # User administration and role management
│   │   └── resources/
│   │       ├── application.yml # Spring Boot configuration
│   │       └── db/migration/   # Flyway SQL migration scripts (V1 through V10)
│   └── test/                   # Integration and unit tests
└── pom.xml                     # Maven build and dependency configuration
```

---

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- Apache Maven 3.9+
- PostgreSQL 16+
- OpenAI API Key

### Configuration

Set up your environment variables or adjust `src/main/resources/application.yml`:

```properties
OPENAI_API_KEY=your-openai-api-key
OPENAI_MODEL=gpt-4o-mini
```

Ensure your PostgreSQL instance is running with a database matching your configuration:

```sql
CREATE DATABASE salescrm;
CREATE USER salescrm_app WITH ENCRYPTED PASSWORD 'salescrm_password';
GRANT ALL PRIVILEGES ON DATABASE salescrm TO salescrm_app;
```

### Build and Run

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd ai-sales-crm-agent
   ```

2. Compile and package the application:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application starts on port `8080` by default. Flyway migrations will run automatically on startup.

---

## API Reference Overview

### Authentication
- `POST /api/auth/register` - Register a new organization and admin account
- `POST /api/auth/login` - Authenticate and obtain JWT access and refresh tokens
- `POST /api/auth/refresh` - Refresh access token

### CRM Operations
- `GET /api/leads`, `POST /api/leads` - List and create leads
- `GET /api/contacts`, `POST /api/contacts` - List and create contacts
- `GET /api/companies`, `POST /api/companies` - List and create companies
- `GET /api/deals`, `POST /api/deals` - List and manage deals
- `GET /api/pipelines` - List pipeline stages
- `GET /api/tasks`, `POST /api/tasks` - List and create assigned tasks
- `GET /api/activities`, `POST /api/activities` - Activity logs and timelines
- `GET /api/customer-360/{id}` - Complete 360-degree customer overview

### AI Chat and Tool Calling
- `POST /api/ai/chat` - Send user message to the AI sales agent
- `POST /api/ai/chat/confirm` - Confirm or reject pending destructive actions
- `GET /api/ai/chat/history/{conversationId}` - Retrieve conversation history

### Audit Logs
- `GET /api/audit-logs` - View system mutation history and action sources

---

## Testing the APIs

API collections are provided in the `bruno/` directory for testing with Bruno API client.

To run automated test suites:
```bash
mvn test
```

---

## License

This project is open source and available under the MIT License.
