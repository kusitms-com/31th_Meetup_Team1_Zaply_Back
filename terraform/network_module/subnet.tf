resource "ncloud_subnet" "public" {
    name            = "${var.name_prefix}-public"
    vpc_no          = ncloud_vpc.this.vpc_no
    subnet          = cidrsubnet(ncloud_vpc.this.ipv4_cidr_block, 8, 0) # "10.0.0.0/24"
    zone            = var.zone
    network_acl_no  = ncloud_vpc.this.default_network_acl_no
    subnet_type     = "PUBLIC"
}

resource "ncloud_subnet" "private_redis" {
    name            = "${var.name_prefix}-private-redis"
    vpc_no          = ncloud_vpc.this.vpc_no
    subnet          = cidrsubnet(ncloud_vpc.this.ipv4_cidr_block, 8, 3)
    zone            = var.zone_private_redis
    network_acl_no  = ncloud_vpc.this.default_network_acl_no
    subnet_type     = "PRIVATE"
}

// ── PostgreSQL 용 Private Subnet ──
resource "ncloud_subnet" "private_postgres" {
    name            = "${var.name_prefix}-private-postgres"
    vpc_no          = ncloud_vpc.this.vpc_no
    subnet          = cidrsubnet(ncloud_vpc.this.ipv4_cidr_block, 8, 2)
    zone            = var.zone_private_postgres
    network_acl_no  = ncloud_vpc.this.default_network_acl_no
    subnet_type     = "PRIVATE"
}