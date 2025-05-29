package org.zapply.product.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.zapply.product.global.apiPayload.exception.CoreException;
import org.zapply.product.global.apiPayload.response.ApiResponse;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CoreException e) {
            setResponse(response, e);
        }
    }

    private void setResponse(HttpServletResponse response, CoreException e) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setContentType("application/json;charset=UTF-8");

        int statusCode = e.getErrorType().getStatus().value();
        response.setStatus(statusCode);

        ApiResponse<?> errorResponse = ApiResponse.error(e.getErrorType());

        String errorJson = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(errorJson);
    }
}