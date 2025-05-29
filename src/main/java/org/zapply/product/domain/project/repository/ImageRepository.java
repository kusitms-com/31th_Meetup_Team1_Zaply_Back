package org.zapply.product.domain.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zapply.product.domain.posting.entity.Image;
import org.zapply.product.domain.posting.entity.Posting;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByPosting(Posting posting);
}
