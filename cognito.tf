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
}

resource "aws_cognito_user_pool_domain" "hosted_ui" {
  domain               = "dsy1107-grupo10"
  user_pool_id         = aws_cognito_user_pool.pool.id
  managed_login_version = 1
}
resource "aws_cognito_user_pool_client" "spa" {
  name                                 = "spa-react"
  user_pool_id                         = aws_cognito_user_pool.pool.id
  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  supported_identity_providers         = ["COGNITO"]
  allowed_oauth_scopes                 = ["openid", "email", "profile"]
  callback_urls                        = ["http://localhost:5173/"]
  logout_urls                          = ["http://localhost:5173/"]
  explicit_auth_flows                  = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]
  access_token_validity                = 60
  id_token_validity                    = 60

  token_validity_units {
    access_token = "minutes"
    id_token     = "minutes"
  }
}