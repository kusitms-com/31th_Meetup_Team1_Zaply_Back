package org.zapply.product.domain.posting.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.global.snsClients.facebook.service.FacebookMediaClient;
import org.zapply.product.global.snsClients.facebook.service.FacebookPostingClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookPostingService {
    private final AccountService accountService;
    private final FacebookMediaClient facebookMediaClient;
    private final FacebookPostingClient facebookPostingClient;

    public String getPageAccessToken(Member member) {
        return accountService.getPageToken(member);
    }

    /*
    // 페이스북 미디어를 가져오는 함수
    public String getMediaUrl(Member member) {
        String accessToken = getAccessToken(member);
        String pageId = facebookMediaClient.getPageId(accessToken);
        return facebookMediaClient.getAllPagePosts(member);
    }
     */

    // 페이스북 단일 포스트를 발행하는 함수(text만 포함)
    public String publishOnlyText(Member member, String message) {
        String pageAccessToken = getPageAccessToken(member);
        String pageId = facebookMediaClient.getPageId(pageAccessToken);
        return facebookPostingClient.createSinglePost(pageAccessToken, pageId, message);
    }

    // 페이스북 글, 그림 1개를 발행하는 함수
    public String publishPostWithSinglePhotoAndText(Member member, String photoUrl, String message) {
        String pageAccessToken = getPageAccessToken(member);
        String pageId = facebookMediaClient.getPageId(pageAccessToken);
        return facebookPostingClient.publishSinglePhotoAndPost(pageAccessToken, pageId, photoUrl, message);
    }

    // 페이스북 글, 그림 여러개를 발행하는 함수
    public String publishPostWithMultiPhotoAndText(Member member, String message, List<String> photoUrls) {
        String pageAccessToken = getPageAccessToken(member);
        String pageId = facebookMediaClient.getPageId(pageAccessToken);
        return facebookPostingClient.createPagePost(pageAccessToken, pageId, message, photoUrls);
    }
}
