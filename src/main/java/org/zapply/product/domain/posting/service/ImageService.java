package org.zapply.product.domain.posting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zapply.product.domain.posting.entity.Image;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.project.repository.ImageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

    /**
     * 주어진 Posting 에 대해 media URL 리스트를 Image 엔티티로 변환하여 저장
     */
    @Transactional
    public void saveAllImages(Posting posting, List<String> mediaUrls) {
        List<Image> images = mediaUrls.stream()
                .map(url -> Image.builder()
                        .posting(posting)
                        .imageUrl(url)
                        .build())
                .collect(Collectors.toList());

        imageRepository.saveAll(images);
    }

    /**
     * 주어진 Posting 에 대해 media URL 리스트를 반환
     */
    public List<String> getImagesURLByPosting(Posting Posting) {
        List<Image> images = imageRepository.findAllByPosting(Posting);

        List<String> urls = images.stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        return urls;
    }
}
