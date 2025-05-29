package org.zapply.product.global.security.oAuth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.zapply.product.domain.user.dto.request.SignInRequest;
import org.zapply.product.domain.user.dto.response.AccountsInfoResponse;
import org.zapply.product.domain.user.dto.response.LoginResponse;
import org.zapply.product.domain.user.dto.response.MemberResponse;
import org.zapply.product.domain.user.dto.response.TokenResponse;
import org.zapply.product.domain.user.entity.Member;
import org.zapply.product.domain.user.repository.MemberRepository;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import org.zapply.product.global.redis.RedisClient;
import org.zapply.product.global.security.jwt.JwtProvider;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;

    @Value("${jwt.token.refresh-expiration-time}")
    private long refreshTokenExpirationTime;

    @Value("${jwt.token.access-expiration-time}")
    private long accessTokenExpirationTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try{
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            // DB에서 회원 조회
            Long memberId = oAuth2User.getMemberId();
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));

            // JWT 토큰 생성 후 Redis에 refresh token 저장
            TokenResponse tokenResponse = jwtProvider.createToken(member);
            redisClient.setValue(member.getEmail(), tokenResponse.refreshToken(), refreshTokenExpirationTime);

            // LoginResponse 생성
            MemberResponse memberResponse = MemberResponse.of(member);
            AccountsInfoResponse accountsInfo = accountService.getAccountsInfo(member);
            LoginResponse loginResponse = LoginResponse.of(tokenResponse, memberResponse, accountsInfo);
            String tempCode = UUID.randomUUID().toString();
            redisClient.setValue("auth:" + tempCode,
                    objectMapper.writeValueAsString(loginResponse), 1000 * 60L);
            System.out.println("auth:"+tempCode);
            String callbackUrl = "http://localhost:3000/google/callback?code=" + tempCode;
            getRedirectStrategy().sendRedirect(request, response, callbackUrl);
        }
        catch (IOException e) {
            throw new CoreException(GlobalErrorType.OAUTH_ERROR);
        }
    }
}
