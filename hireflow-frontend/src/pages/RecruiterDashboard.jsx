import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, run, fmtDate } from '../api'
import { Avatar, Chip, Icon, JobForm, Pill, Empty, PageHead, Stat } from '../components/Bits'
import './RecruiterDashboard.css'

export default function RecruiterDashboard({ user }) {
  const [jobs, setJobs] = useState(null)
  const [apps, setApps] = useState(null)
  const [posting, setPosting] = useState(false)
  const loadJobs = () => run(() => api('/api/jobs/my-jobs')).then(setJobs)

  useEffect(() => {
    loadJobs()
    run(() => api('/api/applications/all')).then(setApps)
  }, [])

  const n = (s) => (apps ? apps.filter((a) => a.status === s).length : '–')
  const first = (user.name || '').split(' ')[0] || 'there'
  const countFor = (id) => (apps || []).filter((a) => a.jobId === id).length

  return (
    <>
      <PageHead title={`Welcome back, ${first}`} sub="Your hiring pipeline at a glance.">
        <button className="btn pri" onClick={() => setPosting(true)}><Icon n="plus" />Post a job</button>
      </PageHead>

      <div className="stats">
        <Stat icon="briefcase" n={jobs?.length ?? '–'} label="Open jobs" />
        <Stat icon="users" n={apps?.length ?? '–'} label="Total applicants" />
        <Stat icon="clock" tone="a" n={n('APPLIED')} label="Need review" />
        <Stat icon="check" tone="g" n={n('SHORTLISTED')} label="Shortlisted" />
      </div>

      <div className="dgrid">
        <section className="card">
          <div className="card-h"><h2>Latest applicants</h2><Link className="more" to="/applicants">View all →</Link></div>
          {(apps || []).slice(-5).reverse().map((a) => (
            <div key={a.applicationId} className="ra">
              <Avatar path={`/api/applications/${a.applicationId}/candidate-photo`} name={a.candidateName} />
              <div className="info"><b>{a.candidateName}</b><small>{a.jobTitle} · {fmtDate(a.appliedAt)}</small></div>
              <Pill s={a.status} rec />
            </div>
          ))}
          {apps && !apps.length && <Empty t="No applicants yet">Applications to your jobs will show up here.</Empty>}
        </section>

        <section className="card">
          <div className="card-h"><h2>Your jobs</h2><Link className="more" to="/manage-jobs">Manage →</Link></div>
          {(jobs || []).slice(0, 6).map((j) => {
            const c = countFor(j.id)
            return (
              <Link key={j.id} className="jr" to="/applicants" state={{ jobId: j.id }}>
                <span><b>{j.title}</b><small>{j.companyName}</small></span>
                <Chip t={`${c} applicant${c === 1 ? '' : 's'}`} c="p" />
              </Link>
            )
          })}
          {jobs && !jobs.length && <Empty t="No jobs posted">Post your first job to start receiving applications.</Empty>}
        </section>
      </div>
      {posting && <JobForm job={{}} onClose={() => setPosting(false)} onSaved={() => { setPosting(false); loadJobs() }} />}
    </>
  )
}
