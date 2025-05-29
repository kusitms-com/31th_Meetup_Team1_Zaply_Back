package org.zapply.product.global.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        setResponse(response);
    }

    private void setResponse(HttpServletResponse response) throws IOException{
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(GlobalErrorType.FORBIDDEN.getStatus().value());

        ApiResponse errorResponse = ApiResponse.error(GlobalErrorType.FORBIDDEN);
        String errorJson = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(errorJson);
    }
}