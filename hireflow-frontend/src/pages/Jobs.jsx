import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { api, run, getUser, getSaved, toggleSaved } from '../api'
import { Empty, Icon, JobCard, JobDetails, MatchModal, PageHead, TYPES } from '../components/Bits'
import './Jobs.css'

// Used for both "Explore Jobs" and "Saved Jobs" (saved = true)
export default function Jobs({ saved = false }) {
  const loc = useLocation()
  const me = getUser()?.email
  const text = loc.state?.q || '' // typed in the search bar at the top
  const q = text.toLowerCase()
  const [jobs, setJobs] = useState(null)
  const [applied, setApplied] = useState(new Set())
  const [savedIds, setSavedIds] = useState(() => getSaved(me))
  const [type, setType] = useState('')
  const [place, setPlace] = useState('')
  const [match, setMatch] = useState(null)
  const [open, setOpen] = useState(null)

  useEffect(() => {
    run(() => api('/api/jobs')).then(setJobs)
    run(() => api('/api/applications/my-applications')).then((l) => setApplied(new Set((l || []).map((a) => a.jobId))))
  }, [])

  const apply = async (id) => {
    if (await run(() => api('/api/applications/' + id, { method: 'POST' }), 'Application sent')) setApplied((s) => new Set(s).add(id))
  }
  const check = async (id) => {
    const d = await run(() => api('/api/skillmatch/match/' + id, { method: 'POST', fallback: 'Upload your resume (PDF) in Profile first.' }))
    if (d) setMatch(d)
  }
  const toggle = (id) => setSavedIds(toggleSaved(me, id))

  const places = [...new Set((jobs || []).map((j) => j.location).filter(Boolean))].sort()
  const list = (jobs || [])
    .filter((j) => !saved || savedIds.includes(j.id))
    .filter((j) => [j.title, j.companyName, j.requiredSkills, j.location].join(' ').toLowerCase().includes(q))
    .filter((j) => !type || j.employmentType === type)
    .filter((j) => !place || j.location === place)
  const filtered = !!(text || type || place)

  return (
    <>
      <PageHead
        title={saved ? 'Saved Jobs' : 'Explore Jobs'}
        sub={jobs && filtered ? `${list.length} result${list.length === 1 ? '' : 's'}` : saved ? 'Jobs you bookmarked to look at later.' : 'Open roles from every recruiter.'}
      />
      <div className="filters">
        <select value={type} onChange={(e) => setType(e.target.value)} aria-label="Employment type">
          <option value="">All job types</option>
          {TYPES.map((t) => <option key={t}>{t}</option>)}
        </select>
        <select value={place} onChange={(e) => setPlace(e.target.value)} aria-label="Location">
          <option value="">All locations</option>
          {places.map((p) => <option key={p}>{p}</option>)}
        </select>
        {(type || place) && <button className="btn" onClick={() => { setType(''); setPlace('') }}><Icon n="x" />Clear filters</button>}
      </div>
      <div className="grid">
        {list.map((j) => (
          <JobCard key={j.id} j={j} onOpen={() => setOpen(j)}>
            <button className="btn sm" onClick={() => check(j.id)}><Icon n="target" />Check match</button>
            <button className={'btn sm' + (savedIds.includes(j.id) ? ' saved' : '')} onClick={() => toggle(j.id)} aria-pressed={savedIds.includes(j.id)}><Icon n="bookmark" />{savedIds.includes(j.id) ? 'Saved' : 'Save'}</button>
            <button className="btn sm pri push" disabled={applied.has(j.id)} onClick={() => apply(j.id)}>{applied.has(j.id) ? 'Applied ✓' : 'Apply now'}</button>
          </JobCard>
        ))}
      </div>
      {jobs && !list.length && (saved && !filtered
        ? <Empty t="No saved jobs yet">Tap Save on any job in Explore Jobs to keep it here.</Empty>
        : <Empty t="No jobs found">Try a different search or clear the filters.</Empty>)}
      {open && (
        <JobDetails j={open} applied={applied.has(open.id)} saved={savedIds.includes(open.id)}
          onApply={() => apply(open.id)} onSave={() => toggle(open.id)} onMatch={() => check(open.id)} onClose={() => setOpen(null)} />
      )}
      {match && <MatchModal d={match} onClose={() => setMatch(null)} />}
    </>
  )
}
