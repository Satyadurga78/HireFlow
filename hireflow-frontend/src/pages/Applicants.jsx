import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { api, run, fmtDate, download } from '../api'
import { ApplicantModal, Avatar, Empty, Icon, InterviewForm, MatchModal, PageHead, Pill } from '../components/Bits'
import './MyApplications.css'
import './Applicants.css'

// Recruiters decide on candidates; there is no "Applied" tab on this side.
const TABS = [['ALL', 'All'], ['SHORTLISTED', 'Shortlisted'], ['INTERVIEW', 'Interview'], ['SELECTED', 'Selected'], ['REJECTED', 'Rejected']]
const OPTIONS = [['SHORTLISTED', 'Shortlisted'], ['INTERVIEW', 'Interview'], ['SELECTED', 'Selected'], ['REJECTED', 'Rejected']]

export default function Applicants() {
  const loc = useLocation()
  const q = (loc.state?.q || '').toLowerCase() // typed in the search bar at the top
  const [all, setAll] = useState(null)
  const [jobs, setJobs] = useState([])
  const [jobId, setJobId] = useState(loc.state?.jobId ? String(loc.state.jobId) : '')
  const [tab, setTab] = useState('ALL')
  const [match, setMatch] = useState(null)
  const [profile, setProfile] = useState(null)
  const [schedule, setSchedule] = useState(null)

  useEffect(() => {
    run(() => api('/api/applications/all')).then(setAll)
    run(() => api('/api/jobs/my-jobs')).then((j) => setJobs(j || []))
  }, [])

  const inJob = (all || [])
    .filter((a) => !jobId || String(a.jobId) === jobId)
    .filter((a) => [a.candidateName, a.candidateEmail, a.jobTitle].join(' ').toLowerCase().includes(q))
  const shown = inJob.filter((a) => tab === 'ALL' || a.status === tab)

  const patch = (id, status) => setAll((l) => l.map((x) => (x.applicationId === id ? { ...x, status } : x)))
  const setStatus = async (a, status) => {
    if (status === 'INTERVIEW') return setSchedule(a) // interview needs a date first
    const label = OPTIONS.find(([s]) => s === status)[1]
    if (await run(() => api(`/api/applications/${a.applicationId}/status`, { method: 'PUT', body: { status } }), 'Marked as ' + label)) patch(a.applicationId, status)
  }
  const skill = async (a) => {
    const d = await run(() => api(`/api/applications/${a.applicationId}/skillmatch`, { fallback: 'This candidate has not uploaded a resume.' }))
    if (d) setMatch(d)
  }
  const resume = (a) => run(() => download(`/api/applications/${a.applicationId}/resume`, 'candidate-resume.pdf'))

  return (
    <>
      <PageHead title="Applicants" sub="Review, match and decide.">
        <select className="ap-filter" value={jobId} onChange={(e) => setJobId(e.target.value)}>
          <option value="">All jobs</option>
          {jobs.map((j) => <option key={j.id} value={j.id}>{j.title}</option>)}
        </select>
      </PageHead>
      <div className="tabs">
        {TABS.map(([t, l]) => (
          <button key={t} className={t === tab ? 'on' : ''} onClick={() => setTab(t)}>
            {l}<em>{inJob.filter((a) => t === 'ALL' || a.status === t).length}</em>
          </button>
        ))}
      </div>
      <div className="list">
        {shown.map((a) => (
          <div key={a.applicationId} className="card app">
            <Avatar path={`/api/applications/${a.applicationId}/candidate-photo`} name={a.candidateName} />
            <div className="info">
              <h3>{a.candidateName}</h3>
              <small>{a.candidateEmail}<br />{a.jobTitle} · {fmtDate(a.appliedAt)}</small>
            </div>
            <Pill s={a.status} rec />
            <div className="row">
              <button className="btn sm" onClick={() => setProfile(a)}><Icon n="user" />Profile</button>
              <button className="btn sm" onClick={() => resume(a)}><Icon n="doc" />Resume</button>
              <button className="btn sm" onClick={() => skill(a)}><Icon n="target" />Skill match</button>
              {(a.status === 'SHORTLISTED' || a.status === 'INTERVIEW') && <button className="btn sm" onClick={() => setSchedule(a)}><Icon n="calendar" />{a.status === 'INTERVIEW' ? 'Reschedule' : 'Schedule'}</button>}
              <select className="st-select" aria-label="Update status" value={a.status} onChange={(e) => setStatus(a, e.target.value)}>
                {a.status === 'APPLIED' && <option value="APPLIED" disabled>Update status…</option>}
                {OPTIONS.map(([s, l]) => <option key={s} value={s}>{l}</option>)}
              </select>
            </div>
          </div>
        ))}
      </div>
      {all && !shown.length && <Empty t="No applicants here">Applications to your jobs will appear in this list.</Empty>}
      {match && <MatchModal d={match} onClose={() => setMatch(null)} />}
      {profile && <ApplicantModal a={profile} onClose={() => setProfile(null)} onResume={() => resume(profile)} />}
      {schedule && <InterviewForm app={schedule} onClose={() => setSchedule(null)} onSaved={() => { patch(schedule.applicationId, 'INTERVIEW'); setSchedule(null) }} />}
    </>
  )
}
