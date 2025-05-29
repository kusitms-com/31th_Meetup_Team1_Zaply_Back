terraform {
  required_version = ">= 1.3.0"
  required_providers {
    ncloud = {
      source  = "NaverCloudPlatform/ncloud"
      version = ">= 3.3.1"    # 최신 Provider v3.3.1 사용 :contentReference[oaicite:1]{index=1}
    }
  }
}

provider "ncloud" {
  support_vpc = true
  region      = var.region
  access_key  = var.access_key
  secret_key  = var.secret_key
}

module "network" {
  source        = "./network_module"
  name_prefix   = var.name_prefix
}

module "server" {
  source           = "./server_module"
  name_prefix      = var.name_prefix
  vpc_no           = module.network.vpc_no
  subnet_public_id = module.network.subnet_public_id
}

module "server_green" {
  source           = "./server_module"
  name_prefix      = "${var.name_prefix}-green"   # 똑같은 모듈을 두 번째 호출
  vpc_no           = module.network.vpc_no
  subnet_public_id = module.network.subnet_public_id
}

module "storage" {
  source      = "./storage_module"
  bucket_name = "${var.name_prefix}-storage"
}

module "redis_simple" {
  source     = "./redis_module"
  vpc_no     = module.network.vpc_no
  subnet_redis_id = module.network.subnet_private_redis_id
}