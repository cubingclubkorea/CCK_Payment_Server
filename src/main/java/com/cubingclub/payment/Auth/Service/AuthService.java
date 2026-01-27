package com.cubingclub.payment.Auth.Service;

import com.cubingclub.payment.Auth.DTO.AccountDTO;
import com.cubingclub.payment.Auth.DTO.PlayerInfo;
import com.cubingclub.payment.Auth.DTO.TokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthClientService authClientService;
    private final TokenService tokenService;

    public TokenResponse exchangeTokenWithCookie(String code, HttpServletResponse response) {
        return tokenService.exchangeTokenWithCookie(code, response);
    }

    public TokenResponse refreshAccessTokenWithCookie(HttpServletRequest request, HttpServletResponse response) {
        return tokenService.refreshAccessTokenWithCookie(request, response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        tokenService.logout(request, response);
    }

    public String extractCckId(String token) throws Exception {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String payload = token.split("\\.")[1];
        String json = new String(Base64.getUrlDecoder().decode(payload));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);

        return node.get("sub").asText();
    }

    public AccountDTO getMyInfo(String token) {
        return authClientService.getMyInfo(token);
    }

    public Boolean existRfid(String cckID){
        return authClientService.existRfid(cckID);
    }

    public PlayerInfo getPlayerInfo(String cckId) {
        return authClientService.getPlayerInfo(cckId);
    }

    public Optional<PlayerInfo> getRFIDInfo(String token, String uid) {
        return authClientService.getRFIDInfo(token, uid);
    }
}
