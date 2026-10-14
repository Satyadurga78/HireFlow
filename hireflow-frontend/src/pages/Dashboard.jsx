import CandidateDashboard from './CandidateDashboard'
import RecruiterDashboard from './RecruiterDashboard'
import './Dashboard.css'

export default function Dashboard({ user }) {
  return user.role === 'RECRUITER' ? <RecruiterDashboard user={user} /> : <CandidateDashboard user={user} />
}
