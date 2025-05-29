variable "name_prefix" {
  description = "리소스 이름 접두사"
  type        = string
}

variable "server_generation" {
  description = "PostgreSQL 서버 세대 (예: G2)"
  type        = string
  default     = "G2"
}

variable "postgresql_version" {
  description = "PostgreSQL 버전 (예: postgresql(14))"
  type        = string
  default     = "postgresql(14)"
}

variable "vpc_no" {
  description = "연결할 VPC 번호"
  type        = string
}

variable "subnet_ids" {
  description = "연결할 Private Subnet ID 리스트"
  type        = list(string)
}

variable "product_code" {
  description = "PostgreSQL 상품코드 (override)"
  type        = string
  default     = ""
}

variable "image_product_code" {
  description = "PostgreSQL 이미지 상품코드 (override)"
  type        = string
  default     = ""
}

variable "data_storage_type" {
  description = "스토리지 타입 (SSD 또는 HDD)"
  type        = string
  default     = "SSD"
}

variable "enable_backup" {
  description = "백업 사용 여부"
  type        = bool
  default     = false
}

variable "enable_high_availability" {
  description = "HA 사용 여부 (Standby)"
  type        = bool
  default     = false
}

variable "server_name_prefix" {
  description = "PostgreSQL Server 이름 접두사"
  type        = string
}

variable "service_name" {
  description = "PostgreSQL 서비스 이름"
  type        = string
  default     = ""
}

variable "port" {
  description = "접속 포트"
  type        = number
  default     = 5432
}

variable "db_name" {
  description = "최초 생성할 데이터베이스 이름"
  type        = string
  default     = "postgres"
}

variable "admin_username" {
  description = "관리자 사용자 이름"
  type        = string
  default     = "postgres"
}

variable "admin_password" {
  description = "관리자 비밀번호"
  type        = string
}