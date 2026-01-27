package com.cubingclub.payment.Auth.Service;

import com.cubingclub.payment.Auth.DTO.TokenResponse;
import com.cubingclub.payment.Exception.Entity.ErrorCode;
import com.cubingclub.payment.Exception.Exceptions.CustomizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final int REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60; // 30일

    private final AuthClientService authClientService;

    public TokenResponse exchangeTokenWithCookie(String code, HttpServletResponse response) {
        Map<String, Object> tokenMap = authClientService.exchangeCodeForToken(code);
        
        String refreshToken = (String) tokenMap.get("refresh_token");
        TokenResponse tokenResponse = createTokenResponse(tokenMap);
        
        setRefreshTokenCookie(response, refreshToken);
        
        return tokenResponse;
    }

    public TokenResponse refreshAccessTokenWithCookie(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new CustomizedException(ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }
        
        Map<String, Object> tokenMap = authClientService.refreshAccessToken(refreshToken);
        
        String newRefreshToken = (String) tokenMap.get("refresh_token");
        TokenResponse tokenResponse = createTokenResponse(tokenMap);
        
        if (newRefreshToken != null) {
            setRefreshTokenCookie(response, newRefreshToken);
        }
        
        return tokenResponse;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractAccessTokenFromHeader(request);
        if (accessToken == null) {
            throw new CustomizedException(ErrorCode.AUTH_ACCESS_TOKEN_NOT_FOUND);
        }
        
        String refreshToken = extractRefreshTokenFromCookie(request);
        
        try {
            authClientService.logout(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("Error during logout", e);
        } finally {
            // 오류 발생 여부와 관계없이 쿠키는 삭제
            deleteRefreshTokenCookie(response);
        }
    }

    private String extractAccessTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken != null) {
            Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
            response.addCookie(cookie);
        }
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.info("Refresh token cookie deleted");
    }

    private TokenResponse createTokenResponse(Map<String, Object> tokenMap) {
        TokenResponse response = new TokenResponse();
        response.setAccessToken((String) tokenMap.get("access_token"));
        response.setTokenType((String) tokenMap.get("token_type"));
        response.setExpiresIn((Integer) tokenMap.get("expires_in"));
        response.setScope((String) tokenMap.get("scope"));
        return response;
    }
}
