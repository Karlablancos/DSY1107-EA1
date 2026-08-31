import { useEffect, useState, useRef } from 'react'
import { configuracionIncompleta, login, logout, procesarRetorno, getTokens, getAccessToken, getIdToken, decodificarJwt, estaExpirado } from './auth.js'
import { obtenerUserInfo, obtenerUsuarioCognito, obtenerIndicadores, obtenerIndicadoresPublicos } from './api.js'

export default function App() {
  const [tokens, setTokens] = useState(getTokens())
  const [error, setError] = useState(null)
  const [resultado, setResultado] = useState(null)
  const [cargando, setCargando] = useState(false)
  const procesado = useRef(false)

  useEffect(() => {
    if (procesado.current) return
    procesado.current = true
    procesarRetorno()
      .then((nuevos) => nuevos && setTokens(nuevos))
      .catch((e) => setError(e.message))
  }, [])

  const faltantes = configuracionIncompleta()
  const sesionActiva = Boolean(tokens) && !estaExpirado(getAccessToken())
  const idClaims = decodificarJwt(getIdToken())
  const accessClaims = decodificarJwt(getAccessToken())

  async function llamar(fn) {
    setCargando(true)
    setResultado(await fn())
    setCargando(false)
  }

  return (
    <main>
      <h1>DSY1107 · Identidad con Cognito</h1>
      {error && <p>{error}</p>}
      {faltantes.length > 0 ? (
        <p>Falta configurar: {faltantes.join(', ')}</p>
      ) : sesionActiva ? (
        <div>
          <p>✅ Sesión iniciada</p>
          <button onClick={logout}>Cerrar sesión</button>

          <details open>
            <summary>ID Token · claims</summary>
            <pre>{JSON.stringify(idClaims, null, 2)}</pre>
          </details>
          <details>
            <summary>Access Token · claims</summary>
            <pre>{JSON.stringify(accessClaims, null, 2)}</pre>
          </details>

          <h2>3 · Consumir APIs con ese token</h2>
          <div className="botones">
            <button onClick={() => llamar(obtenerUserInfo)}>/oauth2/userInfo</button>
            <button onClick={() => llamar(obtenerUsuarioCognito)}>Cognito GetUser</button>
            <button onClick={() => llamar(() => obtenerIndicadores(true))}>/datos con token</button>
            <button onClick={() => llamar(() => obtenerIndicadores(false))}>/datos sin token</button>
            <button onClick={() => llamar(obtenerIndicadoresPublicos)}>/publico/datos</button>
          </div>

          {cargando && <p>Llamando…</p>}
          {resultado && !cargando && (
            <div>
              <p><strong>{resultado.descripcion}</strong> → HTTP {resultado.status || 'sin respuesta'}</p>
              <pre>{JSON.stringify(resultado.cuerpo, null, 2)}</pre>
            </div>
          )}
        </div>
      ) : (
        <button onClick={login}>Iniciar sesión con Cognito</button>
      )}
    </main>
  )
}