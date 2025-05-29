output "endpoint" {
  description = "PostgreSQL 호스트 주소"
  value       = ncloud_postgresql.default.endpoint
}

output "port" {
  description = "PostgreSQL 포트"
  value       = ncloud_postgresql.default.port
}

output "username" {
  description = "관리자 계정"
  value       = var.admin_username
}

output "database" {
  description = "생성된 DB 이름"
  value       = var.db_name
}