variable "name_prefix" {
    description = "리소스 접두사 (Resource name prefix)"
}

variable "vpc_no" {
    description = "상위 모듈에서 전달받는 VPC 번호 (VPC ID)"
}

variable "subnet_public_id" {
    description = "상위 모듈에서 전달받는 Public Subnet ID"
}
