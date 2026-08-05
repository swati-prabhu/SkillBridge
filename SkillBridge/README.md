# SkillBridge — Internship & Career Preparation Portal

A full-stack internship/career portal with role-based auth, application tracking,
aptitude assessments, resume uploads, an analytics dashboard, and an **MCP server**
that exposes the portal's data as tools an AI agent (e.g. Claude Desktop) can call directly.

## Tech Stack

| Layer          | Technology                                             |
|----------------|---------------------------------------------------------|
| Backend        | Java 17, Spring Boot 3 (Web, Security, Data JPA)        |
| Frontend       | Thymeleaf, Bootstrap 5, vanilla JS, Chart.js             |
| Database       | MySQL 8                                                  |
| Auth           | Spring Security (form login, BCrypt, role-based access) |
| AI Integration | Custom MCP server (Node.js) over the same MySQL database |
| Containerization | Docker, Docker Compose (3 services: app, db, mcp-server) |

## Architecture (MVC + service/repository layers)

```
Browser ── Thymeleaf views ── Controllers ── Services ── Repositories ── MySQL
                                                              │
                                            MCP server (Node.js) ── same MySQL DB
                                                              │
                                              Claude Desktop / any MCP client
```

The MCP server is a **separate, independent process** — it doesn't touch your
Spring Boot code. It just reads the same database, which is exactly how a real
company might expose "read-only agent access" to an internal system without
re-architecting it.

## Project Structure

```
SkillBridge/
├── docker-compose.yml
├── database/
│   └── schema.sql                  # reference schema (Hibernate also auto-creates it)
├── app/                            # Spring Boot application
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/skillbridge/
│       ├── entity/                 # User, Internship, Application, AptitudeTest, Question, TestResult,
│       │                           # Roadmap, RoadmapStep, RoadmapProgress, Bookmark, Notification
│       ├── repository/             # Spring Data JPA interfaces
│       ├── service/                # business logic
│       ├── controller/             # MVC + one small JSON API for the analytics chart
│       ├── dto/
│       ├── config/                 # SecurityConfig, DataInitializer (demo seed data)
│       └── SkillBridgeApplication.java
│   └── src/main/resources/
│       ├── templates/              # Thymeleaf pages
│       ├── static/{css,js}/
│       └── application.properties
└── mcp-server/                     # Node.js MCP server
    ├── Dockerfile
    ├── package.json
    └── index.js
```

## Features

### v4 additions — Company Portal expansion
This round added a third role (Recruiter/Company) and 19 more features. Full list,
grouped, with honest notes on what's heuristic vs. what would need a real LLM:

- **Recruiter role** — recruiters register under a company, post/edit/delete their
  own internships (ownership-checked server-side, not just hidden in the UI),
  view applicants, shortlist/hire/reject, schedule interviews, set an offered
  package on hire. Admins verify companies and recruiter accounts (`/admin/companies`).
- **AI Resume Review** & **AI Placement Assistant** (chat widget) — both are
  **rule-based, not LLM calls** (no API key configured for this project). This is
  stated explicitly in the UI and in code comments (`AiResumeReviewService`,
  `AiAssistantService`) so nobody mistakes it for something it isn't. Both reuse
  the same service methods the MCP server exposes to external agents.
- **ATS Resume Match** — resume vs. an internship's required skills (+ optional
  pasted JD text), match %, missing skills, actionable suggestions.
- **Calendar Integration** — real `.ics` file export per interview (RFC 5545),
  opens directly in Google/Outlook/Apple Calendar. No Google Calendar OAuth
  integration (would need a registered Cloud project) — this is the
  self-contained alternative.
- **Certificates** — PDF certificates (OpenPDF) for completed aptitude tests,
  100%-complete roadmaps, and reviewed mock interviews.
- **Mock Interview Module** — text-based Q&A (not audio/video — a bigger infra
  lift flagged as a next step), auto-heuristic preliminary score on submit,
  admin review afterward.
- **Student Portfolio** — GitHub/LinkedIn/portfolio links, skills, education,
  experience, projects, certifications, all in the Resume Builder.
- **Coding Contest Module** — a "contest" is a time-boxed `AptitudeTest`
  (start/end time + external coding-problem links) reusing all existing
  test-taking infrastructure, with a real leaderboard (score desc, then
  submission time asc).
- **Notification Center** — full `/notifications` page: mark individual/all
  read, delete, in addition to the existing bell dropdown.
- **Student Analytics** — see the existing `/progress` page (resume score,
  aptitude average, roadmap completion, composite readiness score).
- **Internship Recommendation Engine (weighted)** — blends "how much of the
  role's requirements you cover" with "how focused the match is relative to
  your whole skillset" via an F1-like harmonic mean, plus a plain-English
  explanation per recommendation (`InternshipService.recommendWeighted`).
- **Saved Search Alerts** — save a keyword/category combo, get notified
  automatically the next time a matching internship is posted.
- **Placement Statistics** — placed students, avg/highest package, offers by
  company, placements by department, with charts (`/placement-statistics`).
- **Discussion Forum** — interview experiences, resume tips, company reviews;
  flag + admin hide/unhide moderation.
- **Recruiter Ratings** — anonymous (student identity stored only to prevent
  duplicate ratings, never displayed) difficulty/communication/process ratings
  per company, shown on the company's public page and the recruiter's own
  dashboard.
- **Referral Requests** — student → alumni (`User.alumni = true`) request/
  approve/decline workflow, notifies both sides.
- **Skill Gap Analysis** — pick a target internship, see exactly which required
  skills you're missing and the roadmap that covers the most of that gap.
- **Admin Reports** — PDF exports (OpenPDF) for placements, interviews, and
  student application progress (`/reports`).
- **Real-time Dashboard** — genuine STOMP-over-WebSocket (`spring-boot-starter-websocket`,
  SockJS client). Every application status change broadcasts to
  `/topic/applications` (admin/recruiter views) and `/topic/applications/{studentId}`
  (that student specifically). Currently wired into the admin and recruiter
  applicant-management pages as a live "something changed, refresh" banner —
  not full in-place DOM patching, which is the honest next step.

- **Role-based auth** — Students and Admins see different dashboards and permissions (Spring Security).
- **Internship listings** — free-text search, category filter, and pagination (Spring Data `Pageable`); admin can post/edit/delete.
- **Application tracking with a real timeline** — every status change (not just the current status) is recorded as an `ApplicationEvent` and rendered as a timeline on the student's Applications page.
- **Interview scheduling + email** — admins schedule an interview from the Manage Applications page; it auto-updates the application status, notifies the student in-app, and sends an email via `JavaMailSender` (falls back to logging if SMTP isn't configured — see `EmailService`).
- **Placement Drives** — admins can group internships under a recruiting drive/event and activate/deactivate drives.
- **Audit Log** — every admin action is recorded with actor, action, entity, and timestamp, viewable at `/admin/audit-log`.
- **Resume Builder + PDF export + scoring** — structured resume builder with a transparent 0–100 completeness score and one-click PDF export via OpenPDF.

### How aptitude tests, roadmaps, bookmarks, notifications, and analytics tie together

These five aren't standalone screens bolted on separately — they feed into
and trigger each other:

- **Aptitude Assessments** — MCQ tests, auto-scored on submit. Completing one
  immediately (1) sends the student an in-app **notification** with their
  score, (2) updates their average on the **Progress Analytics** page, and
  (3) feeds the composite readiness score.
- **Personalized Career Roadmaps** — roadmaps carry a `relatedSkills` tag.
  `RoadmapService.recommendForSkills(...)` matches a student's profile skills
  against every roadmap and surfaces the best match — as a highlighted tile
  on the dashboard, on the Roadmaps list, and on the Progress page — using
  the exact same skill-overlap logic (`SkillMatcher`) the internship
  recommender uses, so "personalized" means the same thing everywhere in
  the app. Crossing a 25/50/75/100% milestone on any roadmap fires a
  **notification** automatically (`RoadmapService.toggleStep`).
- **Bookmarks** — saved internships show up as a KPI on both the dashboard
  and the Progress page, and the Saved list reuses the same internship card
  component as the main listings (bookmark state, apply button, everything
  stays consistent).
- **Notifications** — beyond application-status and interview updates, the
  bell now fires for: aptitude test results, roadmap milestones, and **new
  internship postings that match a student's skills** (`InternshipService
  .notifyMatchingStudents`, triggered the moment an admin posts one) — so a
  student who fills in their skills actually hears about relevant postings
  without re-checking the listings page.
- **Progress Analytics** (`/progress`, student-facing — distinct from the
  admin-facing `/analytics`) — a single page pulling all of the above into
  one view: a composite **Career Readiness Score** (40% resume + 30%
  aptitude average + 30% roadmap completion), an aptitude score trend line
  chart, an application-status doughnut chart, a roadmap progress list, and
  the personalized roadmap recommendation. This is the page that makes the
  "these features feed each other" argument concrete rather than just
  asserted.

- **Skill-based internship recommendations** — the student dashboard surfaces
  a "Recommended for You" section using the same `SkillMatcher` utility
  described above (also exposed via the MCP server).
- **Analytics dashboard (admin)** — students, internships, applications,
  interviews, placement drives, avg. aptitude score, and avg. resume score,
  plus a Chart.js bar chart fed by a small JSON API (`/api/analytics/summary`).
- **MCP server** — 9 tools any MCP-compatible AI client can call:
  - `search_internships`
  - `get_student_applications`
  - `get_analytics_summary`
  - `recommend_internships_for_student` (skill-overlap matching)
  - `get_roadmap_progress` (percent complete + remaining steps per roadmap)
  - `get_upcoming_interviews`
  - `get_placement_drives`
  - `get_resume_score`
  - `get_student_progress` (composite readiness score - resume + aptitude + roadmaps)

## Running It (Docker — recommended)

You'll need Docker Desktop installed. From the `SkillBridge/` folder:

```bash
docker compose up --build
```

This starts three containers:
- `skillbridge-db` — MySQL 8, seeded with schema on first boot
- `skillbridge-app` — Spring Boot app on **http://localhost:8080**
- `skillbridge-mcp` — the MCP server (runs on stdio, meant to be launched by an MCP client — see below)

First boot takes a minute or two while Maven builds the app image. Once it's up:

- Visit **http://localhost:8080/login**
- Demo admin: `admin@skillbridge.com` / `admin123`
- Demo student: `student@skillbridge.com` / `student123`
- Demo alumni (can approve referrals): `alumni@skillbridge.com` / `alumni123`
- Demo recruiter (verified, Nimbus Systems): `recruiter@skillbridge.com` / `recruiter123`
- Demo recruiter (unverified, for the admin-verification flow): `recruiter2@skillbridge.com` / `recruiter123`

To stop: `docker compose down` (add `-v` to also wipe the database volume).

**Email (optional):** to actually send interview-scheduling emails instead of
just logging them, export `SMTP_HOST`, `SMTP_USER`, `SMTP_PASS` (and optionally
`SMTP_PORT`, `SMTP_FROM`) before running `docker compose up` — they're passed
through to the app container automatically. Without them, `EmailService` logs
the email content to the console, so the feature still works end-to-end for a
demo.

## Running It Without Docker (local dev)

1. Install Java 17, Maven, MySQL 8, Node.js 20.
2. Create the database: `mysql -u root -p < database/schema.sql`
3. Update `app/src/main/resources/application.properties` with your local MySQL credentials.
4. From `app/`: `mvn spring-boot:run`
5. From `mcp-server/`: `npm install` (set `DB_HOST=localhost` etc. as env vars, or edit the defaults in `index.js`)

## Connecting the MCP Server to Claude Desktop

The MCP server communicates over stdio, so an MCP client (like Claude Desktop) launches
it as a subprocess rather than you running it standalone. Add this to your Claude Desktop
config (`claude_desktop_config.json`):

```json
{
  "mcpServers": {
    "skillbridge": {
      "command": "node",
      "args": ["/absolute/path/to/SkillBridge/mcp-server/index.js"],
      "env": {
        "DB_HOST": "localhost",
        "DB_USER": "skillbridge",
        "DB_PASSWORD": "skillbridge",
        "DB_NAME": "skillbridge"
      }
    }
  }
}
```

Restart Claude Desktop, and you'll be able to ask things like *"What internships
match this student's skills?"* or *"Show me the application funnel"* and Claude
will call the tools directly against your database.

## Notes on Scope

- CSRF protection is disabled in `SecurityConfig` for simplicity — in a production
  build you'd re-enable it and wire CSRF tokens into the Thymeleaf forms.
- `spring.jpa.hibernate.ddl-auto=update` auto-creates tables from the entities;
  `database/schema.sql` is there for documentation/grading purposes and for the
  MySQL init script Docker uses.
- Resume **file uploads** (Profile page) and the **Resume Builder** (`/resume`)
  are deliberately separate: the file upload is the original "attach your own
  PDF" flow, while the builder is a structured, in-app resume with scoring and
  its own PDF export. Keeping both shows two different but common patterns.
- Resume files are stored on local disk (a Docker volume in the compose setup) —
  swap in S3/Cloud Storage for a production deployment.
- Email is sent via `spring-boot-starter-mail`. Without `SMTP_HOST` configured,
  `EmailService` logs the email instead of throwing — so interview scheduling
  works end-to-end even with no mail server set up, same safety net as the
  Next.js version's Nodemailer stub.
- Resume PDFs are generated with **OpenPDF** (a maintained iText 4 fork, LGPL/MPL
  licensed) rather than a headless-browser approach — lighter weight and no
  extra runtime dependency, at the cost of simpler layout control.
- The recommendation logic (both in-app and in the MCP server) is intentionally
  simple (skill keyword overlap) — a good next step to mention in interviews is
  swapping it for embeddings-based matching.

## Suggested Resume Bullets

> Developed **SkillBridge**, a full-stack internship and career-preparation
> portal using **Java, Spring Boot, MySQL, Thymeleaf, Bootstrap, and Docker**,
> implementing role-based authentication, application tracking with a full
> status timeline, interview scheduling with automated email notifications,
> a resume builder with PDF export and scoring, placement drive management,
> an admin audit log, and an analytics dashboard following **MVC +
> service/repository architecture**.

> Built a custom **MCP (Model Context Protocol) server** exposing the
> portal's internship and application data as callable tools for AI
> agents, enabling natural-language querying and skill-based internship
> recommendations — containerized alongside the main application with
> **Docker Compose**.

## What to Learn/Explain If Asked in an Interview

- Why MVC + service/repository layers (separation of concerns, testability)
- How Spring Security's `UserDetailsService` + BCrypt work together
- Why the MCP server is a separate process instead of baked into the Java app
- Trade-offs of `ddl-auto=update` vs. real migrations (Flyway/Liquibase) — worth
  mentioning as a "next step" you're aware of
