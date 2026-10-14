# HireFlow Frontend (React + Vite)

1. Start the Spring Boot backend on http://localhost:8080 (MySQL running).
2. `npm install`
3. `npm run dev`  -> http://localhost:5173

The backend CORS allows only http://localhost:5173, so keep that port.
To point at another backend, copy `.env.example` to `.env` and edit VITE_API_URL.

Roles: Candidate (Dashboard, Explore Jobs, My Applications, My Profile)
       Recruiter (Dashboard, Manage Jobs, Applicants, My Profile)

Theme: dark violet (navy-purple surfaces, violet brand) (`--brand-*`) with separate status colours (`--ok`, `--warn`, `--bad`).
All colour tokens live at the top of `src/index.css` — change them there to re-theme the whole app.

## Feature coverage (SkillMatch spec)
Candidate: register / login / profile (+experience, bio) / resume upload / explore + search + filter (type, location) /
job details / apply / save jobs (Saved Jobs page) / applied jobs with status tabs (Applied, Shortlisted, Interview, Selected, Rejected) /
required skills, skill match and skill gap / notifications.
Recruiter: register / login / company profile / create, edit, delete, list jobs / applicants with status tabs /
candidate profile modal / shortlist, reject, interview, select via the status dropdown / schedule interviews / notifications / dashboard.

## Backend endpoints the new features rely on
Already used by the original app: auth, users, profile, jobs, applications, skillmatch.
New (adjust in the pages if your Spring Boot paths differ):
- `POST /api/interviews` body `{ applicationId, interviewDate, interviewType }` — schedule interview
- `GET  /api/interviews` — candidate's interviews (optional; notifications still work without it)
- `PUT  /api/applications/{id}/status` must accept `INTERVIEW` and `SELECTED`
- `PUT  /api/profile` may receive `experience, bio` (candidate) and `companyName, companyLocation, companyDescription` (recruiter)
Saved jobs and notification read-state are stored in the browser (localStorage), per account.
