package org.zapply.product.global.snsClients.threads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.repository.PostingRepository;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.enuermerate.SNSType;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThreadsInsightClient {

    private final AccountService accountService;

    private static final String THREADS_API_BASE = "https://graph.threads.net/v1.0/";

    private final RestClient restClient = RestClient.create();
    private final PostingRepository postingRepository;

    /**
     * 게시글 인사이트 불러오기
     * @param member
     * @param postId
     */
    public ThreadsInsightResponse getThreadsInsight(Member member, Long postId) {

        // postId에 해당하는 미디어 불러오기
        Posting posting = postingRepository.findById(postId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.POSTING_NOT_FOUND));

        String mediaId = posting.getMediaId();

        // 스레드 계정 불러오기
        String accessToken = accountService.getAccessToken(member, SNSType.THREADS);

        // 스레드 API에 요청하기 위한 URL 생성
        String url = UriComponentsBuilder.fromHttpUrl(THREADS_API_BASE + mediaId + "/insights")
                .queryParam("metric", "likes,replies,reposts,quotes,views,shares")
                .queryParam("access_token", accessToken)
                .toUriString();

        // 스레드 API에 요청하여 인사이트 데이터 가져오기
        return restClient.get()
                .uri(url)
                .retrieve()
                .body(ThreadsInsightResponse.class);
    }
}