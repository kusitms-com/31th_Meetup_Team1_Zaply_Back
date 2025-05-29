package org.zapply.product.domain.posting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zapply.product.domain.posting.dto.request.PostingRequest;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.global.snsClients.instagram.InstagramPostingClient;
import org.zapply.product.global.snsClients.instagram.InstagramPostingResponse;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstagramPostingService {

    private final InstagramPostingClient instagramPostingClient;

    /**
     * 인스타그램 단일 이미지 게시
     *
     * @param member    사용자 정보
     * @param request   게시물 요청 정보
     * @param projectId 프로젝트 ID
     * @return InstagramPostingResponse
     */
    @Transactional
    public InstagramPostingResponse publishInstagramPost(Member member, PostingRequest request, Long projectId) {
        return instagramPostingClient.createSingleMedia(member, request, projectId);
    }

    /**
     * 인스타그램 캐러셀 게시
     *
     * @param member    사용자 정보
     * @param request   게시물 요청 정보
     * @param projectId 프로젝트 ID
     * @return InstagramPostingResponse
     */
    @Transactional
    public InstagramPostingResponse publishInstagramCarousel(Member member, PostingRequest request, Long projectId) {
        return instagramPostingClient.createCarouselMedia(member, request, projectId);
    }
}
