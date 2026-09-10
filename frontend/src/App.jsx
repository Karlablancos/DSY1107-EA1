import { useEffect, useState, useRef } from 'react'
import { configuracionIncompleta, login, logout, procesarRetorno, getTokens, getAccessToken, getIdToken, decodificarJwt, estaExpirado } from './auth.js'
import { obtenerUserInfo, listarGastos, crearGasto, actualizarGasto, resolverGasto, eliminarGasto, probarSinToken } from './api.js'

export default function App() {
  const [tokens, setTokens] = useState(getTokens())
  const [error, setError] = useState(null)
  const [gastos, setGastos] = useState([])
  const [cargando, setCargando] = useState(false)
  const [mensaje, setMensaje] = useState(null)
  const [formulario, setFormulario] = useState({ descripcion: '', monto: '', categoria: '', fecha: '' })
  const [editando, setEditando] = useState(null)
  const [resolucion, setResolucion] = useState({ estado: 'APROBADO', comentario: '' })
  const [resolviendoId, setResolviendoId] = useState(null)
  const procesado = useRef(false)

  useEffect(() => {
    if (procesado.current) return
    procesado.current = true
    procesarRetorno()
      .then((nuevos) => nuevos && setTokens(nuevos))
      .catch((e) => setError(e.message))
  }, [])

  useEffect(() => {
    if (sesionActiva) cargarGastos()
  }, [tokens])

  const faltantes = configuracionIncompleta()
  const sesionActiva = Boolean(tokens) && !estaExpirado(getAccessToken())
  const idClaims = decodificarJwt(getIdToken())
  const accessClaims = decodificarJwt(getAccessToken())
  const grupos = accessClaims?.['cognito:groups'] ?? []
  const esAprobador = grupos.includes('aprobador')

  async function cargarGastos() {
    setCargando(true)
    const result = await listarGastos()
    if (result.ok) setGastos(result.cuerpo)
    else setMensaje(`Error al cargar: ${result.status}`)
    setCargando(false)
  }

  async function handleCrear(e) {
    e.preventDefault()
    const result = await crearGasto({
      ...formulario,
      monto: parseInt(formulario.monto),
    })
    if (result.ok) {
      setMensaje('Gasto creado ✅')
      setFormulario({ descripcion: '', monto: '', categoria: '', fecha: '' })
      cargarGastos()
    } else {
      setMensaje(`Error: HTTP ${result.status}`)
    }
  }

  async function handleActualizar(e) {
    e.preventDefault()
    const result = await actualizarGasto(editando.id, {
      ...formulario,
      monto: parseInt(formulario.monto),
    })
    if (result.ok) {
      setMensaje('Gasto actualizado ✅')
      setEditando(null)
      setFormulario({ descripcion: '', monto: '', categoria: '', fecha: '' })
      cargarGastos()
    } else {
      setMensaje(`Error: HTTP ${result.status}`)
    }
  }

  async function handleEliminar(id) {
    if (!confirm('¿Eliminar este gasto?')) return
    const result = await eliminarGasto(id)
    if (result.ok || result.status === 204) {
      setMensaje('Gasto eliminado ✅')
      cargarGastos()
    } else {
      setMensaje(`Error: HTTP ${result.status}`)
    }
  }

  async function handleResolver(e) {
    e.preventDefault()
    const result = await resolverGasto(resolviendoId, resolucion)
    if (result.ok || result.status === 200) {
      setMensaje(`Gasto ${resolucion.estado.toLowerCase()} ✅`)
      setResolviendoId(null)
      cargarGastos()
    } else {
      setMensaje(`Error: HTTP ${result.status}`)
    }
  }

  function iniciarEdicion(gasto) {
    setEditando(gasto)
    setFormulario({
      descripcion: gasto.descripcion,
      monto: gasto.monto,
      categoria: gasto.categoria,
      fecha: gasto.fecha,
    })
  }

  if (faltantes.length > 0) return <p>Falta configurar: {faltantes.join(', ')}</p>

  if (!sesionActiva) {
    return (
      <main style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
        <img src="/muck-logo.png" alt="Muck Consultores" style={{ height: 60 }} />
        <h1>Gestión de Gastos</h1>
        <p>Inicia sesión para continuar.</p>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button onClick={login}>Iniciar sesión con Cognito</button>
      </main>
    )
  }

  return (
    <main style={{ padding: '2rem', fontFamily: 'sans-serif', maxWidth: 900 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <img src="/muck-logo.png" alt="Muck Consultores" style={{ height: 40 }} />
          <h1 style={{ display: 'inline', marginLeft: '1rem' }}>Gestión de Gastos</h1>
        </div>
        <div>
          <span style={{ marginRight: '1rem' }}>👤 {idClaims?.email} ({grupos.join(', ') || 'sin grupo'})</span>
          <button onClick={logout}>Cerrar sesión</button>
        </div>
      </div>

      {mensaje && <p style={{ background: '#e8f5e9', padding: '0.5rem', borderRadius: 4 }}>{mensaje}</p>}

      <h2>{editando ? '✏️ Editar gasto' : '➕ Nuevo gasto'}</h2>
      <form onSubmit={editando ? handleActualizar : handleCrear} style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <input placeholder="Descripción" required value={formulario.descripcion}
          onChange={e => setFormulario({ ...formulario, descripcion: e.target.value })} />
        <input placeholder="Monto" type="number" required value={formulario.monto}
          onChange={e => setFormulario({ ...formulario, monto: e.target.value })} />
        <select required value={formulario.categoria}
          onChange={e => setFormulario({ ...formulario, categoria: e.target.value })}>
          <option value="">Categoría</option>
          <option value="transporte">Transporte</option>
          <option value="alimentacion">Alimentación</option>
          <option value="insumos">Insumos</option>
          <option value="otro">Otro</option>
        </select>
        <input type="date" required value={formulario.fecha}
          onChange={e => setFormulario({ ...formulario, fecha: e.target.value })} />
        <button type="submit">{editando ? 'Actualizar' : 'Crear'}</button>
        {editando && <button type="button" onClick={() => { setEditando(null); setFormulario({ descripcion: '', monto: '', categoria: '', fecha: '' }) }}>Cancelar</button>}
      </form>

      <h2>📋 Gastos</h2>
      <button onClick={cargarGastos}>🔄 Refrescar</button>
      <button onClick={probarSinToken} style={{ marginLeft: '0.5rem' }}>🔒 Probar sin token (401)</button>

      {cargando && <p>Cargando...</p>}

      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
        <thead>
          <tr style={{ background: '#f5f5f5' }}>
            <th>Descripción</th><th>Monto</th><th>Categoría</th><th>Fecha</th><th>Estado</th><th>Comentario</th><th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {gastos.length === 0 && !cargando && (
            <tr><td colSpan={7} style={{ textAlign: 'center', padding: '1rem' }}>No hay gastos registrados</td></tr>
          )}
          {gastos.map(g => (
            <tr key={g.id} style={{ borderBottom: '1px solid #eee' }}>
              <td>{g.descripcion}</td>
              <td>${g.monto.toLocaleString()}</td>
              <td>{g.categoria}</td>
              <td>{g.fecha}</td>
              <td>
                <span style={{ color: g.estado === 'APROBADO' ? 'green' : g.estado === 'RECHAZADO' ? 'red' : 'orange' }}>
                  {g.estado}
                </span>
              </td>
              <td>{g.comentario ?? '-'}</td>
              <td style={{ display: 'flex', gap: '0.3rem' }}>
                {g.estado === 'PENDIENTE' && (
                  <button onClick={() => iniciarEdicion(g)}>✏️</button>
                )}
                {g.estado === 'PENDIENTE' && (
                  <button onClick={() => handleEliminar(g.id)}>🗑️</button>
                )}
                {esAprobador && g.estado === 'PENDIENTE' && (
                  <button onClick={() => setResolviendoId(g.id)}>✅</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {resolviendoId && (
        <div style={{ background: '#fff3e0', padding: '1rem', marginTop: '1rem', borderRadius: 8 }}>
          <h3>Resolver gasto #{resolviendoId}</h3>
          <form onSubmit={handleResolver} style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <select value={resolucion.estado} onChange={e => setResolucion({ ...resolucion, estado: e.target.value })}>
              <option value="APROBADO">Aprobar</option>
              <option value="RECHAZADO">Rechazar</option>
            </select>
            <input placeholder="Comentario" value={resolucion.comentario}
              onChange={e => setResolucion({ ...resolucion, comentario: e.target.value })} />
            <button type="submit">Confirmar</button>
            <button type="button" onClick={() => setResolviendoId(null)}>Cancelar</button>
          </form>
        </div>
      )}
    </main>
  )
}