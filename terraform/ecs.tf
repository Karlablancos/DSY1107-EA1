data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "publicas" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "map-public-ip-on-launch"
    values = ["true"]
  }
}

data "aws_route_table" "principal" {
  vpc_id = data.aws_vpc.default.id
  filter {
    name   = "association.main"
    values = ["true"]
  }
}

data "aws_internet_gateway" "default" {
  filter {
    name   = "attachment.vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_caller_identity" "actual" {}

resource "aws_route" "salida_a_internet" {
  route_table_id         = data.aws_route_table.principal.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = data.aws_internet_gateway.default.id

  lifecycle {
    ignore_changes = [gateway_id]
  }
}

resource "aws_ecr_repository" "backend" {
  name         = "dsy1107-backend-${var.estudiante}"
  force_delete = true
  image_scanning_configuration {
    scan_on_push = false
  }
}

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/dsy1107-backend-${var.estudiante}"
  retention_in_days = 7
}

resource "aws_security_group" "tarea" {
  name        = "dsy1107-ecs-${var.estudiante}"
  description = "Permite llamadas al puerto 8080"

  ingress {
    from_port   = 8080
    to_port     = 8080
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

resource "aws_ecs_cluster" "backend" {
  name = "dsy1107-backend-${var.estudiante}"
}

resource "aws_ecs_task_definition" "backend" {
  family                   = "dsy1107-backend-${var.estudiante}"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = "arn:aws:iam::${data.aws_caller_identity.actual.account_id}:role/LabRole"

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([{
    name  = "backend"
    image = "${aws_ecr_repository.backend.repository_url}:latest"

    environment = [
      { name = "SPRING_DATASOURCE_URL",      value = "jdbc:postgresql://${aws_db_instance.postgres.endpoint}/gastos?sslmode=require" },
      { name = "SPRING_DATASOURCE_USERNAME", value = "postgres" },
      { name = "SPRING_DATASOURCE_PASSWORD", value = var.db_password }
    ]

    portMappings = [{ containerPort = 8080 }]

    healthCheck = {
      command     = ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"]
      interval    = 30
      timeout     = 5
      retries     = 3
      startPeriod = 60
    }

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        awslogs-group         = aws_cloudwatch_log_group.backend.name
        awslogs-region        = var.aws_region
        awslogs-stream-prefix = "ecs"
      }
    }
  }])
}

resource "aws_ecs_service" "backend" {
  name            = "dsy1107-backend-${var.estudiante}"
  cluster         = aws_ecs_cluster.backend.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.publicas.ids
    security_groups  = [aws_security_group.tarea.id]
    assign_public_ip = true
  }

  lifecycle {
    ignore_changes = [task_definition]
  }

  wait_for_steady_state = false
}