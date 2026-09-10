resource "aws_cognito_user_pool" "pool" {
  name                     = "dsy1107-grupo10"
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = false
  }

  admin_create_user_config {
    allow_admin_create_user_only = true
  }

  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  user_pool_tier = "ESSENTIALS"

  lambda_config {
    pre_token_generation_config {
      lambda_arn     = aws_lambda_function.user_token_ms.arn
      lambda_version = "V2_0"
    }
  }
}

resource "aws_cognito_user_pool_domain" "hosted_ui" {
  domain                = "dsy1107-grupo10"
  user_pool_id          = aws_cognito_user_pool.pool.id
  managed_login_version = 1
}

resource "aws_cognito_user_pool_client" "spa" {
  name         = "spa-react"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret = false

  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  supported_identity_providers         = ["COGNITO"]

  allowed_oauth_scopes = [
    "openid",
    "email",
    "profile",
    "aws.cognito.signin.user.admin",
  ]

  callback_urls = ["http://localhost:5173/", "https://main.d3a2wy9wnw07n7.amplifyapp.com/"]
  logout_urls   = ["http://localhost:5173/", "https://main.d3a2wy9wnw07n7.amplifyapp.com/"]

  access_token_validity  = 60
  id_token_validity      = 60
  refresh_token_validity = 1

  token_validity_units {
    access_token  = "minutes"
    id_token      = "minutes"
    refresh_token = "days"
  }

  explicit_auth_flows           = ["ALLOW_REFRESH_TOKEN_AUTH"]
  prevent_user_existence_errors = "ENABLED"
  enable_token_revocation       = true
}

# Usuario solicitante de prueba
resource "aws_cognito_user" "solicitante" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = "solicitante@muck.cl"
  password     = "Muck2026!"

  attributes = {
    email          = "solicitante@muck.cl"
    email_verified = true
    name           = "Karla Blanco"
  }

  message_action = "SUPPRESS"
}

# Usuario aprobador de prueba
resource "aws_cognito_user" "aprobador" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = "aprobador@muck.cl"
  password     = "Muck2026!"

  attributes = {
    email          = "aprobador@muck.cl"
    email_verified = true
    name           = "Aprobador RRHH"
  }

  message_action = "SUPPRESS"
}

# Resource server con los scopes de gastos
resource "aws_cognito_resource_server" "gastos" {
  user_pool_id = aws_cognito_user_pool.pool.id
  identifier   = "gastos"
  name         = "API de gastos"

  scope {
    scope_name        = "read"
    scope_description = "Consultar gastos"
  }

  scope {
    scope_name        = "write"
    scope_description = "Crear, modificar y eliminar gastos"
  }
}

# Grupos
resource "aws_cognito_user_group" "solicitante" {
  user_pool_id = aws_cognito_user_pool.pool.id
  name         = "solicitante"
  description  = "Puede crear y gestionar sus propios gastos"
}

resource "aws_cognito_user_group" "aprobador" {
  user_pool_id = aws_cognito_user_pool.pool.id
  name         = "aprobador"
  description  = "Puede aprobar o rechazar gastos"
}

# Asignar usuarios a grupos
resource "aws_cognito_user_in_group" "karla_solicitante" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = aws_cognito_user.solicitante.username
  group_name   = aws_cognito_user_group.solicitante.name
}

resource "aws_cognito_user_in_group" "rrhh_aprobador" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = aws_cognito_user.aprobador.username
  group_name   = aws_cognito_user_group.aprobador.name
}