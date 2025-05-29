output "redis_simple_endpoint" {
  value = ncloud_redis.simple
}

output "redis_simple_port" {
  value = ncloud_redis.simple.port
}
