data "ncloud_redis_image_products" "images" {
  filter {
    name   = "redisVersion"
    values = ["redis(7.2.6)"]
  }
}

data "ncloud_redis_products" "products" {
  redis_image_product_code = data.ncloud_redis_image_products.images.image_product_code
  filter {
    name   = "serverGenerationType"
    values = ["G2"]
  }
  filter {
    name   = "redisVersion"
    values = ["redis(7.2.6)"]
  }
}
