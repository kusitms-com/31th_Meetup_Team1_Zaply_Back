resource "ncloud_vpc" "this" {
    name            = "${var.name_prefix}-vpc"
    ipv4_cidr_block = var.vpc_cidr
}
