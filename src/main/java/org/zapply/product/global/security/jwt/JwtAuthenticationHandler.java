package org.zapply.product.global.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.zapply.product.global.apiPayload.exception.GlobalErrorType;
import org.zapply.product.global.apiPayload.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object exception = request.getAttribute("exception");

        if (exception instanceof GlobalErrorType errorType) {
            setResponse(response, errorType);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<?> errorResponse = ApiResponse.error(GlobalErrorType.UNAUTHORIZED);
        String errorJson = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(errorJson);
    }

    private void setResponse(HttpServletResponse response, GlobalErrorType errorType) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorType.getStatus().value());

        ApiResponse<?> errorResponse = ApiResponse.error(errorType);
        String errorJson = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(errorJson);
    }
}