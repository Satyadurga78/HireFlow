import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, toast } from '../api'
import { Brand } from '../components/Bits'
import './Login.css'
import './Register.css'

export default function Register({ onAuth }) {
  const nav = useNavigate()
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    const f = Object.fromEntries(new FormData(e.target)) // name, role, email, password
    setBusy(true)
    try {
      await api('/api/users', { method: 'POST', body: f, fallback: 'Could not create the account. That email may already be registered.' })
      const r = await api('/api/auth/login', { method: 'POST', body: { email: f.email, password: f.password }, fallback: 'Account created. Please sign in.' })
      onAuth({ token: r.token, email: r.email, role: r.role, name: f.name })
      nav('/dashboard')
    } catch (x) { toast(x.message, 1); setBusy(false) }
  }

  return (
    <div className="auth">
      <section className="hero">
        <Brand size={24} />
        <h1>Start in a minute.</h1>
        <p>Candidates upload a resume and apply. Recruiters post jobs and review matched applicants.</p>
      </section>
      <section className="form">
        <form onSubmit={submit}>
          <h1>Create your account</h1><p className="sub">Pick how you will use HireFlow.</p>
          <label>Full name</label><input name="name" required autoComplete="name" />
          <label>I am a</label>
          <div className="seg">
            <label><input type="radio" name="role" value="CANDIDATE" defaultChecked /><span>Candidate</span></label>
            <label><input type="radio" name="role" value="RECRUITER" /><span>Recruiter</span></label>
          </div>
          <label>Email</label><input name="email" type="email" required autoComplete="email" />
          <label>Password</label><input name="password" type="password" required minLength={6} autoComplete="new-password" />
          <button className="btn pri full lg" disabled={busy}>{busy ? 'Creating…' : 'Create account'}</button>
          <p className="sub alt">Already registered? <Link className="link" to="/login">Sign in</Link></p>
        </form>
      </section>
    </div>
  )
}
