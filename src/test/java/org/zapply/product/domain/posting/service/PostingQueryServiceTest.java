package org.zapply.product.domain.posting.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zapply.product.domain.posting.dto.response.CursorSlice;
import org.zapply.product.domain.posting.dto.response.PostingDetailResponse;
import org.zapply.product.domain.posting.dto.response.PostingInfoResponse;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.repository.PostingRepository;
import org.zapply.product.domain.project.repository.ProjectRepository;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.snsClients.threads.ThreadsMediaResponse;
import org.zapply.product.global.snsClients.threads.ThreadsMediaResponse.ThreadsMedia;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.global.snsClients.threads.ThreadsMediaClient;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PostingQueryServiceTest {

    @Mock private ThreadsMediaClient threadsMediaClient;
    @Mock private AccountService accountService;
    @Mock private PostingRepository postingRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ImageService imageService;

    @InjectMocks private PostingQueryService service;

    @Test
    void whenProjectNotBelongToMember_thenThrowsIsNotUserProject() {
        Long projectId = 1L;
        Member member = mock(Member.class);
        given(member.getId()).willReturn(10L);
        given(projectRepository.existsByProjectIdAndMember_Id(projectId, 10L)).willReturn(false);

        CoreException ex = assertThrows(CoreException.class,
                () -> service.getPostings(member, projectId));

        assertThat(ex.getErrorType()).isEqualTo(GlobalErrorType.IS_NOT_USER_PROJECT);
    }

    @Test
    void whenNoPostings_thenThrowsPostingNotFound() {
        Long projectId = 2L;
        Member member = mock(Member.class);
        given(member.getId()).willReturn(20L);
        given(projectRepository.existsByProjectIdAndMember_Id(projectId, 20L)).willReturn(true);
        given(postingRepository.findAllByProject_ProjectIdAndDeletedAtIsNull(projectId))
                .willReturn(Collections.emptyList());

        CoreException ex = assertThrows(CoreException.class,
                () -> service.getPostings(member, projectId));

        assertThat(ex.getErrorType()).isEqualTo(GlobalErrorType.POSTING_NOT_FOUND);
    }

    @Test
    void whenValidProjectAndPostings_thenReturnsPostingInfoResponses() {
        Long projectId = 3L;
        Member member = mock(Member.class);
        given(member.getId()).willReturn(30L);
        given(projectRepository.existsByProjectIdAndMember_Id(projectId, 30L)).willReturn(true);

        Posting posting = mock(Posting.class);
        given(posting.getPostingId()).willReturn(100L);
        given(posting.getPostingContent()).willReturn("Content");
        given(posting.getPostingType()).willReturn(SNSType.THREADS);
        given(posting.getPostingState()).willReturn(PostingState.POSTED);
        given(posting.getPostingLink()).willReturn("http://example.com");
        given(postingRepository.findAllByProject_ProjectIdAndDeletedAtIsNull(projectId))
                .willReturn(List.of(posting));

        List<String> urls = List.of("url1", "url2");
        given(imageService.getImagesURLByPosting(posting)).willReturn(urls);

        List<PostingInfoResponse> dtos = service.getPostings(member, projectId);

        assertThat(dtos).hasSize(1);
        PostingInfoResponse dto = dtos.get(0);
        assertThat(dto.postingId()).isEqualTo(100L);
        assertThat(dto.postingContent()).isEqualTo("Content");
        assertThat(dto.postingType()).isEqualTo(SNSType.THREADS);
        assertThat(dto.postingState()).isEqualTo(PostingState.POSTED);
        assertThat(dto.postingLink()).isEqualTo("http://example.com");
        assertThat(dto.postingImages()).containsExactlyElementsOf(urls);
    }

    @Test
    void getAllThreadsMedia_callsClientAndReturnsList() {
        Member member = mock(Member.class);
        given(accountService.getAccessToken(member, SNSType.THREADS)).willReturn("token123");

        ThreadsMedia media = ThreadsMedia.builder()
                .id("media1")
                .media_product_type("THREADS")
                .media_type("TEXT")
                .permalink("http://link.com")
                .owner(new ThreadsMedia.Owner("ownerId"))
                .username("user1")
                .text("text")
                .timestamp("2025-05-22T12:00:00Z")
                .thumbnail_url("thumbUrl")
                .shortcode("code")
                .is_quote_post(false)
                .children(new ThreadsMedia.Children(List.of(new ThreadsMedia.Child("child1"))))
                .alt_text("alt")
                .media_url("mediaUrl")
                .carousel_media_urls(List.of("c1", "c2"))
                .quoted_post(null)
                .reposted_post(null)
                .link_attachment_url(null)
                .gif_url(null)
                .build();
        List<ThreadsMedia> mediaList = List.of(media);
        ThreadsMediaResponse response = new ThreadsMediaResponse(mediaList, null);
        given(threadsMediaClient.getAllThreadsMedia("token123", null, 6))
                .willReturn(response);
        CursorSlice<PostingDetailResponse> result = service.getAllThreadsMedia(null, 6, member);

        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void getSingleThreadsMedia_callsClientAndReturnsItem() {
        Member member = mock(Member.class);
        given(accountService.getAccessToken(member, SNSType.THREADS)).willReturn("tokenXYZ");

        ThreadsMedia item = ThreadsMedia.builder()
                .id("media2")
                .media_product_type("THREADS")
                .media_type("IMAGE")
                .permalink("http://link2.com")
                .owner(new ThreadsMedia.Owner("owner2"))
                .username("user2")
                .text("text2")
                .timestamp("2025-05-22T13:00:00Z")
                .thumbnail_url("thumb2")
                .shortcode("code2")
                .is_quote_post(true)
                .children(new ThreadsMedia.Children(List.of(new ThreadsMedia.Child("child2"))))
                .alt_text("alt2")
                .media_url("mediaUrl2")
                .carousel_media_urls(List.of())
                .quoted_post("qPost")
                .reposted_post("rPost")
                .link_attachment_url("linkUrl")
                .gif_url("gifUrl")
                .build();
        ThreadsMediaResponse response = new ThreadsMediaResponse(List.of(item), null);
        given(threadsMediaClient.getSingleThreadsMedia("tokenXYZ", "media2"))
                .willReturn(response.data().get(0));
        PostingDetailResponse result = service.getSingleThreadsMedia( member, "media2");
        assertThat(result.postingContent()).isEqualTo("text2");
    }
}
