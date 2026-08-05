import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  ListToolsRequestSchema,
  CallToolRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import mysql from "mysql2/promise";

// ---- DB connection pool (reads the same MySQL database as the Spring Boot app) ----
const pool = mysql.createPool({
  host: process.env.DB_HOST || "localhost",
  user: process.env.DB_USER || "skillbridge",
  password: process.env.DB_PASSWORD || "skillbridge",
  database: process.env.DB_NAME || "skillbridge",
  waitForConnections: true,
  connectionLimit: 5,
});

// ---- Tool definitions ----
const TOOLS = [
  {
    name: "search_internships",
    description:
      "Search open internships on SkillBridge by keyword (matches title, company, or location).",
    inputSchema: {
      type: "object",
      properties: {
        keyword: { type: "string", description: "Search term, e.g. 'React' or 'Bengaluru'" },
      },
    },
  },
  {
    name: "get_student_applications",
    description: "Get all applications submitted by a student, identified by their email.",
    inputSchema: {
      type: "object",
      properties: {
        email: { type: "string", description: "Student's registered email address" },
      },
      required: ["email"],
    },
  },
  {
    name: "get_analytics_summary",
    description:
      "Get portal-wide analytics: total students, internships, applications, breakdown by status, and average aptitude score.",
    inputSchema: { type: "object", properties: {} },
  },
  {
    name: "recommend_internships_for_student",
    description:
      "Recommend internships for a student by matching their listed skills against each internship's required skills.",
    inputSchema: {
      type: "object",
      properties: {
        email: { type: "string", description: "Student's registered email address" },
      },
      required: ["email"],
    },
  },
  {
    name: "get_roadmap_progress",
    description:
      "Get a student's progress through all career roadmaps (e.g. Backend Developer, Frontend Developer), including percent complete and remaining steps.",
    inputSchema: {
      type: "object",
      properties: {
        email: { type: "string", description: "Student's registered email address" },
      },
      required: ["email"],
    },
  },
  {
    name: "get_upcoming_interviews",
    description: "Get a student's scheduled interviews, including internship, company, time, and mode.",
    inputSchema: {
      type: "object",
      properties: {
        email: { type: "string", description: "Student's registered email address" },
      },
      required: ["email"],
    },
  },
  {
    name: "get_placement_drives",
    description: "List active and upcoming placement drives, including how many internships are linked to each.",
    inputSchema: { type: "object", properties: {} },
  },
  {
    name: "get_resume_score",
    description: "Get a student's resume completeness score (0-100) and a breakdown of what's filled in.",
    inputSchema: {
      type: "object",
      properties: {
        email: { type: "string", description: "Student's registered email address" },
      },
      required: ["email"],
    },
  },
  {
    name: "get_student_progress",
    description:
      "Get a student's overall career-readiness snapshot: resume score, average aptitude test score, average roadmap completion, and a weighted composite readiness score (0-100).",
    inputSchema: {
      type: "object",
      properties: {
        email: { type: "string", description: "Student's registered email address" },
      },
      required: ["email"],
    },
  },
];

const server = new Server(
  { name: "skillbridge-mcp-server", version: "1.0.0" },
  { capabilities: { tools: {} } }
);

server.setRequestHandler(ListToolsRequestSchema, async () => ({ tools: TOOLS }));

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    switch (name) {
      case "search_internships": {
        const keyword = args?.keyword ?? "";
        const [rows] = await pool.query(
          `SELECT title, company, location, stipend, duration, required_skills
           FROM internships
           WHERE title LIKE ? OR company LIKE ? OR location LIKE ?
           LIMIT 20`,
          [`%${keyword}%`, `%${keyword}%`, `%${keyword}%`]
        );
        return textResult(rows);
      }

      case "get_student_applications": {
        const [rows] = await pool.query(
          `SELECT i.title, i.company, a.status, a.applied_at
           FROM applications a
           JOIN users u ON a.student_id = u.id
           JOIN internships i ON a.internship_id = i.id
           WHERE u.email = ?`,
          [args.email]
        );
        return textResult(rows);
      }

      case "get_analytics_summary": {
        const [[{ totalStudents }]] = await pool.query(
          `SELECT COUNT(*) AS totalStudents FROM users WHERE role = 'STUDENT'`
        );
        const [[{ totalInternships }]] = await pool.query(
          `SELECT COUNT(*) AS totalInternships FROM internships`
        );
        const [[{ totalApplications }]] = await pool.query(
          `SELECT COUNT(*) AS totalApplications FROM applications`
        );
        const [statusRows] = await pool.query(
          `SELECT status, COUNT(*) AS count FROM applications GROUP BY status`
        );
        const [[{ avgScore }]] = await pool.query(
          `SELECT COALESCE(AVG(score / total_questions * 100), 0) AS avgScore FROM test_results`
        );

        return textResult({
          totalStudents,
          totalInternships,
          totalApplications,
          applicationsByStatus: Object.fromEntries(statusRows.map(r => [r.status, r.count])),
          averageAptitudeScorePercent: Math.round(avgScore * 100) / 100,
        });
      }

      case "recommend_internships_for_student": {
        const [[student]] = await pool.query(
          `SELECT skills FROM users WHERE email = ?`,
          [args.email]
        );
        if (!student) return textResult({ error: "Student not found" });

        const studentSkills = (student.skills || "")
          .split(",")
          .map(s => s.trim().toLowerCase())
          .filter(Boolean);

        const [internships] = await pool.query(
          `SELECT title, company, required_skills FROM internships`
        );

        const ranked = internships
          .map(i => {
            const reqSkills = (i.required_skills || "")
              .split(",")
              .map(s => s.trim().toLowerCase())
              .filter(Boolean);
            const overlap = reqSkills.filter(s => studentSkills.includes(s));
            return { title: i.title, company: i.company, matchScore: overlap.length, matchedSkills: overlap };
          })
          .filter(r => r.matchScore > 0)
          .sort((a, b) => b.matchScore - a.matchScore);

        return textResult(ranked);
      }

      case "get_roadmap_progress": {
        const [[student]] = await pool.query(`SELECT id FROM users WHERE email = ?`, [args.email]);
        if (!student) return textResult({ error: "Student not found" });

        const [roadmaps] = await pool.query(`SELECT id, title, track FROM roadmaps`);
        const results = [];
        for (const r of roadmaps) {
          const [steps] = await pool.query(
            `SELECT id, title FROM roadmap_steps WHERE roadmap_id = ? ORDER BY order_index`,
            [r.id]
          );
          const [progressRows] = await pool.query(
            `SELECT rs.id AS step_id, rp.completed
             FROM roadmap_steps rs
             LEFT JOIN roadmap_progress rp ON rp.step_id = rs.id AND rp.student_id = ?
             WHERE rs.roadmap_id = ?`,
            [student.id, r.id]
          );
          const completedIds = new Set(
            progressRows.filter(p => p.completed === 1).map(p => p.step_id)
          );
          const total = steps.length;
          const done = completedIds.size;
          results.push({
            roadmap: r.title,
            track: r.track,
            percentComplete: total === 0 ? 0 : Math.round((done / total) * 100),
            remainingSteps: steps.filter(s => !completedIds.has(s.id)).map(s => s.title),
          });
        }
        return textResult(results);
      }

      case "get_upcoming_interviews": {
        const [rows] = await pool.query(
          `SELECT i.title, i.company, iv.scheduled_at, iv.mode, iv.notes
           FROM interviews iv
           JOIN users u ON iv.student_id = u.id
           JOIN applications a ON iv.application_id = a.id
           JOIN internships i ON a.internship_id = i.id
           WHERE u.email = ?
           ORDER BY iv.scheduled_at ASC`,
          [args.email]
        );
        return textResult(rows);
      }

      case "get_placement_drives": {
        const [rows] = await pool.query(
          `SELECT d.name, d.company, d.drive_date, d.is_active, COUNT(i.id) AS linked_internships
           FROM placement_drives d
           LEFT JOIN internships i ON i.drive_id = d.id
           GROUP BY d.id
           ORDER BY d.drive_date ASC`
        );
        return textResult(rows);
      }

      case "get_resume_score": {
        const [[resume]] = await pool.query(
          `SELECT r.score, r.headline, r.summary, r.github, r.linkedin, r.portfolio
           FROM resumes r
           JOIN users u ON r.user_id = u.id
           WHERE u.email = ?`,
          [args.email]
        );
        if (!resume) return textResult({ error: "No resume found for this student" });

        const [[eduCount]] = await pool.query(
          `SELECT COUNT(*) AS c FROM resume_education re JOIN resumes r ON re.resume_id = r.id JOIN users u ON r.user_id = u.id WHERE u.email = ?`,
          [args.email]
        );
        const [[expCount]] = await pool.query(
          `SELECT COUNT(*) AS c FROM resume_experience re JOIN resumes r ON re.resume_id = r.id JOIN users u ON r.user_id = u.id WHERE u.email = ?`,
          [args.email]
        );
        const [[projCount]] = await pool.query(
          `SELECT COUNT(*) AS c FROM resume_projects rp JOIN resumes r ON rp.resume_id = r.id JOIN users u ON r.user_id = u.id WHERE u.email = ?`,
          [args.email]
        );

        return textResult({
          score: resume.score,
          hasHeadline: !!resume.headline,
          hasSummary: !!resume.summary,
          hasLinks: !!(resume.github || resume.linkedin || resume.portfolio),
          educationEntries: eduCount.c,
          experienceEntries: expCount.c,
          projectEntries: projCount.c,
        });
      }

      case "get_student_progress": {
        const [[student]] = await pool.query(`SELECT id, skills FROM users WHERE email = ?`, [args.email]);
        if (!student) return textResult({ error: "Student not found" });

        const [[resumeRow]] = await pool.query(
          `SELECT COALESCE(score, 0) AS score FROM resumes WHERE user_id = ?`,
          [student.id]
        );
        const resumeScore = resumeRow ? resumeRow.score : 0;

        const [[aptitudeRow]] = await pool.query(
          `SELECT COALESCE(AVG(score / total_questions * 100), 0) AS avgScore
           FROM test_results WHERE student_id = ?`,
          [student.id]
        );
        const aptitudeAvg = Math.round(aptitudeRow.avgScore * 100) / 100;

        const [roadmaps] = await pool.query(`SELECT id FROM roadmaps`);
        let roadmapPercents = [];
        for (const r of roadmaps) {
          const [[stepCountRow]] = await pool.query(
            `SELECT COUNT(*) AS c FROM roadmap_steps WHERE roadmap_id = ?`,
            [r.id]
          );
          if (stepCountRow.c === 0) continue;
          const [[doneCountRow]] = await pool.query(
            `SELECT COUNT(*) AS c FROM roadmap_progress rp
             JOIN roadmap_steps rs ON rp.step_id = rs.id
             WHERE rs.roadmap_id = ? AND rp.student_id = ? AND rp.completed = 1`,
            [r.id, student.id]
          );
          roadmapPercents.push((doneCountRow.c / stepCountRow.c) * 100);
        }
        const roadmapOverall = roadmapPercents.length === 0
          ? 0
          : Math.round(roadmapPercents.reduce((a, b) => a + b, 0) / roadmapPercents.length);

        const readinessScore = Math.round(resumeScore * 0.4 + aptitudeAvg * 0.3 + roadmapOverall * 0.3);

        return textResult({
          resumeScore,
          averageAptitudeScorePercent: aptitudeAvg,
          averageRoadmapCompletionPercent: roadmapOverall,
          readinessScore,
        });
      }

      default:
        return { content: [{ type: "text", text: `Unknown tool: ${name}` }], isError: true };
    }
  } catch (err) {
    return { content: [{ type: "text", text: `Error: ${err.message}` }], isError: true };
  }
});

function textResult(data) {
  return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
}

const transport = new StdioServerTransport();
await server.connect(transport);
