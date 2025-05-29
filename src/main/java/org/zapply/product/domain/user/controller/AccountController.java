package org.zapply.product.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zapply.product.global.clova.enuermerate.SNSType;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import org.zapply.product.global.snsClients.instagram.InstagramClient;
import org.zapply.product.global.snsClients.linkedin.LinkedinClient;
import org.zapply.product.global.security.AuthDetails;
import org.zapply.product.global.snsClients.threads.ThreadsClient;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/v1/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final ThreadsClient threadsClient;
    private final LinkedinClient linkedinClient;
    private final InstagramClient instagramClient;


    @GetMapping("/facebook/link")
    @Operation(summary = "페이스북 액세스 토큰 발급", description = "페이스북 액세스 토큰 발급 (계정연동 API에서 연결되는 URL)")
    public ApiResponse<String> linkFacebook(@RequestParam("access_token") String accessToken,
                                            @AuthenticationPrincipal AuthDetails authDetails,
                                            HttpServletResponse response) throws IOException{
        accountService.linkFacebook(accessToken, authDetails.getMember().getId());
        return ApiResponse.success("계정 연동 성공");
    }

    @GetMapping("/threads/login")
    @Operation(summary = "스레드 계정연동", description = "스레드 계정연동 생성 (accessToken 필요)")
    public void loginThreads(HttpServletResponse response,
                             @AuthenticationPrincipal AuthDetails authDetails) throws IOException {
        System.out.println(threadsClient.buildAuthorizationUri(authDetails.getMember().getId()));
        response.sendRedirect(threadsClient.buildAuthorizationUri(authDetails.getMember().getId()));
    }

    @GetMapping("/threads/link")
    @Operation(summary = "스레드 액세스 토큰 발급", description = "스레드 액세스 토큰 발급 (계정연동 API에서 연결되는 URL)")
    public void signInWithThreads(@RequestParam("code") String code, @RequestParam(value="state") Long memberId,
                                  HttpServletResponse response) throws IOException{
        accountService.linkThreads(code, memberId);
        String redirectUrl = "http://localhost:3000/threads/callback";
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/linkedin/login")
    @Operation(summary = "LinkedIn 계정연동", description = "LinkedIn 로그인 URL 생성 및 리다이렉트")
    public void loginLinkedin(HttpServletResponse response,
                              @AuthenticationPrincipal AuthDetails authDetails) throws IOException {
        String uri = linkedinClient.buildAuthorizationUri(authDetails.getMember().getId());
        log.info("LinkedIn login URL: {}", uri);
        response.sendRedirect(uri);
    }

    @GetMapping("/linkedin/link")
    @Operation(summary = "LinkedIn 액세스 토큰 발급", description = "LinkedIn 인가 코드로 액세스 토큰을 받고 계정에 연결")
    public void signInWithLinkedin(@RequestParam("code") String code,
                                   @RequestParam("state") Long memberId,
                                   HttpServletResponse response) throws IOException {
        accountService.linkLinkedin(code, memberId);
        response.sendRedirect("http://localhost:3000/linkedin/callback");
    }


    @GetMapping("/{snsType}/unlink")
    @Operation(summary = "계정연동 해제", description = "스레드 계정연동 해제")
    public ApiResponse<?> unlinkThreads(@PathVariable("snsType") SNSType snsType, @AuthenticationPrincipal AuthDetails authDetails) {
        accountService.unlinkService(snsType, authDetails.getMember());
        return ApiResponse.success("계정 연동 해제 성공");
    }


    @GetMapping("/instagram/login")
    @Operation(summary = "인스타그램 계정연동", description = "인스타그램 액세스토큰 발급")
    public void loginInstagram(HttpServletResponse response,
                               @AuthenticationPrincipal AuthDetails authDetails) throws IOException {
        String uri = instagramClient.buildAuthorizationUri(authDetails.getMember().getId());
        response.sendRedirect(uri);
    }

    @GetMapping("/instagram/link")
    @Operation(summary = "인스타그램 액세스 토큰 발급", description = "인스타그램 인가 코드로 액세스 토큰을 받고 계정에 연결")
    public ApiResponse<?> signInWithInstagram(@RequestParam("access_token") String accessToken,
                                              @AuthenticationPrincipal AuthDetails authDetails,
                                              HttpServletResponse response) throws IOException {
        accountService.linkInstagram(accessToken, authDetails.getMember());
        return ApiResponse.success("계정 연동 성공");
    }
}
