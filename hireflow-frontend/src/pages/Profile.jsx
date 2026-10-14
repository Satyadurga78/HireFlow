import { useEffect, useState } from 'react'
import { api, run, download } from '../api'
import { Avatar, Chip, Icon, Modal, PageHead } from '../components/Bits'
import './Profile.css'

export default function Profile({ user, onChange }) {
  const [p, setP] = useState(null)
  const [v, setV] = useState(0)
  const [text, setText] = useState(null)
  const cand = user.role === 'CANDIDATE'
  const load = () => run(() => api('/api/profile')).then((d) => { setP(d); setV((x) => x + 1); d && onChange(d.name) })
  useEffect(() => { load() }, []) // eslint-disable-line

  if (!p) return null

  const save = async (e) => {
    e.preventDefault()
    if (await run(() => api('/api/profile', { method: 'PUT', body: Object.fromEntries(new FormData(e.target)) }), 'Profile saved')) load()
  }
  const upload = (path, msg) => async (e) => {
    const f = e.target.files[0]; if (!f) return
    const fd = new FormData(); fd.append('file', f)
    if (await run(() => api(path, { method: 'POST', form: fd, fallback: 'Upload failed. Check the file type and size (max 5 MB).' }), msg)) load()
  }
  const del = async (path, msg, ask) => {
    if (ask && !confirm(ask)) return
    if (await run(() => api(path, { method: 'DELETE' }), msg)) load()
  }
  const analyze = async () => {
    const d = await run(() => api('/api/skillmatch/analyze', { method: 'POST', fallback: 'Upload your resume (PDF) first.' }))
    if (d) setText(d.extractedText || '(No text could be read from this PDF)')
  }

  return (
    <>
      <PageHead title="My Profile" sub={cand ? 'Keep your details and resume up to date.' : 'Your account details.'} />
      <div className="stack">
      <div className="card row" style={{ gap: 18 }}>
        <Avatar cls="lg" path="/api/profile/photo" name={p.name} v={v} />
        <div>
          <h3>{p.name}</h3><small>{p.email}</small>
          <div className="row" style={{ margin: '10px 0 6px' }}>
            <label className="btn sm file"><Icon n="upload" />{p.profilePhotoUploaded ? 'Change photo' : 'Upload photo'}
              <input type="file" accept="image/png,image/jpeg" hidden onChange={upload('/api/profile/photo', 'Photo updated')} />
            </label>
            {p.profilePhotoUploaded && <button className="btn sm bad" onClick={() => del('/api/profile/photo', 'Photo removed')}>Remove</button>}
          </div>
          <span className="hint">JPG or PNG, up to 5 MB</span>
        </div>
      </div>

      <form className="card" onSubmit={save} key={[p.name, p.phone, p.location, p.skills, p.education, p.experience, p.bio, p.companyName, p.companyLocation, p.companyDescription].join('|')}>
        <div className="two">
          <div><label>Name</label><input name="name" defaultValue={p.name || ''} required /></div>
          <div><label>Phone</label><input name="phone" defaultValue={p.phone || ''} /></div>
        </div>
        <label>Location</label><input name="location" defaultValue={p.location || ''} />
        {cand ? <>
          <div className="two">
            <div><label>Education</label><input name="education" defaultValue={p.education || ''} /></div>
            <div><label>Experience</label><input name="experience" placeholder="e.g. 2 years" defaultValue={p.experience || ''} /></div>
          </div>
          <label>Skills (comma separated)</label><textarea name="skills" rows={2} defaultValue={p.skills || ''} />
          <label>Bio</label><textarea name="bio" rows={3} placeholder="A short introduction about you" defaultValue={p.bio || ''} />
        </> : <>
          <h3 className="sec">Company profile</h3>
          <div className="two">
            <div><label>Company name</label><input name="companyName" defaultValue={p.companyName || ''} /></div>
            <div><label>Company location</label><input name="companyLocation" defaultValue={p.companyLocation || ''} /></div>
          </div>
          <label>Company description</label><textarea name="companyDescription" rows={3} defaultValue={p.companyDescription || ''} />
        </>}
        <div className="row" style={{ marginTop: 20 }}><button className="btn pri">Save profile</button></div>
      </form>

      {cand && (
        <div className="card">
          <h3>Resume</h3>
          <p className="sub">
            {p.resumeUploaded ? 'Your resume is on file. Recruiters see it when you apply.' : 'Upload a PDF (up to 5 MB) so recruiters and SkillMatch can read it.'}
          </p>
          <div className="row">
            <Chip t={p.resumeUploaded ? 'Uploaded' : 'Missing'} c={p.resumeUploaded ? 'g' : 'a'} />
            <label className="btn sm file"><Icon n="upload" />{p.resumeUploaded ? 'Replace PDF' : 'Upload PDF'}
              <input type="file" accept="application/pdf" hidden onChange={upload('/api/profile/resume', 'Resume uploaded')} />
            </label>
            {p.resumeUploaded && <>
              <button className="btn sm" onClick={() => run(() => download('/api/profile/resume', 'HireFlow-Resume.pdf'))}>Download</button>
              <button className="btn sm" onClick={analyze}>Preview extracted text</button>
              <button className="btn sm bad" onClick={() => del('/api/profile/resume', 'Resume deleted', 'Delete your resume?')}>Delete</button>
            </>}
          </div>
        </div>
      )}
      </div>

      {text !== null && (
        <Modal onClose={() => setText(null)}>
          <h2>Text read from your resume</h2>
          <p className="sub">This is what SkillMatch sees. If it looks empty or scrambled, matches will be poor.</p>
          <pre>{text}</pre>
          <div className="row md-actions"><button className="btn push" onClick={() => setText(null)}>Close</button></div>
        </Modal>
      )}
    </>
  )
}
