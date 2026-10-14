import { useEffect, useState } from 'react'
import { getRead, loadNotifs, markRead } from '../api'
import { Empty, Icon, PageHead } from '../components/Bits'
import './Notifications.css'

export default function Notifications({ user }) {
  const [list, setList] = useState(null)
  const [read, setRead] = useState(() => new Set(getRead(user.email)))
  useEffect(() => { loadNotifs(user).then(setList) }, []) // eslint-disable-line

  const mark = (ids) => { markRead(user.email, ids); setRead((s) => new Set([...s, ...ids])) }
  const unread = (list || []).filter((n) => !read.has(n.id))

  return (
    <>
      <PageHead title="Notifications" sub={list ? (unread.length ? `${unread.length} unread` : 'You are all caught up.') : 'Loading…'}>
        {!!unread.length && <button className="btn" onClick={() => mark(unread.map((n) => n.id))}><Icon n="check" />Mark all as read</button>}
      </PageHead>
      {!!(list || []).length && (
        <section className="card nts">
          {list.map((n) => (
            <button key={n.id} className={'nt' + (read.has(n.id) ? '' : ' unread')} onClick={() => mark([n.id])}>
              <span className={'ni ' + n.tone}><Icon n={n.icon} /></span>
              <span className="nb"><b>{n.title}</b><small>{n.sub}</small></span>
              {!read.has(n.id) && <i className="dot" aria-label="Unread" />}
            </button>
          ))}
        </section>
      )}
      {list && !list.length && <Empty t="No notifications yet">Updates about your applications will show up here.</Empty>}
    </>
  )
}
