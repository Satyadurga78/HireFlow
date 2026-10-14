import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, toast } from '../api'
import { Brand } from '../components/Bits'
import './Login.css'

export default function Login({ onAuth }) {
  const nav = useNavigate()
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    const f = Object.fromEntries(new FormData(e.target))
    setBusy(true)
    try {
      const r = await api('/api/auth/login', { method: 'POST', body: f, fallback: 'Invalid email or password.' })
      const u = { token: r.token, email: r.email, role: r.role }
      onAuth(u) // saves token so the profile call below is authenticated
      const p = await api('/api/profile').catch(() => ({}))
      onAuth({ ...u, name: p.name || r.email })
      nav('/dashboard')
    } catch (x) { toast(x.message, 1); setBusy(false) }
  }

  return (
    <div className="auth">
      <section className="hero">
        <Brand size={24} />
        <h1>Hiring that keeps moving.</h1>
        <p>Post roles, apply in one click, and see exactly how a resume lines up with the skills a job needs.</p>
        <div className="flow"><span>Applied</span><i /><span>Shortlisted</span><i /><span>Hired</span></div>
      </section>
      <section className="form">
        <form onSubmit={submit}>
          <h1>Welcome back</h1><p className="sub">Sign in to continue.</p>
          <label>Email</label><input name="email" type="email" required autoComplete="email" />
          <label>Password</label><input name="password" type="password" required autoComplete="current-password" />
          <button className="btn pri full lg" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
          <p className="sub alt">New here? <Link className="link" to="/register">Create an account</Link></p>
        </form>
      </section>
    </div>
  )
}
