resource "ncloud_redis" "simple" {
  server_name_prefix       = var.name_prefix
  service_name             = "redis"

  redis_mode               = "SIMPLE"
  vpc_no                   = var.vpc_no
  subnet_ids               = var.subnet_redis_id

  product_code             = data.ncloud_redis_products.products.products[0].product_code
  image_product_code       = data.ncloud_redis_image_products.images.image_product_code

  data_storage_type        = "SSD"
  enable_high_availability = false
  enable_backup            = false
  license_model            = "BSD"
  target_config_group_name = ncloud_redis_config_group.cfg.name
  port                     = 6379
}
