package com.cubingclub.payment.Auth.Service;

import com.cubingclub.payment.Auth.DTO.AccountDTO;
import com.cubingclub.payment.Auth.DTO.PlayerInfo;
import com.cubingclub.payment.Exception.Entity.ErrorCode;
import com.cubingclub.payment.Exception.Exceptions.CustomizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthClientService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.oauth.uri}")
    private String authUri;

    @Value("${spring.oauth.id}")
    private String clientId;

    @Value("${spring.oauth.secret}")
    private String clientSecret;

    @Value("${spring.oauth.redirect-uri}")
    private String clientRedirect;

    public Map<String, Object> exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("scope", "openid profile email");
        body.add("redirect_uri", clientRedirect);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    authUri + "/oauth2/token",
                    request,
                    String.class
            );
            return parseTokenResponse(response.getBody());
        } catch (Exception e) {
            log.error("Error exchanging code for token", e);
            throw new RuntimeException("Failed to exchange code for token", e);
        }
    }

    public Map<String, Object> refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    authUri + "/oauth2/token",
                    request,
                    String.class
            );
            return parseTokenResponse(response.getBody());
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    public void logout(String accessToken, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (refreshToken != null) {
            body.add("refresh_token", refreshToken);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(authUri + "/oauth2/token/logout", request, String.class);
            log.info("Successfully logged out from auth server");
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new RuntimeException("Failed to logout", e);
        }
    }

    public AccountDTO getMyInfo(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<AccountDTO> response = restTemplate.exchange(
                authUri + "/api/auth/info",
                HttpMethod.GET,
                entity,
                AccountDTO.class
        );
        return response.getBody();
    }

    public Boolean existRfid(String cckId){
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
                authUri + "/api/rfid/"+cckId,
                HttpMethod.GET,
                entity,
                Void.class
        );
        return response.getStatusCode() != HttpStatus.NOT_FOUND;
    }

    public PlayerInfo getPlayerInfo(String cckId) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<PlayerInfo> response = restTemplate.exchange(
                    authUri + "/api/auth/info/" + cckId,
                    HttpMethod.GET,
                    entity,
                    PlayerInfo.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling auth server");
            throw new CustomizedException(ErrorCode.AUTH_SERVER_FAILED);
        }
    }

    public Optional<PlayerInfo> getRFIDInfo(String token, String uid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(headers);

        String url = authUri + "/api/rfid/info/"+uid;

        try {
            ResponseEntity<PlayerInfo> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PlayerInfo.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Client error from auth server: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Server error from auth server: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error calling auth server", e);
            return Optional.empty();
        }
    }

    private Map<String, Object> parseTokenResponse(String responseBody) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenMap = objectMapper.readValue(responseBody, Map.class);
            return tokenMap;
        } catch (Exception e) {
            log.error("Error parsing token response", e);
            throw new RuntimeException("Failed to parse token response", e);
        }
    }
}
