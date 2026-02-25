package com.cubingclub.payment.Auth.Service;

import com.cubingclub.payment.Auth.DTO.TokenResponse;
import com.cubingclub.payment.Exception.Entity.ErrorCode;
import com.cubingclub.payment.Exception.Exceptions.CustomizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService 테스트")
class TokenServiceTest {

    @Mock
    private AuthClientService authClientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TokenService tokenService;

    private Map<String, Object> tokenMap;

    @BeforeEach
    void setUp() {
        tokenMap = new HashMap<>();
        tokenMap.put("access_token", "test-access-token");
        tokenMap.put("refresh_token", "test-refresh-token");
        tokenMap.put("token_type", "Bearer");
        tokenMap.put("expires_in", 3600);
        tokenMap.put("scope", "openid profile email");
    }

    @Test
    @DisplayName("인가 코드를 토큰으로 교환하고 쿠키에 저장")
    void exchangeTokenWithCookie_Success() {
        // given
        String code = "test-code";
        when(authClientService.exchangeCodeForToken(code)).thenReturn(tokenMap);

        // when
        TokenResponse result = tokenService.exchangeTokenWithCookie(code, response);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("test-access-token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(3600);
        assertThat(result.getScope()).isEqualTo("openid profile email");

        verify(authClientService).exchangeCodeForToken(code);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신 성공")
    void refreshAccessTokenWithCookie_Success() {
        // given
        String refreshToken = "test-refresh-token";
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(authClientService.refreshAccessToken(refreshToken)).thenReturn(tokenMap);

        // when
        TokenResponse result = tokenService.refreshAccessTokenWithCookie(request, response);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("test-access-token");
        verify(authClientService).refreshAccessToken(refreshToken);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Refresh Token이 없을 때 예외 발생")
    void refreshAccessTokenWithCookie_NoRefreshToken_ThrowsException() {
        // given
        when(request.getCookies()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> tokenService.refreshAccessTokenWithCookie(request, response))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);

        verify(authClientService, never()).refreshAccessToken(anyString());
    }

    @Test
    @DisplayName("Refresh Token이 쿠키에 없을 때 예외 발생")
    void refreshAccessTokenWithCookie_NoCookieWithRefreshToken_ThrowsException() {
        // given
        Cookie otherCookie = new Cookie("other_cookie", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});

        // when & then
        assertThatThrownBy(() -> tokenService.refreshAccessTokenWithCookie(request, response))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("새로운 Refresh Token이 있을 때 쿠키 업데이트")
    void refreshAccessTokenWithCookie_WithNewRefreshToken() {
        // given
        String oldRefreshToken = "old-refresh-token";
        String newRefreshToken = "new-refresh-token";
        Cookie cookie = new Cookie("refresh_token", oldRefreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        Map<String, Object> newTokenMap = new HashMap<>(tokenMap);
        newTokenMap.put("refresh_token", newRefreshToken);
        when(authClientService.refreshAccessToken(oldRefreshToken)).thenReturn(newTokenMap);

        // when
        TokenResponse result = tokenService.refreshAccessTokenWithCookie(request, response);

        // then
        assertThat(result).isNotNull();
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("로그아웃 성공 - Access Token과 Refresh Token 모두 있을 때")
    void logout_Success() {
        // given
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        doNothing().when(authClientService).logout(accessToken, refreshToken);

        // when
        tokenService.logout(request, response);

        // then
        verify(authClientService).logout(accessToken, refreshToken);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("로그아웃 - Refresh Token 없이도 성공")
    void logout_WithoutRefreshToken() {
        // given
        String accessToken = "test-access-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(request.getCookies()).thenReturn(null);

        // when
        tokenService.logout(request, response);

        // then
        verify(authClientService).logout(accessToken, null);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("로그아웃 - Access Token이 없을 때 예외 발생")
    void logout_NoAccessToken_ThrowsException() {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> tokenService.logout(request, response))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_ACCESS_TOKEN_NOT_FOUND);

        verify(authClientService, never()).logout(anyString(), anyString());
    }

    @Test
    @DisplayName("로그아웃 - Authorization 헤더가 Bearer로 시작하지 않을 때 예외 발생")
    void logout_InvalidAuthHeader_ThrowsException() {
        // given
        when(request.getHeader("Authorization")).thenReturn("Invalid token");

        // when & then
        assertThatThrownBy(() -> tokenService.logout(request, response))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_ACCESS_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("로그아웃 - Auth 서버 오류 발생해도 쿠키는 삭제됨")
    void logout_AuthServerError_CookieStillDeleted() {
        // given
        String accessToken = "test-access-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + accessToken);
        when(request.getCookies()).thenReturn(null);
        
        doThrow(new RuntimeException("Auth server error"))
                .when(authClientService).logout(anyString(), any());

        // when
        tokenService.logout(request, response);

        // then
        verify(authClientService).logout(accessToken, null);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Refresh Token이 null일 때는 쿠키를 설정하지 않음")
    void exchangeTokenWithCookie_NullRefreshToken() {
        // given
        String code = "test-code";
        Map<String, Object> tokenMapWithoutRefresh = new HashMap<>(tokenMap);
        tokenMapWithoutRefresh.remove("refresh_token");
        tokenMapWithoutRefresh.put("refresh_token", null);
        
        when(authClientService.exchangeCodeForToken(code)).thenReturn(tokenMapWithoutRefresh);

        // when
        TokenResponse result = tokenService.exchangeTokenWithCookie(code, response);

        // then
        assertThat(result).isNotNull();
        verify(response, never()).addCookie(any(Cookie.class));
    }
}
