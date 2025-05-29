package org.zapply.product.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.zapply.product.domain.member.util.AuthRequestUtilTest;
import org.zapply.product.domain.user.dto.request.AuthRequest;
import org.zapply.product.domain.user.dto.response.MemberResponse;
import org.zapply.product.domain.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Nested
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("🔐회원가입 테스트")
    class signUp {

        @Test
        @DisplayName("✅회원가입 성공")
        void signUp_success() throws Exception {
            // given
            AuthRequest authRequest = AuthRequestUtilTest.createAuthRequest();

            // DTO를 JSON으로 변환
            String json = new Gson().toJson(authRequest);

            MemberResponse mockResponse = MemberResponse.builder()
                    .memberId(1L)
                    .name("zaply")
                    .email("zaply123@gmail.com")
                    .phoneNumber("010-1234-5678")
                    .build();

            Mockito.when(authService.signUp(any(AuthRequest.class)))
                    .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/v1/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.memberId").value(1L))
                    .andExpect(jsonPath("$.data.name").value("zaply"))
                    .andExpect(jsonPath("$.data.email").value("zaply123@gmail.com"))
                    .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"));
        }

        @Test
        @DisplayName("❌회원가입 실패 - MemberAlreadyExists")
        void signUp_fail() throws Exception {
            // given
            AuthRequest authRequest = AuthRequestUtilTest.createAuthRequest();

            // authService의 signUp 메서드가 CoreException을 던지도록 설정
            Mockito.when(authService.signUp(any(AuthRequest.class)))
                    .thenThrow(new CoreException(GlobalErrorType.MEMBER_ALREADY_EXISTS));

            // when & then
            mockMvc.perform(post("/v1/auth/sign-up")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}