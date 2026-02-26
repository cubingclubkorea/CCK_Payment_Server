package com.cubingclub.payment.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(3)  // 가장 먼저 실행하여 MDC 설정
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1단계: 요청 정보 MDC에 설정 (인증 전)
            String clientIp = getClientIp(request);
            MDC.put("ip", clientIp);
            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("userId", "anonymous");  // 기본값 설정

            // 2단계: 필터 체인 실행 (여기서 Spring Security의 JWT 인증 실행됨)
            filterChain.doFilter(request, response);

            // 3단계: 인증 후 SecurityContext에서 사용자 정보 추출하여 MDC 업데이트
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
                // OAuth2 JWT 토큰에서 정보 추출
                if (authentication.getPrincipal() instanceof Jwt jwt) {
                    String userId = jwt.getSubject();
                    String roles = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .filter(authority -> authority.startsWith("ROLE_"))
                            .map(authority -> authority.substring(5)) // "ROLE_" 제거
                            .collect(Collectors.joining(","));

                    MDC.put("userId", userId);
                    if (!roles.isEmpty()) {
                        MDC.put("roles", roles);
                    }

                    log.info("Authenticated access");
                } else {
                    // JWT가 아닌 다른 인증 방식
                    MDC.put("userId", authentication.getName());
                    log.info("Authenticated access (non-JWT)");
                }
            } else {
                log.info("Anonymous access");
            }

            // 4단계: 응답 상태 코드 로깅
            int status = response.getStatus();

            if (status >= 400) {
                if (status == 401) {
                    log.warn("Unauthorized");
                } else if (status == 403) {
                    log.warn("Access denied");
                } else if (status >= 500) {
                    log.error("Server error: {}", status);
                } else {
                    log.warn("Client error: {}", status);
                }
            }
        } catch (Exception e) {
            log.error("Error in MDC filter", e);
            throw e;
        } finally {
            // 요청 처리 완료 후 MDC 정리
            MDC.clear();
        }
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출합니다.
     * 프록시, 로드밸런서를 거친 경우에도 실제 IP를 추출합니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For는 여러 IP를 포함할 수 있으며, 첫 번째가 원본 클라이언트 IP입니다
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // IPv6 localhost를 IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return ip;
    }
}
