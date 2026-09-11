output "vpc_id" { value=aws_vpc.main.id }
output "ecs_cluster_name" { value=aws_ecs_cluster.main.name }
output "rds_endpoint" { value=aws_db_instance.postgres.address }
output "msk_bootstrap_brokers" { value=aws_msk_cluster.kafka.bootstrap_brokers }
