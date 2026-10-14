import { useEffect, useState } from 'react'
import { Routes, Route, Navigate, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { getRead, getUser, loadNotifs, setUser } from './api'
import { Avatar, Brand, Icon } from './components/Bits'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Profile from './pages/Profile'
import Jobs from './pages/Jobs'
import ManageJobs from './pages/ManageJobs'
import MyApplications from './pages/MyApplications'
import Applicants from './pages/Applicants'
import Notifications from './pages/Notifications'

// [path, label, icon]
const NAV = {
  CANDIDATE: [['/dashboard', 'Dashboard', 'dash'], ['/jobs', 'Explore Jobs', 'briefcase'], ['/saved', 'Saved Jobs', 'bookmark'], ['/applications', 'My Applications', 'doc'], ['/notifications', 'Notifications', 'bell'], ['/profile', 'My Profile', 'user']],
  RECRUITER: [['/dashboard', 'Dashboard', 'dash'], ['/manage-jobs', 'Manage Jobs', 'briefcase'], ['/applicants', 'Applicants', 'users'], ['/notifications', 'Notifications', 'bell'], ['/profile', 'My Profile', 'user']],
}

// Pages that filter their own list from the search bar: path -> placeholder
const SEARCH = {
  CANDIDATE: { '/jobs': 'Search jobs, skills, or companies…', '/saved': 'Search saved jobs…', '/applications': 'Search your applications…' },
  RECRUITER: { '/manage-jobs': 'Search your jobs…', '/applicants': 'Search applicants by name, email or job…' },
}
export default function App() {
  const nav = useNavigate()
  const loc = useLocation()
  const [user, setU] = useState(getUser())
  const [photoV, setPhotoV] = useState(0) // bump to refresh avatars
  const [q, setQ] = useState('')
  const [unread, setUnread] = useState(0)
  const auth = (u) => { setUser(u); setU(u) }
  const logout = () => { auth(null); nav('/login') }

  // keep the search box in sync with the page's own filter text
  useEffect(() => { setQ(loc.state?.q || '') }, [loc.pathname]) // eslint-disable-line

  // unread count for the Notifications badge
  useEffect(() => {
    if (!user) return
    let live = true
    const calc = () => loadNotifs(user).then((l) => { const r = new Set(getRead(user.email)); live && setUnread(l.filter((n) => !r.has(n.id)).length) })
    calc()
    window.addEventListener('hf-notif', calc)
    return () => { live = false; window.removeEventListener('hf-notif', calc) }
  }, [user?.email, user?.role, loc.pathname]) // eslint-disable-line

  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<Login onAuth={auth} />} />
        <Route path="/register" element={<Register onAuth={auth} />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    )
  }

  const rec = user.role === 'RECRUITER'
  const pages = SEARCH[user.role]
  const here = pages[loc.pathname] // placeholder if this page can be filtered, else undefined
  const type = (v) => {
    setQ(v)
    if (here) nav(loc.pathname, { state: { ...loc.state, q: v.trim() }, replace: true }) // filter live while typing
  }

  return (
    <div className="shell">
      <aside>
        <Brand />
        <nav>
          {NAV[user.role].map(([to, label, ic]) => (
            <NavLink key={to} to={to} className="nv"><Icon n={ic} />{label}{to === '/notifications' && unread > 0 && <span className="badge">{unread > 9 ? '9+' : unread}</span>}</NavLink>
          ))}
        </nav>
        <div className="me">
          <div className="who">
            <Avatar cls="s" path="/api/profile/photo" name={user.name} v={photoV} />
            <div><b>{user.name || user.email}</b><small>{rec ? 'Recruiter' : 'Candidate'}</small></div>
          </div>
          <button onClick={logout}><Icon n="logout" /><span>Logout</span></button>
        </div>
      </aside>
      <div className="content">
        {here && ( // search bar only on pages that have a list to filter (not Dashboard / Profile)
          <header className="top">
            <form onSubmit={(e) => e.preventDefault()} role="search">
              <Icon n="search" />
              <input value={q} onChange={(e) => type(e.target.value)} placeholder={here} aria-label="Search" />
            </form>
          </header>
        )}
        <main>
          <Routes>
            <Route path="/dashboard" element={<Dashboard user={user} />} />
            <Route path="/profile" element={<Profile user={user} onChange={(name) => { auth({ ...user, name }); setPhotoV((v) => v + 1) }} />} />
            <Route path="/jobs" element={rec ? <Navigate to="/manage-jobs" /> : <Jobs key="all" />} />
            <Route path="/saved" element={rec ? <Navigate to="/manage-jobs" /> : <Jobs key="saved" saved />} />
            <Route path="/notifications" element={<Notifications user={user} />} />
            <Route path="/applications" element={rec ? <Navigate to="/applicants" /> : <MyApplications />} />
            <Route path="/manage-jobs" element={rec ? <ManageJobs /> : <Navigate to="/jobs" />} />
            <Route path="/applicants" element={rec ? <Applicants /> : <Navigate to="/applications" />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  )
}
