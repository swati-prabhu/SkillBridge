-- SkillBridge schema (reference — Hibernate auto-creates these via ddl-auto=update,
-- but this file documents the structure and can be used for manual setup/grading).

CREATE DATABASE IF NOT EXISTS skillbridge;
USE skillbridge;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    resume_path VARCHAR(255),
    skills VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS internships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    company VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(120),
    stipend VARCHAR(50),
    duration VARCHAR(50),
    required_skills VARCHAR(255),
    category VARCHAR(60) DEFAULT 'General',
    posted_by BIGINT,
    drive_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    status VARCHAR(30) DEFAULT 'APPLIED',
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (internship_id) REFERENCES internships(id)
);

CREATE TABLE IF NOT EXISTS aptitude_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_id BIGINT NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    option_a VARCHAR(255),
    option_b VARCHAR(255),
    option_c VARCHAR(255),
    option_d VARCHAR(255),
    correct_option CHAR(1),
    FOREIGN KEY (test_id) REFERENCES aptitude_tests(id)
);

CREATE TABLE IF NOT EXISTS test_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL,
    taken_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (test_id) REFERENCES aptitude_tests(id)
);

CREATE TABLE IF NOT EXISTS roadmaps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    track VARCHAR(80),
    icon VARCHAR(40),
    related_skills VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roadmap_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roadmap_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(400),
    resource_link VARCHAR(255),
    order_index INT,
    FOREIGN KEY (roadmap_id) REFERENCES roadmaps(id)
);

CREATE TABLE IF NOT EXISTS roadmap_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    step_id BIGINT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    completed_at DATETIME,
    UNIQUE KEY uk_student_step (student_id, step_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (step_id) REFERENCES roadmap_steps(id)
);

CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    internship_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_internship (student_id, internship_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (internship_id) REFERENCES internships(id)
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    link VARCHAR(255),
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS application_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    note VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(id)
);

CREATE TABLE IF NOT EXISTS placement_drives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(150) NOT NULL,
    description TEXT,
    drive_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Now that placement_drives exists, wire up the FK left pending on internships.
ALTER TABLE internships ADD CONSTRAINT fk_internship_drive FOREIGN KEY (drive_id) REFERENCES placement_drives(id);

CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    scheduled_at DATETIME NOT NULL,
    mode VARCHAR(40) DEFAULT 'Video Call',
    notes VARCHAR(400),
    email_sent_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES applications(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT NOT NULL,
    action VARCHAR(40) NOT NULL,
    entity VARCHAR(60) NOT NULL,
    entity_id VARCHAR(60),
    details VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    headline VARCHAR(150),
    summary TEXT,
    github VARCHAR(255),
    linkedin VARCHAR(255),
    portfolio VARCHAR(255),
    score INT DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS resume_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    institution VARCHAR(150) NOT NULL,
    degree VARCHAR(150) NOT NULL,
    start_year VARCHAR(10),
    end_year VARCHAR(10),
    FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

CREATE TABLE IF NOT EXISTS resume_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    company VARCHAR(150) NOT NULL,
    role VARCHAR(150) NOT NULL,
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    description VARCHAR(500),
    FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

CREATE TABLE IF NOT EXISTS resume_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    link VARCHAR(255),
    tech_stack VARCHAR(255),
    FOREIGN KEY (resume_id) REFERENCES resumes(id)
);
