resource "aws_apigatewayv2_api" "api" {
  name          = "dsy1107-api-${var.estudiante}"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["http://localhost:5173", "https://main.d3a2wy9wnw07n7.amplifyapp.com"]
    allow_methods = ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
    allow_headers = ["authorization", "content-type", "x-usuario-id"]
  }
}