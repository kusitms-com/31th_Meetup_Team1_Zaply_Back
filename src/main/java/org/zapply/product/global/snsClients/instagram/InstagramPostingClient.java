package org.zapply.product.global.snsClients.instagram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.posting.dto.request.PostingRequest;
import org.zapply.product.domain.posting.entity.Posting;
import org.zapply.product.domain.posting.enumerate.PostingState;
import org.zapply.product.domain.posting.repository.PostingRepository;
import org.zapply.product.domain.project.entity.Project;
import org.zapply.product.domain.project.repository.ProjectRepository;
import org.zapply.product.domain.user.entity.Account;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.AccountRepository;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.clova.enuermerate.SNSType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramPostingClient {

    private static final String INSTAGRAM_API_BASE = "https://graph.facebook.com/v22.0";

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final ProjectRepository projectRepository;
    private final PostingRepository postingRepository;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Instagram 계정 불러오기
     *
     * @param member
     * @return account
     */
    public Account getInstagramAccount(Member member) {
        return accountRepository.findByAccountTypeAndMember(SNSType.INSTAGRAM, member)
                .orElseThrow(() -> new CoreException(GlobalErrorType.ACCOUNT_NOT_FOUND));
    }

    /**
     * Instagram API에 요청하기 위한 액세스 토큰 가져오기
     *
     * @param member
     * @return accessToken
     */
    private String getAccessToken(Member member) {
        return accountService.getAccessToken(member, SNSType.INSTAGRAM);
    }

    /**
     * Instagram에 미디어 컨테이너 생성하기
     *
     * @param userId
     * @param accessToken
     * @param imageUrl
     * @param caption
     * @return mediaContainerId
     */
    public String createSingleMediaContainer(String userId, String accessToken, String imageUrl, String caption) {
        String url = INSTAGRAM_API_BASE + "/" + userId + "/media"; // POST 대상 엔드포인트

        Map<String, Object> body = new HashMap<>();
        body.put("ig_id", userId);
        body.put("access_token", accessToken);
        body.put("image_url", imageUrl);
        body.put("caption", caption);
        body.put("access_token", accessToken);

        log.info("Instagram API URL: {}", url);
        try {
            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> response = objectMapper.readValue(rawResponse, Map.class);

            log.info("Instagram API Response: {}", response);
            if (response == null || response.isEmpty()) {
                throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
            }

            return response.get("id").toString();

        } catch (Exception e) {
            log.error("Error creating media container: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /**
     * Instagram 캐러셀 컨테이너 생성하기
     *
     * @param userId      Instagram User ID
     * @param accessToken 액세스 토큰
     * @param caption     게시글 내용
     * @param childrenIds 미디어 컨테이너 ID 목록 (최대 10개)
     * @return 캐러셀 컨테이너 ID
     */
    public String createCarouselContainer(String userId, String accessToken, String caption, List<String> childrenIds) {
        String url = INSTAGRAM_API_BASE + "/" + userId + "/media";

        // children 파라미터: 쉼표로 구분된 ID 문자열 생성
        String children = String.join(",", childrenIds);

        Map<String, Object> body = new HashMap<>();
        body.put("media_type", "CAROUSEL");
        body.put("caption", caption);
        body.put("children", children);
        body.put("access_token", accessToken);

        log.info("Instagram Carousel Container API URL: {}", url);
        log.info("Instagram Carousel Container Body: {}", body);

        try {
            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> response = objectMapper.readValue(rawResponse, Map.class);

            log.info("Instagram Carousel Container Response: {}", response);
            if (response == null || !response.containsKey("id")) {
                throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
            }

            return response.get("id").toString();
        } catch (Exception e) {
            log.error("Error creating carousel container: {}", e.getMessage(), e);
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /**
     * Instagram에 미디어 컨테이너를 게시하기
     *
     * @param userId        Instagram User ID
     * @param accessToken   액세스 토큰
     * @param creationId    생성된 미디어 컨테이너 ID (단일 이미지 또는 캐러셀 컨테이너 ID)
     * @return mediaId 게시된 인스타그램 미디어 ID
     */
    public String publishMediaContainer(String userId, String accessToken, String creationId) {
        String url = INSTAGRAM_API_BASE + "/" + userId + "/media_publish";

        Map<String, Object> body = new HashMap<>();
        body.put("creation_id", creationId);
        body.put("access_token", accessToken);

        log.info("Instagram Media Publish API URL: {}", url);
        log.info("Instagram Media Publish Body: {}", body);

        try {
            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            Map<String, Object> response = objectMapper.readValue(rawResponse, Map.class);
            log.info("Instagram Media Publish Response: {}", response);

            if (response != null && response.containsKey("id")) {
                return response.get("id").toString();
            } else {
                throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
            }

        } catch (Exception e) {
            log.error("Unexpected error publishing media container: {}", e.getMessage(), e);
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /** 인스타그램 게시물 저장하기
     * @param mediaId 인스타그램 미디어 ID
     * @param projectId 프로젝트 ID
     * @param mediaType 미디어 타입
     * @param media 미디어 URL
     * @param text 게시물 내용
     * @return 게시물 ID
     */
    private InstagramPostingResponse savePosting(String mediaId, Long projectId, String mediaType, String media, String text) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CoreException(GlobalErrorType.PROJECT_NOT_FOUND));
        Posting posting = Posting.builder()
                .postingLink("")
                .postingType(SNSType.THREADS)
                .postingState(PostingState.POSTED)
                .project(project)
                .mediaId(mediaId)
                .build();
        postingRepository.save(posting);
        return InstagramPostingResponse.of(posting, mediaType, media, text);
    }

    /**
     * Instagram에 미디어 만들기
     *
     * @param member
     * @param request
     * @param projectId
     */
    public InstagramPostingResponse createSingleMedia(Member member, PostingRequest request, Long projectId)  {
        Account account = getInstagramAccount(member);
        String accessToken = getAccessToken(member);

        String mediaContainerId = createSingleMediaContainer(account.getUserId(), accessToken, request.media().get(0), request.text());

        // 미디어 컨테이너 게시하기
        String publishedId = publishMediaContainer(account.getUserId(), accessToken, mediaContainerId);

        // 게시글 url 가져오기
        String postingUrl = getPostingUrl(account.getUserId(), publishedId);

        return savePosting(publishedId, projectId, "IMAGE", postingUrl, request.text());
    }

    /**
     * Instagram 게시물 URL 가져오기
     * @param mediaId
     * @param accessToken
     * @return 게시물 URL
     */
    public String getPostingUrl(String mediaId, String accessToken){
        try {
            String uri = UriComponentsBuilder.fromHttpUrl(INSTAGRAM_API_BASE)
                    .pathSegment("/{mediaId}", mediaId)
                    .queryParam("fields", "id,media_type,media_url,owner,timestamp,caption,permalink,username")
                    .queryParam("access_token", accessToken)
                    .toUriString();

            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            return String.valueOf(responseMap.get("permalink"));
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON response: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        } catch (Exception e) {
            log.error("Error getting posting URL: {}", e.getMessage());
            throw new CoreException(GlobalErrorType.INSTAGRAM_API_ERROR);
        }
    }

    /**
     * Instagram에 캐러셀 미디어 만들기
     *
     * @param member
     * @param request
     * @param projectId
     */
    public InstagramPostingResponse createCarouselMedia(Member member, PostingRequest request, Long projectId) {
        Account account = getInstagramAccount(member);
        String accessToken = getAccessToken(member);

        // 미디어 컨테이너 생성
        List<String> mediaContainerIds = request.media().stream()
                .map(url -> createSingleMediaContainer(account.getUserId(), accessToken, url, null))
                .toList();
        // 캐러셀 컨테이너 생성
        String carouselContainerId = createCarouselContainer(account.getUserId(), accessToken, request.text(), mediaContainerIds);

        // 캐러셀 컨테이너 게시하기
        String publishedId = publishMediaContainer(account.getUserId(), accessToken, carouselContainerId);

        // 게시글 url 가져오기
        String postingUrl = getPostingUrl(account.getUserId(), publishedId);

        return savePosting(publishedId, projectId, "CAROUSEL", postingUrl, request.text());
    }

}