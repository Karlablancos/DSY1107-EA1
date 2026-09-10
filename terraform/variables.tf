variable "estudiante" {
  description = "Tu apellido en minusculas."
  type        = string
  default     = "blanco"
  validation {
    condition     = can(regex("^[a-z0-9-]{3,20}$", var.estudiante))
    error_message = "Solo minusculas, numeros y guiones, entre 3 y 20 caracteres."
  }
}

variable "aws_region" {
  description = "Region donde se despliega todo."
  type        = string
  default     = "us-east-1"
}

variable "db_password" {
  description = "Contraseña de la base de datos PostgreSQL"
  type        = string
  sensitive   = true
  default     = "Duoc2026Postgres"
}

variable "callback_urls" {
  description = "URLs de callback del Hosted UI."
  type        = list(string)
  default     = ["http://localhost:5173/", "https://main.d3a2wy9wnw07n7.amplifyapp.com/"]
}

variable "logout_urls" {
  description = "URLs de logout del Hosted UI."
  type        = list(string)
  default     = ["http://localhost:5173/", "https://main.d3a2wy9wnw07n7.amplifyapp.com/"]
}

variable "origenes_frontend" {
  description = "Origenes autorizados por CORS."
  type        = list(string)
  default     = ["http://localhost:5173", "https://main.d3a2wy9wnw07n7.amplifyapp.com"]
}

variable "backend_url" {
  description = "URL temporal del backend. El script la reemplaza con la IP real."
  type        = string
  default     = "https://mindicador.cl"
}