package org.zapply.product.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.dto.request.SignInRequest;
import org.zapply.product.domain.user.dto.response.LoginResponse;
import org.zapply.product.domain.user.dto.response.MemberResponse;
import org.zapply.product.domain.user.dto.response.TokenResponse;
import org.zapply.product.domain.user.service.AccountService;
import org.zapply.product.domain.user.service.AuthService;
import org.zapply.product.domain.user.service.MemberService;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import org.zapply.product.global.redis.RedisClient;
import org.zapply.product.global.security.AuthDetails;
import org.zapply.product.global.security.jwt.JwtProvider;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${jwt.header}")
    private String tokenHeader;

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/sign-up")
    @Operation(summary = "회원가입", description = "사용자의 정보 입력 후 회원가입")
    public ApiResponse<MemberResponse> signUp(@Valid @RequestBody AuthRequest authRequest) {
        return ApiResponse.success(authService.signUp(authRequest));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "로그인", description = "로그인 기능")
    public ApiResponse<LoginResponse> signIn(@Valid @RequestBody SignInRequest signInRequest) {
        return ApiResponse.success(authService.signIn(signInRequest));
    }

    @PostMapping("/sign-out")
    @Operation(summary = "로그아웃", description = "로그아웃 기능")
    public ApiResponse<?> signOut(HttpServletRequest request) {
        String refreshToken = jwtProvider.resolveRefreshToken(request);
        String accessToken = jwtProvider.resolveAccessToken(request);

        authService.signOut(refreshToken, accessToken);
        return ApiResponse.success("로그아웃 성공");
    }

    @GetMapping("/recreate")
    @Operation(summary = "토큰 재발급", description = "AccessToken 만료 시 RefreshToken으로 AccessToken 재발급")
    public ApiResponse<TokenResponse> recreate(HttpServletRequest request, @AuthenticationPrincipal AuthDetails authDetails) {
        String token = request.getHeader(tokenHeader);
        return ApiResponse.success(authService.recreate(token, authDetails.getMember()));
    }

    @GetMapping("/email/duplicate")
    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 확인. true - 중복, false - 사용 가능")
    public ApiResponse<Boolean> checkEmailDuplicate(@RequestParam("email") String email) {
        return ApiResponse.success(authService.checkEmailDuplicate(email));
    }


    @GetMapping("/google/exchange")
    @Operation(summary = "google 로그인", description = "발급된 코드를 가지고 사용자 정보로 교환합니다.")
    public ApiResponse<LoginResponse> exchangeCodetoUserResponse(@RequestParam("code") String code) {
        return ApiResponse.success(authService.exchangeCodeToUserInfo(code));
    }
}

