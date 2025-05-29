resource "ncloud_redis_config_group" "cfg" {
  name          = "tf-ncp-redis-cfg"
  redis_version = "redis(7.2.6)"
}
