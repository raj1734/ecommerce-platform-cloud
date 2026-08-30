locals {
  services = {
    gateway-service = 8080
    auth-service = 8081
    catalog-service = 8082
    order-service = 8083
    notification-service = 8084
    inventory-service = 8086
    user-service = 8087
    payment-service = 8088
    config-server = 8888
  }
}
resource "aws_ecr_repository" "service" {
  for_each = local.services
  name = "${local.name}/${each.key}"
  image_scanning_configuration { scan_on_push = true }
  force_delete = true
}
resource "aws_iam_role" "ecs_execution" {
  name = "${local.name}-ecs-execution"
  assume_role_policy = jsonencode({Version="2012-10-17",Statement=[{Effect="Allow",Principal={Service="ecs-tasks.amazonaws.com"},Action="sts:AssumeRole"}]})
}
resource "aws_iam_role_policy_attachment" "ecs_execution" {
  role = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}
resource "aws_ecs_task_definition" "service" {
  for_each = local.services
  family = "${local.name}-${each.key}"
  network_mode = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu = each.key == "catalog-service" ? 512 : 256
  memory = each.key == "catalog-service" ? 1024 : 512
  execution_role_arn = aws_iam_role.ecs_execution.arn
  container_definitions = jsonencode([{
    name = each.key
    image = "${aws_ecr_repository.service[each.key].repository_url}:latest"
    essential = true
    portMappings = [{containerPort=each.value,hostPort=each.value,protocol="tcp"}]
    logConfiguration = {logDriver="awslogs",options={awslogs-group=aws_cloudwatch_log_group.ecs.name,awslogs-region=var.aws_region,awslogs-stream-prefix=each.key}}
    environment = [
      {name="SPRING_PROFILES_ACTIVE",value="prod"},
      {name="CONFIG_SERVER_HOST",value="config-server"},
      {name="SPRING_DATASOURCE_USERNAME",value=var.db_username},
      {name="SPRING_DATASOURCE_PASSWORD",value=var.db_password}
    ]
  }])
}
resource "aws_security_group" "alb" {
  name="${local.name}-alb" vpc_id=aws_vpc.main.id
  ingress {from_port=80 to_port=80 protocol="tcp" cidr_blocks=["0.0.0.0/0"]}
  egress {from_port=0 to_port=0 protocol="-1" cidr_blocks=["0.0.0.0/0"]}
}
resource "aws_lb" "gateway" { name="${local.name}-gateway" internal=false load_balancer_type="application" security_groups=[aws_security_group.alb.id] subnets=aws_subnet.public[*].id }
resource "aws_lb_target_group" "gateway" { name="${local.name}-gateway" port=8080 protocol="HTTP" target_type="ip" vpc_id=aws_vpc.main.id health_check {path="/actuator/health" matcher="200"} }
resource "aws_lb_listener" "gateway" { load_balancer_arn=aws_lb.gateway.arn port=80 protocol="HTTP" default_action {type="forward" target_group_arn=aws_lb_target_group.gateway.arn} }
resource "aws_ecs_service" "gateway" {
  name="gateway-service" cluster=aws_ecs_cluster.main.id task_definition=aws_ecs_task_definition.service["gateway-service"].arn desired_count=var.ecs_desired_count launch_type="FARGATE"
  network_configuration {subnets=aws_subnet.private[*].id security_groups=[aws_security_group.ecs.id] assign_public_ip=false}
  load_balancer {target_group_arn=aws_lb_target_group.gateway.arn container_name="gateway-service" container_port=8080}
  depends_on=[aws_lb_listener.gateway]
}
output "gateway_url" { value = "http://${aws_lb.gateway.dns_name}" }
