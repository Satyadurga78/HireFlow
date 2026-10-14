export const API = import.meta.env.VITE_API_URL || 'http://localhost:8080' // set VITE_API_URL in .env to change

export const getUser = () => JSON.parse(localStorage.getItem('hf') || 'null')
export const setUser = (u) => (u ? localStorage.setItem('hf', JSON.stringify(u)) : localStorage.removeItem('hf'))

export const ini = (n) => (n || '?').trim().split(/\s+/).map((w) => w[0]).slice(0, 2).join('').toUpperCase()
export const split = (s) => (s || '').split(',').map((x) => x.trim()).filter(Boolean)
export const fmtDate = (d) => (d ? new Date(d).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' }) : '')

export function toast(m, err) {
  let box = document.getElementById('toast')
  if (!box) { box = document.createElement('div'); box.id = 'toast'; document.body.append(box) }
  const t = document.createElement('div')
  t.className = 't' + (err ? ' err' : '')
  t.textContent = m
  box.append(t)
  setTimeout(() => t.remove(), 3500)
}

// Central fetch wrapper: adds JWT, handles errors and blobs
export async function api(path, o = {}) {
  const u = getUser()
  const h = {}
  if (u?.token) h.Authorization = 'Bearer ' + u.token
  let body = o.form
  if (o.body) { h['Content-Type'] = 'application/json'; body = JSON.stringify(o.body) }
  let r
  try { r = await fetch(API + path, { method: o.method || 'GET', headers: h, body }) }
  catch { throw new Error('Cannot reach the server. Start the backend on ' + API) }
  if (r.status === 401 && u && !o.fallback) { setUser(null); location.href = '/login'; throw new Error('Session expired') }
  if (!r.ok) {
    let m = o.fallback || (r.status === 403 ? "You don't have access to this." : 'Something went wrong. Please try again.')
    try { const j = await r.json(); if (r.status < 500 && (j.message || j.error)) m = j.message || j.error } catch {}
    throw new Error(m)
  }
  if (o.blob) return r.blob()
  const t = await r.text()
  try { return JSON.parse(t) } catch { return t }
}

// run an API action, toast on success/failure
export const run = async (fn, ok) => {
  try { const r = await fn(); if (ok) toast(ok); return r } catch (e) { toast(e.message, 1) }
}

export async function download(path, name) {
  const b = await api(path, { blob: 1 })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(b); a.download = name; a.click()
}

export const fmtDateTime = (d) => (d ? new Date(d).toLocaleString(undefined, { day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit' }) : '')

// ---- Saved jobs (stored in this browser, one list per account) ----
const readJson = (k) => { try { return JSON.parse(localStorage.getItem(k) || '[]') } catch { return [] } }
export const getSaved = (email) => readJson('hf-saved:' + email)
export function toggleSaved(email, id) {
  const s = new Set(getSaved(email))
  s.has(id) ? s.delete(id) : s.add(id)
  const a = [...s]
  localStorage.setItem('hf-saved:' + email, JSON.stringify(a))
  return a
}

// ---- Notifications: built from the user's applications (and interviews), read-state kept per account ----
export const getRead = (email) => readJson('hf-read:' + email)
export function markRead(email, ids) {
  localStorage.setItem('hf-read:' + email, JSON.stringify([...new Set([...getRead(email), ...ids])]))
  window.dispatchEvent(new Event('hf-notif'))
}

// candidate status -> [message, tone, icon]
const STATUS_MSG = {
  SHORTLISTED: ['You were shortlisted', 'g', 'check'],
  INTERVIEW: ['You moved to the interview stage', 'p', 'calendar'],
  SELECTED: ['Congratulations, you were selected', 'g', 'check'],
  REJECTED: ['Your application was not selected', 'r', 'x'],
}

export async function loadNotifs(u) {
  const rec = u.role === 'RECRUITER'
  let apps
  try { apps = (await api(rec ? '/api/applications/all' : '/api/applications/my-applications')) || [] } catch { return [] }
  if (rec) {
    return [...apps].reverse().slice(0, 30).map((a) => ({
      id: a.applicationId + ':new', tone: 'p', icon: 'users',
      title: (a.candidateName || 'A candidate') + ' applied', sub: [a.jobTitle, fmtDate(a.appliedAt)].filter(Boolean).join(' · '),
    }))
  }
  const ivs = await api('/api/interviews', { fallback: 'x' }).catch(() => [])
  const ivList = Array.isArray(ivs) ? ivs : []
  const withIv = new Set(ivList.map((i) => i.applicationId))
  const out = ivList.map((i) => {
    const a = apps.find((x) => x.id === i.applicationId)
    return { id: 'iv:' + (i.id ?? i.applicationId), tone: 'p', icon: 'calendar', title: 'Interview scheduled', sub: [a?.title, fmtDateTime(i.interviewDate), i.interviewType].filter(Boolean).join(' · ') }
  })
  for (const a of [...apps].reverse()) {
    const m = STATUS_MSG[a.status]
    if (!m || (a.status === 'INTERVIEW' && withIv.has(a.id))) continue
    out.push({ id: a.id + ':' + a.status, tone: m[1], icon: m[2], title: m[0], sub: [a.title, a.companyName].filter(Boolean).join(' · ') })
  }
  return out
}
