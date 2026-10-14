import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, run, fmtDate, split } from '../api'
import { Chip, Logo, Pill, Empty, PageHead, Stat } from '../components/Bits'
import './CandidateDashboard.css'

// [profile field, label shown when it is missing]
const FIELDS = [['name', 'Name'], ['phone', 'Phone'], ['location', 'Location'], ['education', 'Education'], ['skills', 'Skills'], ['resumeUploaded', 'Resume'], ['profilePhotoUploaded', 'Photo']]

export default function CandidateDashboard({ user }) {
  const nav = useNavigate()
  const [apps, setApps] = useState(null)
  const [profile, setProfile] = useState(null)
  const [jobs, setJobs] = useState(null)

  useEffect(() => {
    run(() => api('/api/applications/my-applications')).then(setApps)
    run(() => api('/api/profile')).then(setProfile)
    run(() => api('/api/jobs')).then(setJobs)
  }, [])

  const n = (s) => (apps ? apps.filter((a) => a.status === s).length : '–')
  const first = (user.name || '').split(' ')[0] || 'there'

  // profile completion from real profile fields
  const missing = profile ? FIELDS.filter(([k]) => !profile[k]).map(([, l]) => l) : []
  const pct = profile ? Math.round(((FIELDS.length - missing.length) / FIELDS.length) * 100) : 0

  // recommended = not applied yet, ranked by overlap with profile skills
  const mine = split(profile?.skills).map((s) => s.toLowerCase())
  const applied = new Set((apps || []).map((a) => a.jobId))
  const score = (j) => split(j.requiredSkills).filter((s) => mine.includes(s.toLowerCase())).length
  const rec = (jobs || []).filter((j) => !applied.has(j.id)).sort((a, b) => score(b) - score(a)).slice(0, 3)

  const apply = async (id) => {
    if (await run(() => api('/api/applications/' + id, { method: 'POST' }), 'Application sent')) run(() => api('/api/applications/my-applications')).then(setApps)
  }

  return (
    <>
      <PageHead title={`Welcome back, ${first}`} sub="Here is where your job search stands." />

      <div className="stats">
        <Stat icon="doc" n={apps?.length ?? '–'} label="Applied" />
        <Stat icon="clock" tone="a" n={n('APPLIED')} label="In review" />
        <Stat icon="check" tone="g" n={n('SHORTLISTED')} label="Shortlisted" />
        <Stat icon="calendar" n={n('INTERVIEW')} label="Interviews" />
        <Stat icon="target" tone="g" n={n('SELECTED')} label="Selected" />
      </div>

      <div className="dgrid">
        <div className="col">
          <section className="card">
            <div className="card-h"><h2>Recommended jobs</h2><Link className="more" to="/jobs">View all →</Link></div>
            <div className="rj">
              {rec.map((j) => (
                <article key={j.id} className="rjc">
                  <div className="head"><Logo name={j.companyName} /><div><h3>{j.title}</h3><small>{j.companyName}</small></div></div>
                  <div className="meta">{[j.location, j.employmentType, j.experience].filter(Boolean).join(' · ')}</div>
                  {j.salary && <div className="pay">{j.salary}</div>}
                  <button className="btn pri sm" onClick={() => apply(j.id)}>Apply now</button>
                </article>
              ))}
            </div>
            {jobs && !rec.length && <Empty t="No new jobs right now">Check back soon — new roles appear here.</Empty>}
          </section>

          <section className="card">
            <div className="card-h"><h2>Recent applications</h2><Link className="more" to="/applications">View all →</Link></div>
            {(apps || []).slice(-4).reverse().map((a) => (
              <div key={a.id} className="ra">
                <Logo name={a.companyName} />
                <div className="info"><b>{a.title} · {a.companyName}</b><small>Applied {fmtDate(a.appliedAt)}</small></div>
                <Pill s={a.status} />
              </div>
            ))}
            {apps && !apps.length && <Empty t="No applications yet">Find a role in Explore Jobs and apply.</Empty>}
          </section>
        </div>

        <div className="col">
          <section className="card">
            <div className="card-h"><h2>Profile completion</h2><b className="pct">{pct}%</b></div>
            <div className="meter"><i style={{ width: pct + '%' }} /></div>
            {!!missing.length && (
              <div className="todo">
                <small>Still missing</small>
                <div className="row">{missing.map((m) => <Chip key={m} t={m} c="a" />)}</div>
              </div>
            )}
            <button className="btn pri full" onClick={() => nav('/profile')}>{missing.length ? 'Complete profile' : 'Edit profile'}</button>
          </section>
        </div>
      </div>
    </>
  )
}
