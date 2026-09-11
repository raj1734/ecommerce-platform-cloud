variable "aws_region" { type=string default="ap-south-1" }
variable "project_name" { type=string default="ecommerce-platform" }
variable "vpc_cidr" { type=string default="10.20.0.0/16" }
variable "db_username" { type=string default="ecommerce" sensitive=true }
variable "db_password" { type=string sensitive=true }
variable "ecs_desired_count" { type=number default=1 }
