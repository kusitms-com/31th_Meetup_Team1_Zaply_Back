resource "ncloud_postgresql" "default" {
  server_name_prefix = var.server_name_prefix
  service_name       = var.service_name != "" ? var.service_name : var.server_name_prefix

  vpc_no             = var.vpc_no
  subnet_ids         = var.subnet_ids

  # 상품 및 이미지 코드 지정
  product_code       = var.product_code != "" ? var.product_code : data.ncloud_postgresql_products.products.products[0].product_code
  image_product_code = var.image_product_code != "" ? var.image_product_code : data.ncloud_postgresql_image_products.images.image_product_code

  data_storage_type  = var.data_storage_type
  enable_backup      = var.enable_backup
  enable_high_availability = var.enable_high_availability

  # 초기 접속 설정
  port               = var.port
  database_name      = var.db_name
  username           = var.admin_username
  password           = var.admin_password
}