# AWS Terraform

This stack provisions the Week-1/Week-3 foundation: VPC, public/private subnets, NAT, PostgreSQL RDS, Amazon MSK, ECS Fargate cluster, and CloudWatch logs.

1. Copy `terraform.tfvars.example` to `terraform.tfvars` and set a strong DB password.
2. `terraform init`
3. `terraform validate`
4. `terraform plan`
5. `terraform apply`

The application task definitions/ECR repositories are intentionally kept separate so CI/CD can publish images before ECS services are created. Do not commit `terraform.tfvars` or credentials.
