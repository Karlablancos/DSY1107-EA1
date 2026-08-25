import { useEffect, useState } from 'react'

const COGNITO_DOMAIN = 'https://dsy1107-grupo10.auth.us-east-1.amazoncognito.com'
const CLIENT_ID = 'dblq8gdjkl7p3qntcnt3g5kq'
const REDIRECT_URI = 'http://localhost:5173/'
const API_URL = 'https://dh0ga4nlg6.execute-api.us-east-1.amazonaws.com/datos'

function generateCodeVerifier() {
  const array = new Uint8Array(64)
  crypto.getRandomValues(array)
  return btoa(String.fromCharCode(...array))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')
}

async function generateCodeChallenge(verifier) {
  const encoder = new TextEncoder()
  const data = encoder.encode(verifier)
  const digest = await crypto.subtle.digest('SHA-256', data)
  return btoa(String.fromCharCode(...new Uint8Array(digest)))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')
}

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]))
  } catch {
    return null
  }
}

function App() {
  const [token, setToken] = useState(null)
  const [idToken, setIdToken] = useState(null)
  const [resultado, setResultado] = useState(null)
  const [etiqueta, setEtiqueta] = useState('')
  const [mostrarId, setMostrarId] = useState(false)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const code = params.get('code')
    if (code) {
      const verifier = sessionStorage.getItem('code_verifier')
      fetch(`${COGNITO_DOMAIN}/oauth2/token`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
          grant_type: 'authorization_code',
          client_id: CLIENT_ID,
          redirect_uri: REDIRECT_URI,
          code,
          code_verifier: verifier,
        }),
      })
        .then(r => r.json())
        .then(data => {
          setToken(data.access_token)
          setIdToken(data.id_token)
          window.history.replaceState({}, '', '/')
        })
    }
  }, [])

  async function login() {
    const verifier = generateCodeVerifier()
    const challenge = await generateCodeChallenge(verifier)
    sessionStorage.setItem('code_verifier', verifier)
    const url = `${COGNITO_DOMAIN}/oauth2/authorize?` +
      new URLSearchParams({
        response_type: 'code',
        client_id: CLIENT_ID,
        redirect_uri: REDIRECT_URI,
        scope: 'openid profile email',
        code_challenge_method: 'S256',
        code_challenge: challenge,
      })
    window.location.href = url
  }

  async function llamar(url, conToken, label) {
    setEtiqueta(label)
    setResultado(null)
    try {
      const headers = conToken ? { Authorization: `Bearer ${token}` } : {}
      const res = await fetch(url, { headers })
      const data = await res.json()
      setResultado({ status: res.status, data })
    } catch (e) {
      setResultado({ status: 'Error', data: e.message })
    }
  }

  const btnStyle = (color) => ({
    backgroundColor: color || '#1a73e8',
    color: 'white',
    border: 'none',
    padding: '10px 20px',
    fontSize: '0.9rem',
    fontWeight: 'bold',
    borderRadius: '6px',
    cursor: 'pointer',
    margin: '4px'
  })

  const preStyle = {
    background: '#f4f4f4',
    padding: '1rem',
    borderRadius: '6px',
    overflow: 'auto',
    fontSize: '0.85rem'
  }

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: '900px', margin: '0 auto' }}>
      <h1>DSY1107 - Grupo 10</h1>

      {!token ? (
        <button onClick={login} style={{
          backgroundColor: '#000000',
          color: '#FFD700',
          border: '2px solid #FFD700',
          padding: '12px 32px',
          fontSize: '1rem',
          fontWeight: 'bold',
          borderRadius: '8px',
          cursor: 'pointer',
          letterSpacing: '1px'
        }}>
          Iniciar sesión
        </button>
      ) : (
        <div>

          <h2>2 · Los tokens que devolvió el IDaaS</h2>
          <p>El <strong>ID Token</strong> dice quién eres (OIDC). El <strong>Access Token</strong> dice qué puedes hacer (OAuth2): fíjate en su claim <code>scope</code>.</p>

          <div style={{ marginBottom: '0.5rem' }}>
            <button style={btnStyle(mostrarId ? '#1a73e8' : '#555')} onClick={() => setMostrarId(true)}>
              ▶ ID Token · claims
            </button>
          </div>

          <div>
            <strong>▼ Access Token · claims</strong>
            <pre style={preStyle}>{JSON.stringify(parseJwt(token), null, 2)}</pre>
          </div>

          {mostrarId && idToken && (
            <div>
              <strong>▼ ID Token · claims</strong>
              <pre style={preStyle}>{JSON.stringify(parseJwt(idToken), null, 2)}</pre>
            </div>
          )}

          <h2>3 · Consumir APIs con ese token</h2>
          <div style={{ marginBottom: '1rem' }}>
            <button style={btnStyle()} onClick={() =>
              llamar(`${COGNITO_DOMAIN}/oauth2/userInfo`, true, 'GET /oauth2/userInfo')}>
              /oauth2/userInfo
            </button>
            <button style={btnStyle()} onClick={() =>
              llamar(`${COGNITO_DOMAIN}/oauth2/userInfo`, true, 'Cognito GetUser')}>
              Cognito GetUser
            </button>
            <button style={btnStyle()} onClick={() =>
              llamar(API_URL, true, 'GET /datos con token → HTTP 200')}>
              /datos con token
            </button>
            <button style={btnStyle('#c0392b')} onClick={() =>
              llamar(API_URL, false, 'GET /datos sin token')}>
              /datos sin token
            </button>
            <button style={btnStyle()} onClick={() =>
              llamar(API_URL.replace('/datos', '/publico/datos'), false, 'GET /publico/datos')}>
              /publico/datos
            </button>
          </div>

          {resultado && (
            <div>
              <p><strong>{etiqueta}</strong></p>
              <pre style={preStyle}>{JSON.stringify(resultado.data, null, 2)}</pre>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default App