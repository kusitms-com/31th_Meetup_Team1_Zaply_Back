package org.zapply.product.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zapply.product.domain.user.entity.Credential;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
}