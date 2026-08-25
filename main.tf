resource "aws_apigatewayv2_api" "api_manager" {
  name          = "api-mindicador"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["*"]
    allow_methods = ["GET", "OPTIONS"]
    allow_headers = ["Authorization", "Content-Type"]
  }
}

resource "aws_apigatewayv2_integration" "mindicador" {
  api_id             = aws_apigatewayv2_api.api_manager.id
  integration_type   = "HTTP_PROXY"
  integration_method = "GET"
  integration_uri    = "https://mindicador.cl/api"
}

resource "aws_apigatewayv2_route" "datos" {
  api_id    = aws_apigatewayv2_api.api_manager.id
  route_key = "GET /datos"
  target    = "integrations/${aws_apigatewayv2_integration.mindicador.id}"
}

resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.api_manager.id
  name        = "$default"
  auto_deploy = true
}