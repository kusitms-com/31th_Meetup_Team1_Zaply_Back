output "vpc_no" {
    description = "생성된 VPC 번호"
    value       = ncloud_vpc.this.vpc_no
}
output "subnet_public_id" {
    description = "생성된 퍼블랫 서브넷 ID"
    value       = ncloud_subnet.public.id
}
output "subnet_private_redis_id" {
    description = "생성된 Redis 전용 Private 서브넷 ID"
    value       = ncloud_subnet.private_redis.id
}

output "subnet_private_postgres_id" {
    description = "생성된 PostgreSQL 전용 Private 서브넷 ID"
    value       = ncloud_subnet.private_postgres.id
}