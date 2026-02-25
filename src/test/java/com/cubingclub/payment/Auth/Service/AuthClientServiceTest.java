package com.cubingclub.payment.Auth.Service;

import com.cubingclub.payment.Auth.DTO.AccountDTO;
import com.cubingclub.payment.Auth.DTO.PlayerInfo;
import com.cubingclub.payment.Exception.Entity.ErrorCode;
import com.cubingclub.payment.Exception.Exceptions.CustomizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthClientService 테스트")
class AuthClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuthClientService authClientService;

    private static final String AUTH_URI = "http://test-auth-server.com";
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String CLIENT_REDIRECT = "http://test-redirect.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authClientService, "authUri", AUTH_URI);
        ReflectionTestUtils.setField(authClientService, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(authClientService, "clientSecret", CLIENT_SECRET);
        ReflectionTestUtils.setField(authClientService, "clientRedirect", CLIENT_REDIRECT);
        ReflectionTestUtils.setField(authClientService, "restTemplate", restTemplate);
    }

    @Test
    @DisplayName("인가 코드를 토큰으로 교환 성공")
    void exchangeCodeForToken_Success() throws Exception {
        // given
        String code = "test-code";
        String tokenJson = "{\"access_token\":\"test-access-token\",\"refresh_token\":\"test-refresh-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
        ResponseEntity<String> response = new ResponseEntity<>(tokenJson, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq(AUTH_URI + "/oauth2/token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // when
        Map<String, Object> result = authClientService.exchangeCodeForToken(code);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("access_token")).isEqualTo("test-access-token");
        assertThat(result.get("refresh_token")).isEqualTo("test-refresh-token");
        assertThat(result.get("token_type")).isEqualTo("Bearer");
        assertThat(result.get("expires_in")).isEqualTo(3600);
        
        verify(restTemplate).postForEntity(
                eq(AUTH_URI + "/oauth2/token"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("인가 코드 교환 중 오류 발생")
    void exchangeCodeForToken_Error() {
        // given
        String code = "test-code";
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Connection error"));

        // when & then
        assertThatThrownBy(() -> authClientService.exchangeCodeForToken(code))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to exchange code for token");
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신 성공")
    void refreshAccessToken_Success() throws Exception {
        // given
        String refreshToken = "test-refresh-token";
        String tokenJson = "{\"access_token\":\"new-access-token\",\"refresh_token\":\"new-refresh-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
        ResponseEntity<String> response = new ResponseEntity<>(tokenJson, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq(AUTH_URI + "/oauth2/token"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // when
        Map<String, Object> result = authClientService.refreshAccessToken(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("access_token")).isEqualTo("new-access-token");
        assertThat(result.get("refresh_token")).isEqualTo("new-refresh-token");
        
        verify(restTemplate).postForEntity(
                eq(AUTH_URI + "/oauth2/token"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("Access Token 갱신 중 오류 발생")
    void refreshAccessToken_Error() {
        // given
        String refreshToken = "test-refresh-token";
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Token expired"));

        // when & then
        assertThatThrownBy(() -> authClientService.refreshAccessToken(refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to refresh access token");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq(AUTH_URI + "/oauth2/token/logout"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // when
        authClientService.logout(accessToken, refreshToken);

        // then
        verify(restTemplate).postForEntity(
                eq(AUTH_URI + "/oauth2/token/logout"),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    @DisplayName("로그아웃 중 오류 발생")
    void logout_Error() {
        // given
        String accessToken = "test-access-token";
        String refreshToken = "test-refresh-token";
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Logout failed"));

        // when & then
        assertThatThrownBy(() -> authClientService.logout(accessToken, refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to logout");
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        // given
        String token = "test-token";
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setCckId("test-cck-id");
        accountDTO.setName("홍길동");
        accountDTO.setEmail("test@example.com");

        ResponseEntity<AccountDTO> response = new ResponseEntity<>(accountDTO, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(AUTH_URI + "/api/auth/info"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccountDTO.class)
        )).thenReturn(response);

        // when
        AccountDTO result = authClientService.getMyInfo(token);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCckId()).isEqualTo("test-cck-id");
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        
        verify(restTemplate).exchange(
                eq(AUTH_URI + "/api/auth/info"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(AccountDTO.class)
        );
    }

    @Test
    @DisplayName("RFID 존재 여부 확인 - 존재함")
    void existRfid_Exists() {
        // given
        String cckId = "test-cck-id";
        String token = "test-token";
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(
                eq(AUTH_URI + "/api/rfid/exist/" + cckId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(response);

        // when
        Boolean result = authClientService.existRfid(cckId, token);

        // then
        assertThat(result).isTrue();
        
        verify(restTemplate).exchange(
                eq(AUTH_URI + "/api/rfid/exist/" + cckId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    @DisplayName("RFID 존재 여부 확인 - 존재하지 않음 (404)")
    void existRfid_NotFound() {
        // given
        String cckId = "test-cck-id";
        String token = "test-token";

        when(restTemplate.exchange(
                eq(AUTH_URI + "/api/rfid/exist/" + cckId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // when
        Boolean result = authClientService.existRfid(cckId, token);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("RFID 존재 여부 확인 - 다른 에러는 예외 발생")
    void existRfid_OtherError_ThrowsException() {
        // given
        String cckId = "test-cck-id";
        String token = "test-token";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> authClientService.existRfid(cckId, token))
                .isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("플레이어 정보 조회 성공")
    void getPlayerInfo_Success() {
        // given
        String cckId = "test-cck-id";
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setCckId(cckId);
        playerInfo.setName("홍길동");
        playerInfo.setState("ACTIVE");

        ResponseEntity<PlayerInfo> response = new ResponseEntity<>(playerInfo, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(AUTH_URI + "/api/auth/info/" + cckId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PlayerInfo.class)
        )).thenReturn(response);

        // when
        PlayerInfo result = authClientService.getPlayerInfo(cckId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCckId()).isEqualTo(cckId);
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getState()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("플레이어 정보 조회 실패 - 서버 오류")
    void getPlayerInfo_ServerError() {
        // given
        String cckId = "test-cck-id";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PlayerInfo.class)
        )).thenThrow(new RuntimeException("Server error"));

        // when & then
        assertThatThrownBy(() -> authClientService.getPlayerInfo(cckId))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_SERVER_FAILED);
    }

    @Test
    @DisplayName("RFID 정보 조회 성공")
    void getRFIDInfo_Success() {
        // given
        String token = "test-token";
        String uid = "test-uid";
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.setCckId("test-cck-id");
        playerInfo.setName("홍길동");

        ResponseEntity<PlayerInfo> response = new ResponseEntity<>(playerInfo, HttpStatus.OK);

        when(restTemplate.exchange(
                eq(AUTH_URI + "/api/rfid/info/" + uid),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PlayerInfo.class)
        )).thenReturn(response);

        // when
        Optional<PlayerInfo> result = authClientService.getRFIDInfo(token, uid);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCckId()).isEqualTo("test-cck-id");
        assertThat(result.get().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("RFID 정보 조회 실패 - 클라이언트 오류")
    void getRFIDInfo_ClientError() {
        // given
        String token = "test-token";
        String uid = "unknown-uid";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PlayerInfo.class)
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                null
        ));

        // when
        Optional<PlayerInfo> result = authClientService.getRFIDInfo(token, uid);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("RFID 정보 조회 실패 - 서버 오류")
    void getRFIDInfo_ServerError() {
        // given
        String token = "test-token";
        String uid = "test-uid";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PlayerInfo.class)
        )).thenThrow(new org.springframework.web.client.HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // when
        Optional<PlayerInfo> result = authClientService.getRFIDInfo(token, uid);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("RFID 정보 조회 실패 - 예상치 못한 오류")
    void getRFIDInfo_UnexpectedError() {
        // given
        String token = "test-token";
        String uid = "test-uid";

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(PlayerInfo.class)
        )).thenThrow(new RuntimeException("Unexpected error"));

        // when
        Optional<PlayerInfo> result = authClientService.getRFIDInfo(token, uid);

        // then
        assertThat(result).isEmpty();
    }
}
