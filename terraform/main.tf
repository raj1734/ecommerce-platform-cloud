locals { name=var.project_name; azs=["${var.aws_region}a","${var.aws_region}b"] }
resource "aws_vpc" "main" { cidr_block=var.vpc_cidr enable_dns_hostnames=true enable_dns_support=true tags={Name="${local.name}-vpc"} }
resource "aws_internet_gateway" "igw" { vpc_id=aws_vpc.main.id }
resource "aws_subnet" "public" { count=2 vpc_id=aws_vpc.main.id cidr_block=cidrsubnet(var.vpc_cidr,4,count.index) availability_zone=local.azs[count.index] map_public_ip_on_launch=true tags={Name="${local.name}-public-${count.index+1}"} }
resource "aws_subnet" "private" { count=2 vpc_id=aws_vpc.main.id cidr_block=cidrsubnet(var.vpc_cidr,4,count.index+8) availability_zone=local.azs[count.index] tags={Name="${local.name}-private-${count.index+1}"} }
resource "aws_route_table" "public" { vpc_id=aws_vpc.main.id route {cidr_block="0.0.0.0/0" gateway_id=aws_internet_gateway.igw.id} }
resource "aws_route_table_association" "public" { count=2 subnet_id=aws_subnet.public[count.index].id route_table_id=aws_route_table.public.id }
resource "aws_eip" "nat" { domain="vpc" }
resource "aws_nat_gateway" "nat" { allocation_id=aws_eip.nat.id subnet_id=aws_subnet.public[0].id depends_on=[aws_internet_gateway.igw] }
resource "aws_route_table" "private" { vpc_id=aws_vpc.main.id route {cidr_block="0.0.0.0/0" nat_gateway_id=aws_nat_gateway.nat.id} }
resource "aws_route_table_association" "private" { count=2 subnet_id=aws_subnet.private[count.index].id route_table_id=aws_route_table.private.id }
resource "aws_security_group" "ecs" { name="${local.name}-ecs" vpc_id=aws_vpc.main.id ingress {from_port=8080 to_port=8080 protocol="tcp" cidr_blocks=["0.0.0.0/0"]} ingress {from_port=9092 to_port=9098 protocol="tcp" self=true} egress {from_port=0 to_port=0 protocol="-1" cidr_blocks=["0.0.0.0/0"]} }
resource "aws_security_group" "rds" { name="${local.name}-rds" vpc_id=aws_vpc.main.id ingress {from_port=5432 to_port=5432 protocol="tcp" security_groups=[aws_security_group.ecs.id]} egress {from_port=0 to_port=0 protocol="-1" cidr_blocks=["0.0.0.0/0"]} }
resource "aws_db_subnet_group" "postgres" { name="${local.name}-postgres" subnet_ids=aws_subnet.private[*].id }
resource "aws_db_instance" "postgres" { identifier="${local.name}-postgres" engine="postgres" engine_version="15" instance_class="db.t4g.micro" allocated_storage=20 db_name="platform" username=var.db_username password=var.db_password db_subnet_group_name=aws_db_subnet_group.postgres.name vpc_security_group_ids=[aws_security_group.rds.id] publicly_accessible=false skip_final_snapshot=true }
resource "aws_msk_cluster" "kafka" { cluster_name="${local.name}-kafka" kafka_version="3.6.0" number_of_broker_nodes=2 broker_node_group_info { instance_type="kafka.t3.small" client_subnets=aws_subnet.private[*].id security_groups=[aws_security_group.ecs.id] storage_info { ebs_storage_info { volume_size=20 } } } }
resource "aws_ecs_cluster" "main" { name=local.name setting {name="containerInsights" value="enabled"} }
resource "aws_cloudwatch_log_group" "ecs" { name="/ecs/${local.name}" retention_in_days=14 }
