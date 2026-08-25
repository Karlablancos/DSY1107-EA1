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

function App() {
  const [token, setToken] = useState(null)
  const [datos, setDatos] = useState(null)
  const [error, setError] = useState(null)

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

  async function llamarApi() {
    try {
      const res = await fetch(API_URL, {
        headers: { Authorization: `Bearer ${token}` }
      })
      const data = await res.json()
      setDatos(data)
    } catch (e) {
      setError('Error al llamar la API')
    }
  }

  return (
    <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
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
          <p>✅ Sesión iniciada</p>
          <button onClick={llamarApi} style={{
            backgroundColor: '#1a73e8',
            color: 'white',
            border: 'none',
            padding: '10px 28px',
            fontSize: '1rem',
            fontWeight: 'bold',
            borderRadius: '8px',
            cursor: 'pointer'
          }}>
            Llamar API segura
          </button>
          {datos && <pre>{JSON.stringify(datos, null, 2)}</pre>}
          {error && <p style={{ color: 'red' }}>{error}</p>}
        </div>
      )}
    </div>
  )
}

export default App