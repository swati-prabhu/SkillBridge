package com.skillbridge.config;

import com.skillbridge.entity.*;
import com.skillbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final AptitudeTestRepository testRepository;
    private final RoadmapRepository roadmapRepository;
    private final PlacementDriveRepository placementDriveRepository;
    private final ResumeRepository resumeRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final MockInterviewRepository mockInterviewRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final RecruiterRatingRepository recruiterRatingRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // already seeded

        // ── Core accounts ──
        User admin = new User();
        admin.setFullName("Portal Admin");
        admin.setEmail("admin@skillbridge.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User student = new User();
        student.setFullName("Demo Student");
        student.setEmail("student@skillbridge.com");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setRole(Role.STUDENT);
        student.setSkills("Java,SQL,React");
        student.setDepartment("Computer Science");
        student.setGraduationYear(2026);
        userRepository.save(student);

        User alumni = new User();
        alumni.setFullName("Asha Rao");
        alumni.setEmail("alumni@skillbridge.com");
        alumni.setPassword(passwordEncoder.encode("alumni123"));
        alumni.setRole(Role.STUDENT);
        alumni.setSkills("Java,Spring Boot,AWS");
        alumni.setDepartment("Computer Science");
        alumni.setGraduationYear(2023);
        alumni.setAlumni(true);
        userRepository.save(alumni);

        // ── Companies + recruiters ──
        Company nimbus = new Company();
        nimbus.setName("Nimbus Systems");
        nimbus.setIndustry("Enterprise Software");
        nimbus.setWebsite("https://nimbussystems.example.com");
        nimbus.setDescription("Cloud infrastructure and internal tooling for mid-size enterprises.");
        nimbus.setVerified(true);
        companyRepository.save(nimbus);

        Company pixelForge = new Company();
        pixelForge.setName("Pixel Forge");
        pixelForge.setIndustry("Design & Frontend Tooling");
        pixelForge.setDescription("Design systems and frontend tooling for product teams.");
        pixelForge.setVerified(true);
        companyRepository.save(pixelForge);

        Company insightWorks = new Company();
        insightWorks.setName("InsightWorks");
        insightWorks.setIndustry("Data Analytics");
        insightWorks.setVerified(true);
        companyRepository.save(insightWorks);

        Company cascade = new Company();
        cascade.setName("Cascade Analytics");
        cascade.setIndustry("Analytics Platforms");
        cascade.setVerified(false); // demonstrates the admin verification workflow
        companyRepository.save(cascade);

        User recruiter = new User();
        recruiter.setFullName("Priya Menon");
        recruiter.setEmail("recruiter@skillbridge.com");
        recruiter.setPassword(passwordEncoder.encode("recruiter123"));
        recruiter.setRole(Role.RECRUITER);
        recruiter.setCompany(nimbus);
        recruiter.setDesignation("Talent Acquisition Lead");
        recruiter.setRecruiterVerified(true);
        userRepository.save(recruiter);

        User unverifiedRecruiter = new User();
        unverifiedRecruiter.setFullName("Rahul Iyer");
        unverifiedRecruiter.setEmail("recruiter2@skillbridge.com");
        unverifiedRecruiter.setPassword(passwordEncoder.encode("recruiter123"));
        unverifiedRecruiter.setRole(Role.RECRUITER);
        unverifiedRecruiter.setCompany(cascade);
        unverifiedRecruiter.setDesignation("Campus Hiring Manager");
        unverifiedRecruiter.setRecruiterVerified(false);
        userRepository.save(unverifiedRecruiter);

        // ── Internships ──
        Internship i1 = new Internship();
        i1.setTitle("Backend Developer Intern");
        i1.setCompany("Nimbus Systems");
        i1.setCompanyRef(nimbus);
        i1.setDescription("Work on Spring Boot microservices and REST APIs.");
        i1.setLocation("Bengaluru (Hybrid)");
        i1.setStipend("₹20,000/month");
        i1.setDuration("6 months");
        i1.setRequiredSkills("Java,Spring Boot,SQL");
        i1.setCategory("Engineering");
        i1.setPostedBy(recruiter);
        internshipRepository.save(i1);

        Internship i2 = new Internship();
        i2.setTitle("Frontend Developer Intern");
        i2.setCompany("Pixel Forge");
        i2.setCompanyRef(pixelForge);
        i2.setDescription("Build responsive UI components with React and Bootstrap.");
        i2.setLocation("Remote");
        i2.setStipend("₹15,000/month");
        i2.setDuration("3 months");
        i2.setRequiredSkills("JavaScript,React,CSS");
        i2.setCategory("Engineering");
        i2.setPostedBy(admin);
        internshipRepository.save(i2);

        Internship i3 = new Internship();
        i3.setTitle("Data Analyst Intern");
        i3.setCompany("InsightWorks");
        i3.setCompanyRef(insightWorks);
        i3.setDescription("Analyze application funnels and build dashboards.");
        i3.setLocation("Mumbai");
        i3.setStipend("₹18,000/month");
        i3.setDuration("4 months");
        i3.setRequiredSkills("SQL,Python,Excel");
        i3.setCategory("Data");
        i3.setPostedBy(admin);
        internshipRepository.save(i3);

        PlacementDrive drive = new PlacementDrive();
        drive.setName("Fall 2026 Campus Drive");
        drive.setCompany("Cascade Analytics");
        drive.setDescription("On-campus recruiting drive for full-stack and data roles.");
        drive.setDriveDate(LocalDate.now().plusDays(21));
        drive.setActive(true);
        placementDriveRepository.save(drive);

        Internship i4 = new Internship();
        i4.setTitle("Full-Stack Developer Intern");
        i4.setCompany("Cascade Analytics");
        i4.setCompanyRef(cascade);
        i4.setDescription("Work across a Spring Boot backend and a React frontend on internal tooling.");
        i4.setLocation("Hybrid — Bengaluru");
        i4.setStipend("₹22,000/month");
        i4.setDuration("6 months");
        i4.setRequiredSkills("Java,React,SQL,REST APIs");
        i4.setCategory("Engineering");
        i4.setPostedBy(admin);
        i4.setDrive(drive);
        internshipRepository.save(i4);

        // ── Sample applications (drives placement statistics + recruiter dashboard) ──
        Application app1 = new Application();
        app1.setStudent(student);
        app1.setInternship(i1);
        app1.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        ApplicationEvent e1a = new ApplicationEvent();
        e1a.setApplication(app1); e1a.setStatus(ApplicationStatus.APPLIED); e1a.setNote("Application submitted");
        ApplicationEvent e1b = new ApplicationEvent();
        e1b.setApplication(app1); e1b.setStatus(ApplicationStatus.SHORTLISTED); e1b.setNote("Shortlisted after resume screen");
        ApplicationEvent e1c = new ApplicationEvent();
        e1c.setApplication(app1); e1c.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED); e1c.setNote("Interview scheduled");
        app1.getTimeline().add(e1a); app1.getTimeline().add(e1b); app1.getTimeline().add(e1c);
        applicationRepository.save(app1);

        Interview interview = new Interview();
        interview.setApplication(app1);
        interview.setStudent(student);
        interview.setScheduledAt(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0));
        interview.setMode("Video Call");
        interview.setNotes("Technical round - Java & Spring Boot fundamentals.");
        interviewRepository.save(interview);

        Application app2 = new Application();
        app2.setStudent(alumni); // seeding a SELECTED application under the alumni account for placement stats
        app2.setInternship(i3);
        app2.setStatus(ApplicationStatus.SELECTED);
        app2.setOfferedPackage(6.5);
        ApplicationEvent e2a = new ApplicationEvent();
        e2a.setApplication(app2); e2a.setStatus(ApplicationStatus.APPLIED); e2a.setNote("Application submitted");
        ApplicationEvent e2b = new ApplicationEvent();
        e2b.setApplication(app2); e2b.setStatus(ApplicationStatus.SELECTED); e2b.setNote("Offer extended");
        app2.getTimeline().add(e2a); app2.getTimeline().add(e2b);
        applicationRepository.save(app2);

        // ── Resume for demo student ──
        Resume resume = new Resume();
        resume.setUser(student);
        resume.setHeadline("Aspiring Backend Developer");
        resume.setSummary("Final-year CS student focused on backend development with Java and Spring Boot. Built and deployed a full internship portal (SkillBridge) end-to-end.");
        resume.setGithub("github.com/demo-student");
        resume.setLinkedin("linkedin.com/in/demo-student");

        ResumeEducation edu = new ResumeEducation();
        edu.setResume(resume);
        edu.setInstitution("NMAM Institute of Technology");
        edu.setDegree("B.Tech, Computer Science");
        edu.setStartYear("2022");
        edu.setEndYear("2026");
        resume.getEducation().add(edu);

        ResumeProject proj = new ResumeProject();
        proj.setResume(resume);
        proj.setName("SkillBridge");
        proj.setTechStack("Java, Spring Boot, MySQL, Thymeleaf, Docker");
        proj.setDescription("Full-stack internship and career-prep portal with role-based auth, application tracking, and an MCP server exposing portal data to AI agents.");
        resume.getProjects().add(proj);

        resume.setScore(55); // headline+summary+education+one project+skills, no work experience yet
        resumeRepository.save(resume);

        // ── Aptitude test ──
        AptitudeTest test = new AptitudeTest();
        test.setTitle("General Aptitude & Java Basics");
        test.setDescription("10-minute screening test covering logic and core Java.");
        addQuestions(test);
        testRepository.save(test);

        // ── Coding contest (a time-boxed AptitudeTest) ──
        AptitudeTest contest = new AptitudeTest();
        contest.setTitle("SkillBridge Winter Coding Contest");
        contest.setDescription("MCQ round plus two external coding problems. Leaderboard ranks by score, then submission speed.");
        contest.setContest(true);
        contest.setStartsAt(LocalDateTime.now().minusDays(1));
        contest.setEndsAt(LocalDateTime.now().plusDays(6));
        contest.setCodingLinks("https://leetcode.com/problems/two-sum/,https://leetcode.com/problems/valid-parentheses/");
        addQuestions(contest);
        testRepository.save(contest);

        // ── Roadmaps ──
        seedRoadmap("Backend Developer Roadmap", "Backend Developer", "bi-hdd-network", "Java,Spring Boot,SQL",
                "Path to becoming an internship-ready backend developer.",
                new String[][]{
                        {"Core Java Fundamentals", "OOP, collections, exceptions, streams.", "https://docs.oracle.com/javase/tutorial/"},
                        {"SQL & Relational Databases", "Joins, indexing, normalization, transactions.", "https://www.postgresqltutorial.com/"},
                        {"Spring Boot Basics", "REST APIs, dependency injection, MVC.", "https://spring.io/guides"},
                        {"Spring Security", "Authentication, authorization, JWT/session auth.", "https://spring.io/projects/spring-security"},
                        {"Testing", "JUnit, Mockito, integration tests.", "https://junit.org/junit5/"},
                        {"Docker & Deployment", "Containerizing a Spring Boot app.", "https://docs.docker.com/get-started/"},
                        {"Build a Capstone Project", "Ship something like SkillBridge end-to-end.", ""}
                });

        seedRoadmap("Frontend Developer Roadmap", "Frontend Developer", "bi-window", "JavaScript,React,CSS",
                "Path to becoming an internship-ready frontend developer.",
                new String[][]{
                        {"HTML & CSS Fundamentals", "Semantic HTML, Flexbox, Grid.", "https://web.dev/learn/css/"},
                        {"JavaScript Core Concepts", "DOM, async/await, ES6+.", "https://javascript.info/"},
                        {"React Basics", "Components, hooks, state management.", "https://react.dev/learn"},
                        {"Responsive Design", "Bootstrap/Tailwind, mobile-first design.", "https://getbootstrap.com/"},
                        {"API Integration", "Fetch/Axios, handling loading & error states.", ""},
                        {"Version Control", "Git branching, pull requests, code review.", "https://learngitbranching.js.org/"},
                        {"Build a Portfolio Project", "Deploy a polished, responsive app.", ""}
                });

        seedRoadmap("Data Analyst Roadmap", "Data Analyst", "bi-bar-chart-line", "SQL,Python,Excel",
                "Path to becoming an internship-ready data analyst.",
                new String[][]{
                        {"Excel & Spreadsheets", "Pivot tables, formulas, data cleaning.", ""},
                        {"SQL for Analysis", "Aggregations, window functions, CTEs.", "https://www.postgresqltutorial.com/"},
                        {"Python for Data", "Pandas, NumPy basics.", "https://pandas.pydata.org/docs/getting_started/"},
                        {"Data Visualization", "Matplotlib/Seaborn or Chart.js dashboards.", ""},
                        {"Statistics Fundamentals", "Distributions, hypothesis testing, correlation.", ""},
                        {"Build a Dashboard Project", "End-to-end analysis with a clear narrative.", ""}
                });

        // ── Mock interview ──
        MockInterview mock = new MockInterview();
        mock.setTitle("Backend Developer Mock Interview");
        mock.setDescription("Common conceptual questions for a backend/Java internship screen.");
        mock.setTrack("Backend Developer");

        addMockQuestion(mock, "Explain the difference between an ArrayList and a LinkedList.",
                "array, index, linked list, insertion, deletion, performance", 0);
        addMockQuestion(mock, "What is dependency injection and why does Spring use it?",
                "dependency injection, spring, loose coupling, testability, container", 1);
        addMockQuestion(mock, "How would you design a REST API for a simple to-do list app?",
                "rest, endpoint, get, post, put, delete, resource, status code", 2);
        mockInterviewRepository.save(mock);

        // ── Forum seed posts ──
        ForumPost post1 = new ForumPost();
        post1.setAuthor(alumni);
        post1.setTitle("My Nimbus Systems interview experience");
        post1.setBody("Two rounds: a DSA screen (arrays/strings) followed by a Spring Boot system design chat. "
                + "They cared more about how I reasoned through trade-offs than getting the \"optimal\" answer immediately.");
        post1.setCategory(ForumCategory.INTERVIEW_EXPERIENCE);
        post1.setCompanyTag("Nimbus Systems");
        forumPostRepository.save(post1);

        ForumComment comment1 = new ForumComment();
        comment1.setPost(post1);
        comment1.setAuthor(student);
        comment1.setBody("This is really helpful, thank you! Did they ask about testing at all?");
        forumCommentRepository.save(comment1);

        ForumPost post2 = new ForumPost();
        post2.setAuthor(admin);
        post2.setTitle("Resume tip: quantify your project impact");
        post2.setBody("\"Built a REST API\" is weaker than \"Built a REST API handling 500+ requests/day, cutting manual data entry by 80%.\" "
                + "Even estimated numbers on a class project are better than none.");
        post2.setCategory(ForumCategory.RESUME_TIPS);
        forumPostRepository.save(post2);

        // ── Recruiter rating ──
        RecruiterRating rating = new RecruiterRating();
        rating.setStudent(alumni);
        rating.setCompany(nimbus);
        rating.setDifficulty(4);
        rating.setCommunication(5);
        rating.setProcess(4);
        rating.setComment("Clear communication throughout, and they gave detailed feedback even after rejecting my first application.");
        recruiterRatingRepository.save(rating);

        // ── Saved search ──
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setStudent(student);
        savedSearch.setKeyword("backend");
        savedSearch.setCategory("Engineering");
        savedSearchRepository.save(savedSearch);

        // ── A couple of starter notifications so the bell isn't empty on first login ──
        Notification n1 = new Notification();
        n1.setUser(student);
        n1.setMessage("Welcome to SkillBridge! Complete your Resume Builder to get personalized recommendations.");
        n1.setLink("/resume");
        notificationRepository.save(n1);

        Notification n2 = new Notification();
        n2.setUser(student);
        n2.setMessage("Your application for \"Backend Developer Intern\" at Nimbus Systems is now INTERVIEW SCHEDULED.");
        n2.setLink("/applications/my");
        notificationRepository.save(n2);
    }

    private void addQuestions(AptitudeTest test) {
        Question q1 = new Question();
        q1.setTest(test);
        q1.setQuestionText("Which keyword is used to inherit a class in Java?");
        q1.setOptionA("implements");
        q1.setOptionB("extends");
        q1.setOptionC("inherits");
        q1.setOptionD("super");
        q1.setCorrectOption("B");

        Question q2 = new Question();
        q2.setTest(test);
        q2.setQuestionText("What does JVM stand for?");
        q2.setOptionA("Java Virtual Machine");
        q2.setOptionB("Java Verified Method");
        q2.setOptionC("Java Variable Manager");
        q2.setOptionD("Joint Virtual Machine");
        q2.setCorrectOption("A");

        Question q3 = new Question();
        q3.setTest(test);
        q3.setQuestionText("Which HTTP method is idempotent and used to update a resource fully?");
        q3.setOptionA("POST");
        q3.setOptionB("PATCH");
        q3.setOptionC("PUT");
        q3.setOptionD("GET");
        q3.setCorrectOption("C");

        test.getQuestions().add(q1);
        test.getQuestions().add(q2);
        test.getQuestions().add(q3);
    }

    private void addMockQuestion(MockInterview mock, String text, String hints, int order) {
        MockInterviewQuestion q = new MockInterviewQuestion();
        q.setMockInterview(mock);
        q.setQuestionText(text);
        q.setIdealAnswerHints(hints);
        q.setOrderIndex(order);
        mock.getQuestions().add(q);
    }

    private void seedRoadmap(String title, String track, String icon, String relatedSkills, String description, String[][] steps) {
        Roadmap roadmap = new Roadmap();
        roadmap.setTitle(title);
        roadmap.setTrack(track);
        roadmap.setIcon(icon);
        roadmap.setRelatedSkills(relatedSkills);
        roadmap.setDescription(description);

        int order = 0;
        for (String[] s : steps) {
            RoadmapStep step = new RoadmapStep();
            step.setRoadmap(roadmap);
            step.setTitle(s[0]);
            step.setDescription(s[1]);
            step.setResourceLink(s.length > 2 ? s[2] : null);
            step.setOrderIndex(order++);
            roadmap.getSteps().add(step);
        }
        roadmapRepository.save(roadmap);
    }
}
