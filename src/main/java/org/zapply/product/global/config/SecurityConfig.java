package org.zapply.product.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.zapply.product.global.security.ExceptionFilter;
import org.zapply.product.global.security.oAuth2.CustomOAuth2UserService;
import org.zapply.product.global.security.oAuth2.OAuth2SuccessHandler;
import org.zapply.product.global.security.jwt.JwtAccessDeniedHandler;
import org.zapply.product.global.security.jwt.JwtAuthenticationHandler;
import org.zapply.product.global.security.jwt.JwtFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ExceptionFilter exceptionFilter;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationHandler jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS & CSRF
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        // Session Management
        http.sessionManagement((session) ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));// Session 미사용

        // Disable default login forms
        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        // Exception handling
        http.exceptionHandling((exceptionHandling) -> exceptionHandling
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );

        // Add JWT filters
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(exceptionFilter, JwtFilter.class);

        // Configure OAuth2 login
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
        );

        // Authorization
        http.authorizeHttpRequests((authorize) ->
                authorize
                        // Swagger UI 및 API 문서
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v1/api-docs/**"
                        ).permitAll()

                        // OAuth2 및 Google 인증 엔드포인트
                        .requestMatchers(
                                "/oauth2/authorize/**",
                                "/oauth2/callback/**",
                                "/api/auth/google/**",
                                "/api/oauth/callback/*",
                                "/v1/account/facebook/link",
                                "/v1/account/threads/link",
                                "/v1/account/linkedin/link"
                        ).permitAll()

                        // 문자 인증
                        .requestMatchers(
                                "/v1/sms/**"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/**",
                                "/actuator/prometheus",
                                "/actuator/health/**",
                                "/actuator/info"
                        ).permitAll()

                        // LoadBalancer용 헬스 체크
                        .requestMatchers(HttpMethod.GET,"/v1/healthcheck").permitAll()
                        .requestMatchers("/v1/auth/**","/v1/user/**").permitAll()
                        .requestMatchers("/error/**").permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new HttpStatusReturningLogoutSuccessHandler();
    }
}