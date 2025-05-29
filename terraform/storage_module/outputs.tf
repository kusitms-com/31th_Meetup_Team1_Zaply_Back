output "bucket_name" {
  description = "생성된 버킷 이름"
  value       = ncloud_objectstorage_bucket.bucket.bucket_name
}