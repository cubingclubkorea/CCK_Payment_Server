package com.cubingclub.payment.Auth.Service;

import com.cubingclub.payment.Auth.DTO.AccountDTO;
import com.cubingclub.payment.Auth.DTO.PlayerInfo;
import com.cubingclub.payment.Auth.DTO.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock
    private AuthClientService authClientService;

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    private TokenResponse tokenResponse;
    private AccountDTO accountDTO;
    private PlayerInfo playerInfo;

    @BeforeEach
    void setUp() {
        tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("test-access-token");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(3600);
        tokenResponse.setScope("openid profile email");

        accountDTO = new AccountDTO();
        accountDTO.setCckId("test-cck-id");
        accountDTO.setName("홍길동");
        accountDTO.setEmail("test@example.com");

        playerInfo = new PlayerInfo();
        playerInfo.setCckId("test-cck-id");
        playerInfo.setName("홍길동");
        playerInfo.setState("ACTIVE");
    }

    @Test
    @DisplayName("인가 코드로 토큰 교환")
    void exchangeTokenWithCookie_Success() {
        // given
        String code = "test-code";
        when(tokenService.exchangeTokenWithCookie(code, response)).thenReturn(tokenResponse);

        // when
        TokenResponse result = authService.exchangeTokenWithCookie(code, response);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("test-access-token");
        verify(tokenService).exchangeTokenWithCookie(code, response);
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신")
    void refreshAccessTokenWithCookie_Success() {
        // given
        when(tokenService.refreshAccessTokenWithCookie(request, response)).thenReturn(tokenResponse);

        // when
        TokenResponse result = authService.refreshAccessTokenWithCookie(request, response);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("test-access-token");
        verify(tokenService).refreshAccessTokenWithCookie(request, response);
    }

    @Test
    @DisplayName("로그아웃")
    void logout_Success() {
        // given
        doNothing().when(tokenService).logout(request, response);

        // when
        authService.logout(request, response);

        // then
        verify(tokenService).logout(request, response);
    }

    @Test
    @DisplayName("JWT 토큰에서 CCK ID 추출 성공")
    void extractCckId_Success() throws Exception {
        // given
        // JWT 형식: header.payload.signature
        // payload는 {"sub":"test-user-id"} 형태
        String payload = "{\"sub\":\"test-user-id\",\"iat\":1234567890}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes());
        String token = "eyJhbGciOiJIUzI1NiJ9." + encodedPayload + ".signature";

        // when
        String result = authService.extractCckId(token);

        // then
        assertThat(result).isEqualTo("test-user-id");
    }

    @Test
    @DisplayName("Bearer 접두사가 있는 토큰에서 CCK ID 추출")
    void extractCckId_WithBearerPrefix_Success() throws Exception {
        // given
        String payload = "{\"sub\":\"test-user-id\"}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes());
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9." + encodedPayload + ".signature";

        // when
        String result = authService.extractCckId(token);

        // then
        assertThat(result).isEqualTo("test-user-id");
    }

    @Test
    @DisplayName("잘못된 토큰 형식으로 예외 발생")
    void extractCckId_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.token";

        // when & then
        assertThatThrownBy(() -> authService.extractCckId(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        // given
        String token = "test-token";
        when(authClientService.getMyInfo(token)).thenReturn(accountDTO);

        // when
        AccountDTO result = authService.getMyInfo(token);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCckId()).isEqualTo("test-cck-id");
        assertThat(result.getName()).isEqualTo("홍길동");
        verify(authClientService).getMyInfo(token);
    }

    @Test
    @DisplayName("RFID 존재 여부 확인 - 존재함")
    void existRfid_Exists() {
        // given
        String cckId = "test-cck-id";
        String token = "test-token";
        when(authClientService.existRfid(cckId, token)).thenReturn(true);

        // when
        Boolean result = authService.existRfid(cckId, token);

        // then
        assertThat(result).isTrue();
        verify(authClientService).existRfid(cckId, token);
    }

    @Test
    @DisplayName("RFID 존재 여부 확인 - 존재하지 않음")
    void existRfid_NotExists() {
        // given
        String cckId = "test-cck-id";
        String token = "test-token";
        when(authClientService.existRfid(cckId, token)).thenReturn(false);

        // when
        Boolean result = authService.existRfid(cckId, token);

        // then
        assertThat(result).isFalse();
        verify(authClientService).existRfid(cckId, token);
    }

    @Test
    @DisplayName("플레이어 정보 조회 성공")
    void getPlayerInfo_Success() {
        // given
        String cckId = "test-cck-id";
        when(authClientService.getPlayerInfo(cckId)).thenReturn(playerInfo);

        // when
        PlayerInfo result = authService.getPlayerInfo(cckId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCckId()).isEqualTo("test-cck-id");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getState()).isEqualTo("ACTIVE");
        verify(authClientService).getPlayerInfo(cckId);
    }

    @Test
    @DisplayName("RFID 정보 조회 성공")
    void getRFIDInfo_Success() {
        // given
        String token = "test-token";
        String uid = "test-uid";
        when(authClientService.getRFIDInfo(token, uid)).thenReturn(Optional.of(playerInfo));

        // when
        Optional<PlayerInfo> result = authService.getRFIDInfo(token, uid);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCckId()).isEqualTo("test-cck-id");
        verify(authClientService).getRFIDInfo(token, uid);
    }

    @Test
    @DisplayName("RFID 정보 조회 - 정보 없음")
    void getRFIDInfo_NotFound() {
        // given
        String token = "test-token";
        String uid = "unknown-uid";
        when(authClientService.getRFIDInfo(token, uid)).thenReturn(Optional.empty());

        // when
        Optional<PlayerInfo> result = authService.getRFIDInfo(token, uid);

        // then
        assertThat(result).isEmpty();
        verify(authClientService).getRFIDInfo(token, uid);
    }

    @Test
    @DisplayName("null 토큰으로 CCK ID 추출 시 예외 발생")
    void extractCckId_NullToken_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> authService.extractCckId(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("빈 토큰으로 CCK ID 추출 시 예외 발생")
    void extractCckId_EmptyToken_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> authService.extractCckId(""))
                .isInstanceOf(Exception.class);
    }
}
