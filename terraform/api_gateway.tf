resource "aws_apigatewayv2_integration" "mindicador" {
  api_id             = aws_apigatewayv2_api.api.id
  integration_type   = "HTTP_PROXY"
  integration_uri    = "https://mindicador.cl/api"
  integration_method = "GET"
}

resource "aws_apigatewayv2_authorizer" "cognito" {
  api_id           = aws_apigatewayv2_api.api.id
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  name             = "cognito-authorizer"

  jwt_configuration {
    audience = [aws_cognito_user_pool_client.spa.id]
    issuer   = "https://cognito-idp.us-east-1.amazonaws.com/${aws_cognito_user_pool.pool.id}"
  }
}

resource "aws_apigatewayv2_route" "datos" {
  api_id             = aws_apigatewayv2_api.api.id
  route_key          = "GET /datos"
  authorization_type = "JWT"
  authorizer_id      = aws_apigatewayv2_authorizer.cognito.id
  target             = "integrations/${aws_apigatewayv2_integration.mindicador.id}"
}

resource "aws_apigatewayv2_route" "publico" {
  api_id             = aws_apigatewayv2_api.api.id
  route_key          = "GET /publico/datos"
  authorization_type = "NONE"
  target             = "integrations/${aws_apigatewayv2_integration.mindicador.id}"
}

resource "aws_apigatewayv2_stage" "desarrollo" {
  api_id      = aws_apigatewayv2_api.api.id
  name        = "desarrollo"
  auto_deploy = true
}

output "api_url" {
  value = "${aws_apigatewayv2_stage.desarrollo.invoke_url}/datos"
}

output "api_url_publica" {
  value = "${aws_apigatewayv2_stage.desarrollo.invoke_url}/publico/datos"
}