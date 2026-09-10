import { getAccessToken, config } from './auth.js'

const REGION = import.meta.env.VITE_AWS_REGION
const API_URL = import.meta.env.VITE_API_URL

async function ejecutar(descripcion, promesa) {
  try {
    const respuesta = await promesa
    const texto = await respuesta.text()
    let cuerpo
    try { cuerpo = JSON.parse(texto) } catch { cuerpo = texto }
    return { descripcion, status: respuesta.status, ok: respuesta.ok, cuerpo }
  } catch (error) {
    return { descripcion, status: 0, ok: false, cuerpo: `Error de red o CORS: ${error.message}` }
  }
}

export function obtenerUserInfo() {
  return ejecutar(
    'GET /oauth2/userInfo (OIDC)',
    fetch(`${config.dominio}/oauth2/userInfo`, {
      headers: { Authorization: `Bearer ${getAccessToken()}` },
    })
  )
}

export function obtenerUsuarioCognito() {
  return ejecutar(
    'POST cognito-idp GetUser (API de AWS)',
    fetch(`https://cognito-idp.${REGION}.amazonaws.com/`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-amz-json-1.1',
        'X-Amz-Target': 'AWSCognitoIdentityProviderService.GetUser',
      },
      body: JSON.stringify({ AccessToken: getAccessToken() }),
    })
  )
}

export function listarGastos() {
  return ejecutar(
    'GET /gastos (con token)',
    fetch(`${API_URL}/gastos`, {
      headers: { Authorization: `Bearer ${getAccessToken()}` },
    })
  )
}

export function crearGasto(datos) {
  return ejecutar(
    'POST /gastos',
    fetch(`${API_URL}/gastos`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${getAccessToken()}`,
        'Content-Type': 'application/json',
        'X-Usuario-Id': 'solicitante@muck.cl',
      },
      body: JSON.stringify(datos),
    })
  )
}

export function actualizarGasto(id, datos) {
  return ejecutar(
    `PUT /gastos/${id}`,
    fetch(`${API_URL}/gastos/${id}`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${getAccessToken()}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(datos),
    })
  )
}

export function resolverGasto(id, resolucion) {
  return ejecutar(
    `PATCH /gastos/${id}/resolucion`,
    fetch(`${API_URL}/gastos/${id}/resolucion`, {
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${getAccessToken()}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(resolucion),
    })
  )
}

export function eliminarGasto(id) {
  return ejecutar(
    `DELETE /gastos/${id}`,
    fetch(`${API_URL}/gastos/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${getAccessToken()}` },
    })
  )
}

export function probarSinToken() {
  return ejecutar(
    'GET /gastos SIN token (debe dar 401)',
    fetch(`${API_URL}/gastos`)
  )
}