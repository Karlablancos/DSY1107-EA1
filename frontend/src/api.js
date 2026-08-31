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

export function obtenerIndicadores(conToken = true) {
  return ejecutar(
    conToken ? 'GET /datos con token' : 'GET /datos SIN token',
    fetch(`${API_URL}/datos`, {
      headers: conToken ? { Authorization: `Bearer ${getAccessToken()}` } : {},
    })
  )
}

export function obtenerIndicadoresPublicos() {
  return ejecutar(
    'GET /publico/datos (ruta sin proteger)',
    fetch(`${API_URL}/publico/datos`)
  )
}