output "user_pool_id" {
  description = "Identificador del user pool."
  value       = aws_cognito_user_pool.pool.id
}

output "client_id" {
  description = "Client ID de la SPA."
  value       = aws_cognito_user_pool_client.spa.id
}

output "dominio_cognito" {
  value = "https://${aws_cognito_user_pool_domain.hosted_ui.domain}.auth.${var.aws_region}.amazoncognito.com"
}

output "issuer" {
  value = "https://${aws_cognito_user_pool.pool.endpoint}"
}

output "ecs_cluster" {
  value = aws_ecs_cluster.backend.name
}

output "ecs_repositorio" {
  value = aws_ecr_repository.backend.repository_url
}

output "api_id" {
  value = aws_apigatewayv2_api.api.id
}

output "integracion_gastos_coleccion_id" {
  value = aws_apigatewayv2_integration.gastos_coleccion.id
}

output "integracion_gastos_elemento_id" {
  value = aws_apigatewayv2_integration.gastos_elemento.id
}

output "url_gastos" {
  value = "${aws_apigatewayv2_api.api.api_endpoint}/gastos"
}

output "probar_sin_token" {
  value = "curl -s -o /dev/null -w 'HTTP %%{http_code}\\n' ${aws_apigatewayv2_api.api.api_endpoint}/gastos"
}

output "backend_env" {
  sensitive = true
  value     = <<-EOT
    SPRING_DATASOURCE_URL=jdbc:postgresql://${aws_db_instance.postgres.endpoint}/gastos?sslmode=require
    SPRING_DATASOURCE_USERNAME=postgres
    SPRING_DATASOURCE_PASSWORD=${var.db_password}
  EOT
}

output "env_frontend" {
  value = <<-EOT
    FE_AWS_REGION=${var.aws_region}
    FE_COGNITO_DOMAIN=https://${aws_cognito_user_pool_domain.hosted_ui.domain}.auth.${var.aws_region}.amazoncognito.com
    FE_COGNITO_CLIENT_ID=${aws_cognito_user_pool_client.spa.id}
    FE_REDIRECT_URI=${var.callback_urls[0]}
    FE_API_URL=${aws_apigatewayv2_api.api.api_endpoint}
  EOT
}