import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { api, run, fmtDate } from '../api'
import { Empty, Icon, Logo, MatchModal, PageHead, Pill, statusLabel } from '../components/Bits'
import './MyApplications.css'
import './Applicants.css'

const TABS = [['ALL', 'All'], ['APPLIED', 'In review'], ['SHORTLISTED', 'Shortlisted'], ['INTERVIEW', 'Interview'], ['SELECTED', 'Selected'], ['REJECTED', 'Rejected']]

export default function MyApplications() {
  const loc = useLocation()
  const text = loc.state?.q || '' // typed in the search bar at the top
  const [apps, setApps] = useState(null)
  const [tab, setTab] = useState('ALL')
  const [match, setMatch] = useState(null)
  const load = () => run(() => api('/api/applications/my-applications')).then(setApps)
  useEffect(() => { load() }, [])

  const withdraw = async (id) => {
    if (confirm('Withdraw this application?') && await run(() => api('/api/applications/' + id, { method: 'DELETE' }), 'Application withdrawn')) load()
  }
  const check = async (jobId) => {
    const d = await run(() => api('/api/skillmatch/match/' + jobId, { method: 'POST', fallback: 'Upload your resume (PDF) in Profile first.' }))
    if (d) setMatch(d)
  }

  const q = text.toLowerCase()
  const searched = (apps || []).filter((a) => [a.title, a.companyName, a.location, a.jobType, statusLabel(a.status)].join(' ').toLowerCase().includes(q))
  const list = searched.filter((a) => tab === 'ALL' || a.status === tab)

  return (
    <>
      <PageHead title="My Applications" sub={apps && text ? `${searched.length} result${searched.length === 1 ? '' : 's'} for “${text}”` : 'Track where each application stands.'} />
      <div className="tabs">
        {TABS.map(([t, l]) => (
          <button key={t} className={t === tab ? 'on' : ''} onClick={() => setTab(t)}>
            {l}<em>{searched.filter((a) => t === 'ALL' || a.status === t).length}</em>
          </button>
        ))}
      </div>
      <div className="list">
        {list.map((a) => (
          <div key={a.id} className="card app">
            <Logo name={a.companyName} />
            <div className="info">
              <h3>{a.title}</h3><span className="co">{a.companyName}</span>
              <small>{[a.location, a.jobType, 'Applied ' + fmtDate(a.appliedAt)].filter(Boolean).join(' · ')}</small>
            </div>
            <Pill s={a.status} />
            <div className="row">
              <button className="btn sm" onClick={() => check(a.jobId)}><Icon n="target" />Match</button>
              <button className="btn sm bad" onClick={() => withdraw(a.id)}><Icon n="trash" />Withdraw</button>
            </div>
          </div>
        ))}
      </div>
      {apps && !apps.length && <Empty t="No applications yet">Find a role in Explore Jobs and apply.</Empty>}
      {apps && !!apps.length && !list.length && <Empty t="No matching applications">Try a different tab or search.</Empty>}
      {match && <MatchModal d={match} onClose={() => setMatch(null)} />}
    </>
  )
}
