# 1) SSH 키 생성 및 파일 저장
resource "ncloud_login_key" "bastion_key" {
    key_name = "${var.name_prefix}-bastion-key"
}
resource "local_file" "pem" {
    filename = "${ncloud_login_key.bastion_key.key_name}.pem"
    content  = ncloud_login_key.bastion_key.private_key
}

# 2) ACG(Security Group) 설정
resource "ncloud_access_control_group" "bastion_acg" {
    name        = "${var.name_prefix}-acg"
    description = "Bastion 서버용 ACG"
    vpc_no      = var.vpc_no
}
resource "ncloud_access_control_group_rule" "ssh" {
  access_control_group_no = ncloud_access_control_group.bastion_acg.id

  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "22"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "80"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "3000"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "3389"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "443"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8000"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8080"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8200"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8201"
  }

  outbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "1-65535"
  }
}

resource "ncloud_access_control_group_rule" "http" {
  access_control_group_no = ncloud_access_control_group.bastion_acg.id

  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "80"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "22"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "3000"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "3389"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "443"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8000"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8080"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8200"
  }
  inbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "8201"
  }

  outbound {
    protocol   = "TCP"
    ip_block   = "0.0.0.0/0"
    port_range = "1-65535"
  }
}

# 3) Network Interface
resource "ncloud_network_interface" "bastion_nic" {
    name                  = "${var.name_prefix}-nic"
    subnet_no             = var.subnet_public_id
    access_control_groups = [ncloud_access_control_group.bastion_acg.id]
}

# 4) Server 생성
data "ncloud_server_image" "ubuntu" {
    filter {
        name   = "product_name"
        values = ["ubuntu-20.04"]   # Ubuntu 22.04 LTS 선택 :contentReference[oaicite:2]{index=2}
    }
}
data "ncloud_server_product" "spec" {
    server_image_product_code = data.ncloud_server_image.ubuntu.id

    filter {
        name   = "product_code"
        values = ["SSD"]
        regex = true
    }
    filter {
        name   = "cpu_count"
        values = ["2"]
    }
    filter {
        name   = "memory_size"
        values = ["4GB"]
    }
    filter {
        name   = "product_type"
        values = ["HICPU"]
    }
}

resource "ncloud_server" "bastion" {
    name                     = "${var.name_prefix}-bastion"
    subnet_no                = var.subnet_public_id
    server_image_product_code = data.ncloud_server_image.ubuntu.id
    server_product_code      = data.ncloud_server_product.spec.id
    login_key_name           = ncloud_login_key.bastion_key.key_name

    network_interface {
        network_interface_no = ncloud_network_interface.bastion_nic.id
        order                = 0
    }
}

# 5) Public IP 할당
resource "ncloud_public_ip" "bastion_ip" {
    server_instance_no = ncloud_server.bastion.id
    description        = "Bastion Public IP"
}

# 6) 루트 패스워드 추출
data "ncloud_root_password" "root_pw" {
    server_instance_no = ncloud_server.bastion.instance_no
    private_key        = ncloud_login_key.bastion_key.private_key
}
resource "local_file" "root_pw" {
    filename = "${ncloud_server.bastion.name}-root_password.txt"
    content  = data.ncloud_root_password.root_pw.root_password
}
