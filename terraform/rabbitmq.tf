# =============================================================================
# RabbitMQ en Docker sobre una EC2.
#
# El Learner Lab no tiene Amazon MQ, así que el broker corre como contenedor
# (la misma imagen que docker-compose.yml) dentro de una instancia EC2.
# El backend en ECS se conecta por IP PRIVADA (misma VPC); el panel de
# administración y los microservicios locales, por IP PÚBLICA.
# =============================================================================

variable "rabbitmq_usuario" {
  description = "Usuario administrador de RabbitMQ (guest solo sirve en localhost)."
  type        = string
  default     = "admin"
}

variable "rabbitmq_password" {
  description = "Contraseña del usuario administrador de RabbitMQ."
  type        = string
  sensitive   = true
  default     = "Rabbit2026!"
}

# Última imagen de Amazon Linux 2023 (x86_64)
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]
  filter {
    name   = "name"
    values = ["al2023-ami-2023*-x86_64"]
  }
}

resource "aws_security_group" "rabbitmq" {
  name        = "dsy1107-rabbitmq-${var.estudiante}"
  description = "AMQP (5672) y panel de administracion (15672) de RabbitMQ"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "AMQP: backend en ECS y microservicios"
    from_port   = 5672
    to_port     = 5672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Panel de administracion web"
    from_port   = 15672
    to_port     = 15672
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "rabbitmq" {
  ami                         = data.aws_ami.al2023.id
  instance_type               = "t3.small"
  subnet_id                   = data.aws_subnets.publicas.ids[0]
  vpc_security_group_ids      = [aws_security_group.rabbitmq.id]
  associate_public_ip_address = true

  # Al arrancar: instala Docker y levanta RabbitMQ con su panel.
  # --restart unless-stopped: si el Learner Lab apaga y prende la EC2,
  # el contenedor vuelve a levantarse solo.
  user_data = <<-EOF
    #!/bin/bash
    dnf install -y docker
    systemctl enable --now docker
    docker run -d --name rabbitmq --restart unless-stopped \
      -p 5672:5672 -p 15672:15672 \
      -e RABBITMQ_DEFAULT_USER='${var.rabbitmq_usuario}' \
      -e RABBITMQ_DEFAULT_PASS='${var.rabbitmq_password}' \
      public.ecr.aws/docker/library/rabbitmq:4-management
  EOF

  tags = {
    Name = "dsy1107-rabbitmq-${var.estudiante}"
  }
}

output "rabbitmq_panel" {
  description = "Panel de administración de RabbitMQ"
  value       = "http://${aws_instance.rabbitmq.public_ip}:15672"
}

output "rabbitmq_ip_publica" {
  description = "Para conectar microservicios que corren fuera de AWS (tu PC)"
  value       = aws_instance.rabbitmq.public_ip
}