resource "aws_db_instance" "postgres" {
  identifier        = "dsy1107-db-${var.estudiante}"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = "db.t3.micro"
  allocated_storage = 20

  db_name  = "gastos"
  username = "postgres"
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.rds.id]
  skip_final_snapshot    = true
  publicly_accessible    = true

  tags = {
    Name = "dsy1107-db-${var.estudiante}"
  }
}

resource "aws_security_group" "rds" {
  name        = "dsy1107-rds-${var.estudiante}"
  description = "Acceso PostgreSQL para ECS y desarrollo local"

  ingress {
    from_port   = 5432
    to_port     = 5432
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