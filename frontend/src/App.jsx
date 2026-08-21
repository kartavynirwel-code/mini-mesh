import { useState } from 'react'

// Read the greeting-service base URL from the runtime config injected via
// index.html -> config.js (see public/config.template.js for how that
// gets generated). Falling back to '/api' assumes an Ingress rule routes
// that path prefix to greeting-service — a reasonable default if no
// runtime config was provided.
const API_BASE = window.__APP_CONFIG__?.GREETING_SERVICE_URL || '/api'

function App() {
  const [name, setName] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  async function handleGreet() {
    const trimmed = name.trim()
    if (!trimmed) {
      setError('Please enter a name.')
      return
    }

    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const response = await fetch(`${API_BASE}/hello/${encodeURIComponent(trimmed)}`)

      if (!response.ok) {
        throw new Error(`Request failed with status ${response.status}`)
      }

      const data = await response.json()
      setResult(data)
    } catch (err) {
      // Deliberately surfaced to the user rather than swallowed — this
      // is a debugging tool for a devops project as much as a demo UI.
      // When you break mTLS or misconfigure a VirtualService later,
      // seeing this error is exactly how you'll notice.
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter') {
      handleGreet()
    }
  }

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>Mini-Mesh</h1>
        <p style={styles.subtitle}>
          Frontend &rarr; greeting-service &rarr; user-service &rarr; notification-service
        </p>

        <div style={styles.inputRow}>
          <input
            style={styles.input}
            type="text"
            placeholder="Enter a name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <button style={styles.button} onClick={handleGreet} disabled={loading}>
            {loading ? 'Greeting...' : 'Greet'}
          </button>
        </div>

        {error && <div style={styles.error}>Error: {error}</div>}

        {result && (
          <div style={styles.result}>
            <h2 style={styles.greeting}>{result.greeting}</h2>
            {result.userDetails && (
              <div style={styles.details}>
                <div><strong>User ID:</strong> {result.userDetails.userId}</div>
                <div><strong>Joined:</strong> {result.userDetails.joinedDate}</div>
              </div>
            )}
            <div style={styles.notificationStatus}>
              Notification: triggered asynchronously by user-service
              (check its logs / notification-service logs to confirm delivery)
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

// Inline styles keep this a single-file component with zero extra
// dependencies — fine for a demo UI whose whole point is the backend
// plumbing, not the frontend's own styling architecture.
const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: '#0f172a',
    fontFamily: 'system-ui, sans-serif',
  },
  card: {
    background: '#1e293b',
    padding: '2.5rem',
    borderRadius: '12px',
    width: '420px',
    boxShadow: '0 10px 30px rgba(0,0,0,0.3)',
  },
  title: { color: '#f8fafc', margin: 0, fontSize: '1.75rem' },
  subtitle: { color: '#94a3b8', fontSize: '0.85rem', marginTop: '0.25rem', marginBottom: '1.5rem' },
  inputRow: { display: 'flex', gap: '0.5rem' },
  input: {
    flex: 1,
    padding: '0.6rem 0.8rem',
    borderRadius: '8px',
    border: '1px solid #334155',
    background: '#0f172a',
    color: '#f8fafc',
    fontSize: '1rem',
  },
  button: {
    padding: '0.6rem 1.2rem',
    borderRadius: '8px',
    border: 'none',
    background: '#6366f1',
    color: '#fff',
    fontWeight: 600,
    cursor: 'pointer',
  },
  error: {
    marginTop: '1rem',
    color: '#f87171',
    fontSize: '0.9rem',
  },
  result: {
    marginTop: '1.5rem',
    padding: '1rem',
    background: '#0f172a',
    borderRadius: '8px',
  },
  greeting: { color: '#f8fafc', margin: '0 0 0.75rem 0', fontSize: '1.25rem' },
  details: { color: '#cbd5e1', fontSize: '0.9rem', lineHeight: 1.6 },
  notificationStatus: {
    marginTop: '0.75rem',
    paddingTop: '0.75rem',
    borderTop: '1px solid #334155',
    color: '#64748b',
    fontSize: '0.8rem',
  },
}

export default App
