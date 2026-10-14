import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { api, run } from '../api'
import { Empty, Icon, JobCard, JobForm, PageHead } from '../components/Bits'
import './Jobs.css'
import './ManageJobs.css'

export default function ManageJobs() {
  const nav = useNavigate()
  const loc = useLocation()
  const text = loc.state?.q || '' // typed in the search bar at the top
  const [jobs, setJobs] = useState(null)
  const [edit, setEdit] = useState(null) // null | {} (new) | job
  const load = () => run(() => api('/api/jobs/my-jobs')).then(setJobs)
  useEffect(() => { load() }, [])

  const del = async (id) => {
    if (confirm('Delete this job?') && await run(() => api('/api/jobs/' + id, { method: 'DELETE' }), 'Job deleted')) load()
  }

  const q = text.toLowerCase()
  const list = (jobs || []).filter((j) => [j.title, j.companyName, j.requiredSkills, j.location].join(' ').toLowerCase().includes(q))

  return (
    <>
      <PageHead title="Manage Jobs" sub={jobs && text ? `${list.length} result${list.length === 1 ? '' : 's'} for “${text}”` : 'Roles you have posted.'}>
        <button className="btn pri" onClick={() => setEdit({})}><Icon n="plus" />Post a job</button>
      </PageHead>
      <div className="grid">
        {list.map((j) => (
          <JobCard key={j.id} j={j}>
            <button className="btn sm pri" onClick={() => nav('/applicants', { state: { jobId: j.id } })}><Icon n="users" />Applicants</button>
            <button className="btn sm" onClick={() => setEdit(j)}><Icon n="edit" />Edit</button>
            <button className="btn sm bad push" onClick={() => del(j.id)}><Icon n="trash" />Delete</button>
          </JobCard>
        ))}
      </div>
      {jobs && !jobs.length && <Empty t="No jobs posted">Post your first job to start receiving applications.</Empty>}
      {jobs && !!jobs.length && !list.length && <Empty t="No matching jobs">Try a different search.</Empty>}
      {edit && <JobForm job={edit} onClose={() => setEdit(null)} onSaved={() => { setEdit(null); load() }} />}
    </>
  )
}
