resource "aws_apigatewayv2_integration" "backend" {
  api_id                 = aws_apigatewayv2_api.api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "GET"
  integration_uri        = "${var.backend_url}/gastos"
  payload_format_version = "1.0"
  timeout_milliseconds   = 29000

  lifecycle {
    ignore_changes = [integration_uri]
  }
}

resource "aws_apigatewayv2_integration" "gastos_coleccion" {
  api_id                 = aws_apigatewayv2_api.api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "${var.backend_url}/gastos"
  payload_format_version = "1.0"
  timeout_milliseconds   = 29000

  lifecycle {
    ignore_changes = [integration_uri]
  }
}

resource "aws_apigatewayv2_integration" "gastos_elemento" {
  api_id                 = aws_apigatewayv2_api.api.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "${var.backend_url}/gastos/{proxy}"
  payload_format_version = "1.0"
  timeout_milliseconds   = 29000

  lifecycle {
    ignore_changes = [integration_uri]
  }
}

resource "aws_apigatewayv2_authorizer" "cognito" {
  api_id           = aws_apigatewayv2_api.api.id
  name             = "cognito-jwt"
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]

  jwt_configuration {
    audience = [aws_cognito_user_pool_client.spa.id]
    issuer   = "https://${aws_cognito_user_pool.pool.endpoint}"
  }
}

locals {
  rutas_gastos = {
    "GET /gastos"             = { scope = "gastos/read",  integracion = aws_apigatewayv2_integration.gastos_coleccion.id }
    "POST /gastos"            = { scope = "gastos/write", integracion = aws_apigatewayv2_integration.gastos_coleccion.id }
    "GET /gastos/{proxy+}"    = { scope = "gastos/read",  integracion = aws_apigatewayv2_integration.gastos_elemento.id }
    "PUT /gastos/{proxy+}"    = { scope = "gastos/write", integracion = aws_apigatewayv2_integration.gastos_elemento.id }
    "PATCH /gastos/{proxy+}"  = { scope = "gastos/write", integracion = aws_apigatewayv2_integration.gastos_elemento.id }
    "DELETE /gastos/{proxy+}" = { scope = "gastos/write", integracion = aws_apigatewayv2_integration.gastos_elemento.id }
  }
}

resource "aws_apigatewayv2_route" "gastos" {
  for_each = local.rutas_gastos

  api_id    = aws_apigatewayv2_api.api.id
  route_key = each.key
  target    = "integrations/${each.value.integracion}"

  authorization_type   = "JWT"
  authorizer_id        = aws_apigatewayv2_authorizer.cognito.id
  authorization_scopes = [each.value.scope]
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.api.id
  name        = "$default"
  auto_deploy = true
}