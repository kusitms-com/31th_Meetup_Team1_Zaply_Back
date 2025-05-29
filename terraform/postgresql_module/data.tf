data "ncloud_postgresql_image_products" "images" {
  filter {
    name   = "postgresqlVersion"
    values = [var.postgresql_version]
  }
}

// PostgreSQL 상품 코드 조회 (Data Source)
data "ncloud_postgresql_products" "products" {
  postgresql_image_product_code = data.ncloud_postgresql_image_products.images.image_product_code
  filter {
    name   = "serverGenerationType"
    values = [var.server_generation]
  }
  filter {
    name   = "postgresqlVersion"
    values = [var.postgresql_version]
  }
}