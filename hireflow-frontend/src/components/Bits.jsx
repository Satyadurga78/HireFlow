import { useEffect, useState } from 'react'
import { api, ini, run, split, toast, fmtDate, fmtDateTime } from '../api'

const P = {
  dash: 'M3 3h7v9H3zM14 3h7v5h-7zM14 12h7v9h-7zM3 16h7v5H3z',
  briefcase: 'M3 7h18v13H3zM8 7V4h8v3M3 13h18',
  doc: 'M6 2h9l5 5v15H6zM14 2v6h6M9 13h8M9 17h8',
  users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM22 21v-2a4 4 0 0 0-3-3.9M16 3.1a4 4 0 0 1 0 7.8',
  user: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
  search: 'M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM21 21l-4.3-4.3',
  logout: 'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9',
  upload: 'M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12',
  edit: 'M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z',
  plus: 'M12 5v14M5 12h14',
  check: 'M20 6 9 17l-5-5',
  x: 'M18 6 6 18M6 6l12 12',
  clock: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zM12 6v6l4 2',
  target: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zM12 18a6 6 0 1 0 0-12 6 6 0 0 0 0 12zM12 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4z',
  trash: 'M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6',
  bookmark: 'M6 3h12v18l-6-4-6 4z',
  bell: 'M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9M13.7 21a2 2 0 0 1-3.4 0',
  calendar: 'M3 5h18v16H3zM3 10h18M8 3v4M16 3v4',
}
export const Icon = ({ n }) => (
  <svg className="ic" viewBox="0 0 24 24" aria-hidden="true"><path d={P[n]} /></svg>
)

const COLORS = ['#4338ca', '#0e7490', '#be185d', '#b45309', '#047857', '#6d28d9', '#1d4ed8']
export const Logo = ({ name }) => {
  const c = COLORS[[...(name || 'x')].reduce((a, ch) => a + ch.charCodeAt(0), 0) % COLORS.length]
  return <div className="logo-tile" style={{ background: c }}>{(name || '?')[0].toUpperCase()}</div>
}

export const Chip = ({ t, c = '' }) => (t ? <span className={'chip ' + c}>{t}</span> : null)

// Recruiter side never shows "Applied" - a new application is simply "New".
const ST = { APPLIED: ['Applied', 'a'], SHORTLISTED: ['Shortlisted', 'g'], INTERVIEW: ['Interview', 'p'], SELECTED: ['Selected', 'g'], REJECTED: ['Rejected', 'r'] }
const ST_REC = { APPLIED: ['New', 'a'], SHORTLISTED: ['Shortlisted', 'g'], INTERVIEW: ['Interview', 'p'], SELECTED: ['Selected', 'g'], REJECTED: ['Rejected', 'r'] }
export const Pill = ({ s, rec }) => {
  const [l, c] = (rec ? ST_REC : ST)[s] || [s, '']
  return <Chip t={l} c={c} />
}
export const statusLabel = (s) => (ST[s] || [s])[0]

export const Empty = ({ t, children }) => (
  <div className="empty"><b>{t}</b>{children}</div>
)

export const PageHead = ({ title, sub, children }) => (
  <div className="bar"><div><h1>{title}</h1><p className="sub">{sub}</p></div>{children}</div>
)

// Loads an image from a protected endpoint (needs the JWT header)
export function Avatar({ path, name, cls = '', v }) {
  const [src, setSrc] = useState(null)
  useEffect(() => {
    let url
    setSrc(null)
    api(path, { blob: 1 }).then((b) => { url = URL.createObjectURL(b); setSrc(url) }).catch(() => {})
    return () => url && URL.revokeObjectURL(url)
  }, [path, v])
  return <div className={'av ' + cls}>{src ? <img src={src} alt="" /> : ini(name)}</div>
}

export function Modal({ onClose, children }) {
  return (
    <div className="ov" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="md" role="dialog" aria-modal="true">{children}</div>
    </div>
  )
}

function Ring({ p }) {
  const c = 2 * Math.PI * 44
  const col = p >= 70 ? 'var(--ok)' : p >= 40 ? 'var(--warn)' : 'var(--bad)'
  return (
    <div className="ring">
      <svg width="120" height="120" viewBox="0 0 100 100">
        <circle cx="50" cy="50" r="44" fill="none" stroke="var(--line)" strokeWidth="9" />
        <circle cx="50" cy="50" r="44" fill="none" stroke={col} strokeWidth="9" strokeLinecap="round" strokeDasharray={`${(c * p) / 100} ${c}`} />
      </svg>
      <b>{Math.round(p)}%</b>
    </div>
  )
}

// Response of /api/skillmatch/match/{id} or /api/applications/{id}/skillmatch
export function MatchModal({ d, onClose }) {
  const none = !(d.requiredSkills || []).length
  const list = (a, c) => (a || []).length ? a.map((s) => <Chip key={s} t={s} c={c} />) : <small>None</small>
  return (
    <Modal onClose={onClose}>
      <h2>{d.candidateName} · {d.jobTitle}</h2>
      <p className="sub">Skills found in the resume compared with what the job requires.</p>
      {none ? <div className="empty">{d.message || 'This job lists no required skills.'}</div> : <Ring p={d.matchPercentage || 0} />}
      <label>Matched</label><div className="row">{list(d.matchedSkills, 'g')}</div>
      <label>Missing</label><div className="row">{list(d.missingSkills, 'r')}</div>
      <label>All skills detected in resume</label><div className="row">{list(d.candidateSkills)}</div>
      <div className="row md-actions"><button className="btn pri push" onClick={onClose}>Close</button></div>
    </Modal>
  )
}

export const Brand = ({ size = 21 }) => (
  <div className="logo" style={{ fontSize: size }}>
    <span className="mark"><Icon n="briefcase" /></span><span>Hire<i>Flow</i></span>
  </div>
)

// Stat tile used by both dashboards. tone: '' (brand) | 'a' (amber) | 'g' (green) | 'r' (red)
export const Stat = ({ icon, n, label, tone = '' }) => (
  <div className={'card stat ' + tone}>
    <span className="qi"><Icon n={icon} /></span>
    <div><b>{n}</b><span>{label}</span></div>
  </div>
)

// One job card for Explore Jobs and Manage Jobs; the action buttons come in as children.
export function JobCard({ j, onOpen, children }) {
  const skills = split(j.requiredSkills)
  const meta = [j.location, j.employmentType, j.experience].filter(Boolean).join(' · ')
  return (
    <article className="card job">
      <div className="head">
        <Logo name={j.companyName} />
        <div><h3>{onOpen ? <button className="job-title" onClick={onOpen}>{j.title}</button> : j.title}</h3><span className="co">{j.companyName}</span></div>
      </div>
      <div>
        {meta && <div className="meta">{meta}</div>}
        {j.salary && <div className="pay">{j.salary}</div>}
      </div>
      {j.description && <p>{j.description}</p>}
      {!!skills.length && (
        <div className="row">
          {skills.slice(0, 4).map((s) => <Chip key={s} t={s} />)}
          {skills.length > 4 && <Chip t={'+' + (skills.length - 4)} />}
        </div>
      )}
      <div className="foot">{children}</div>
    </article>
  )
}

// Post / edit job modal, shared by Manage Jobs and the recruiter Dashboard
export const TYPES = ['Full-time', 'Part-time', 'Internship', 'Contract']

export function JobForm({ job, onClose, onSaved }) {
  const save = async (e) => {
    e.preventDefault()
    const body = Object.fromEntries(new FormData(e.target))
    // trim every field; reject values that are only spaces
    for (const k of Object.keys(body)) body[k] = body[k].trim()
    if (Object.values(body).some((v) => !v)) return toast('Please fill in all the fields.', 1)
    const r = await run(() => api(job.id ? '/api/jobs/' + job.id : '/api/jobs', { method: job.id ? 'PUT' : 'POST', body }), job.id ? 'Job updated' : 'Job published')
    if (r) onSaved()
  }
  return (
    <Modal onClose={onClose}>
      <h2>{job.id ? 'Edit job' : 'Post a job'}</h2>
      <form onSubmit={save}>
        <label>Job title</label><input name="title" required defaultValue={job.title || ''} />
        <div className="two">
          <div><label>Company</label><input name="companyName" required defaultValue={job.companyName || ''} /></div>
          <div><label>Location</label><input name="location" required defaultValue={job.location || ''} /></div>
        </div>
        <div className="two">
          <div><label>Employment type</label>
            <select name="employmentType" required defaultValue={job.employmentType || TYPES[0]}>{TYPES.map((t) => <option key={t}>{t}</option>)}</select></div>
          <div><label>Experience</label><input name="experience" required placeholder="e.g. 0-2 years" defaultValue={job.experience || ''} /></div>
        </div>
        <label>Salary</label><input name="salary" required placeholder="e.g. 6-9 LPA" defaultValue={job.salary || ''} />
        <label>Required skills (comma separated)</label><input name="requiredSkills" required placeholder="Java, Spring Boot, MySQL" defaultValue={job.requiredSkills || ''} />
        <label>Description</label><textarea name="description" rows={4} required defaultValue={job.description || ''} />
        <div className="row md-actions">
          <button type="button" className="btn push" onClick={onClose}>Cancel</button>
          <button className="btn pri">{job.id ? 'Save changes' : 'Publish job'}</button>
        </div>
      </form>
    </Modal>
  )
}


// Full job details (title, every field, all required skills) with the main actions
export function JobDetails({ j, applied, saved, onApply, onSave, onMatch, onClose }) {
  const skills = split(j.requiredSkills)
  return (
    <Modal onClose={onClose}>
      <div className="dt-head"><Logo name={j.companyName} /><div><h2>{j.title}</h2><span className="co">{j.companyName}</span></div></div>
      <div className="row">
        <Chip t={j.location} /><Chip t={j.employmentType} c="p" /><Chip t={j.experience} /><Chip t={j.salary} c="g" />
      </div>
      {j.description && <><label>About the role</label><p className="dt-desc">{j.description}</p></>}
      <label>Required skills</label>
      <div className="row">{skills.length ? skills.map((x) => <Chip key={x} t={x} />) : <small>None listed</small>}</div>
      <div className="row md-actions">
        <button className="btn" onClick={onClose}>Close</button>
        <button className="btn push" onClick={onMatch}><Icon n="target" />Check match</button>
        <button className={'btn' + (saved ? ' saved' : '')} onClick={onSave}><Icon n="bookmark" />{saved ? 'Saved' : 'Save'}</button>
        <button className="btn pri" disabled={applied} onClick={onApply}>{applied ? 'Applied ✓' : 'Apply now'}</button>
      </div>
    </Modal>
  )
}

// Recruiter: schedule an interview for an application (also moves it to the Interview stage)
export function InterviewForm({ app, onClose, onSaved }) {
  const save = async (e) => {
    e.preventDefault()
    const f = Object.fromEntries(new FormData(e.target))
    try {
      await api('/api/interviews', { method: 'POST', body: { applicationId: app.applicationId, interviewDate: f.interviewDate, interviewType: f.interviewType }, fallback: 'Could not schedule the interview.' })
    } catch (x) { return toast(x.message, 1) }
    if (app.status !== 'INTERVIEW') await run(() => api(`/api/applications/${app.applicationId}/status`, { method: 'PUT', body: { status: 'INTERVIEW' } }))
    toast('Interview scheduled')
    onSaved()
  }
  return (
    <Modal onClose={onClose}>
      <h2>Schedule interview</h2>
      <p className="sub">{app.candidateName} · {app.jobTitle}</p>
      <form onSubmit={save}>
        <label>Date and time</label><input type="datetime-local" name="interviewDate" required />
        <label>Interview type</label>
        <select name="interviewType" required defaultValue="Online">{['Online', 'In-person', 'Phone'].map((t) => <option key={t}>{t}</option>)}</select>
        <div className="row md-actions">
          <button type="button" className="btn push" onClick={onClose}>Cancel</button>
          <button className="btn pri">Schedule</button>
        </div>
      </form>
    </Modal>
  )
}

// Recruiter: candidate profile for one application. Extra fields show whenever the backend sends them.
export function ApplicantModal({ a, onClose, onResume }) {
  const rows = [
    ['Email', a.candidateEmail], ['Phone', a.candidatePhone], ['Location', a.candidateLocation],
    ['Education', a.candidateEducation], ['Experience', a.candidateExperience], ['Skills', a.candidateSkills],
    ['Bio', a.candidateBio], ['Applied for', a.jobTitle], ['Applied on', fmtDate(a.appliedAt)],
  ].filter(([, v]) => v)
  return (
    <Modal onClose={onClose}>
      <div className="dt-head">
        <Avatar cls="lg" path={`/api/applications/${a.applicationId}/candidate-photo`} name={a.candidateName} />
        <div><h2>{a.candidateName}</h2><Pill s={a.status} rec /></div>
      </div>
      <dl className="kv">{rows.map(([k, v]) => <div key={k}><dt>{k}</dt><dd>{v}</dd></div>)}</dl>
      <div className="row md-actions">
        <button className="btn push" onClick={onClose}>Close</button>
        <button className="btn pri" onClick={onResume}><Icon n="doc" />Resume</button>
      </div>
    </Modal>
  )
}
