# SkillMatch

## Project Purpose

SkillMatch is a full-stack recruitment platform that connects candidates
with recruiters.

Candidates can create profiles, upload resumes, search for jobs,
apply for jobs, track application status, and identify missing skills
for specific job requirements.

Recruiters can create job postings, manage jobs, view applicants,
shortlist or reject candidates, and track the recruitment process.

The main goal of SkillMatch is to build a real-world recruitment
system using modern full-stack development technologies.

## Candidate Features

1. Candidate Registration
2. Candidate Login
3. Candidate Profile
4. Resume Upload
5. View Available Jobs
6. Search Jobs
7. Filter Jobs
8. View Job Details
9. Apply for Jobs
10. Save Jobs
11. View Applied Jobs
12. Track Application Status
13. View Required Skills for a Job
14. Compare Candidate Skills with Job Skills
15. View Skill Gap
16. Receive Notifications

## Recruiter Features

1. Recruiter Registration
2. Recruiter Login
3. Company Profile
4. Create Job Posting
5. Edit Job Posting
6. Delete Job Posting
7. View Posted Jobs
8. Manage Job Applications
9. View Applicants
10. View Candidate Profiles
11. Shortlist Candidates
12. Reject Candidates
13. Update Application Status
14. Schedule Interviews
15. Receive Notifications
16. View Recruitment Dashboard

## Users

### Candidate

A candidate can:
- Create an account
- Build a profile
- Upload a resume
- Search for jobs
- Apply for jobs
- Track applications
- Check skill gaps

### Recruiter

A recruiter can:
- Create an account
- Create a company profile
- Post jobs
- Manage jobs
- View applicants
- Shortlist or reject candidates
- Schedule interviews

## Initial Database Entities

### 1. User
Stores login and basic account information.

Main fields:
- user_id
- name
- email
- password
- role

Roles:
- CANDIDATE
- RECRUITER


### 2. CandidateProfile
Stores candidate-specific information.

Main fields:
- candidate_id
- user_id
- phone
- location
- education
- experience
- bio


### 3. RecruiterProfile
Stores recruiter/company information.

Main fields:
- recruiter_id
- user_id
- company_name
- company_description
- company_location


### 4. Resume
Stores candidate resume information.

Main fields:
- resume_id
- candidate_id
- file_name
- file_path
- uploaded_at


### 5. Skill
Stores skills such as Java, SQL, React, Spring Boot etc.

Main fields:
- skill_id
- skill_name


### 6. Job
Stores job posting information.

Main fields:
- job_id
- recruiter_id
- title
- description
- location
- salary
- experience_required
- created_at


### 7. JobSkill
Connects jobs with required skills.

Main fields:
- job_id
- skill_id


### 8. Application
Stores candidate job applications.

Main fields:
- application_id
- candidate_id
- job_id
- status
- applied_at


Application status:
- APPLIED
- SHORTLISTED
- REJECTED
- INTERVIEW
- SELECTED


### 9. Interview
Stores interview information.

Main fields:
- interview_id
- application_id
- interview_date
- interview_type
- status


### 10. Notification
Stores notifications for candidates and recruiters.

Main fields:
- notification_id
- user_id
- message
- is_read
- created_at