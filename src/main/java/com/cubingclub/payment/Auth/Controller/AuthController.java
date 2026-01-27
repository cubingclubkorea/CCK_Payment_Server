package com.cubingclub.payment.Auth.Controller;

import com.cubingclub.payment.Auth.DTO.TokenResponse;
import com.cubingclub.payment.Auth.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/csrf")
    public ResponseEntity<Void> initCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok().header("X-CSRF-TOKEN", csrfToken.getToken()).build();
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> getToken(@RequestParam(name = "code") String code, HttpServletResponse response) {
        return ResponseEntity.ok(authService.exchangeTokenWithCookie(code, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshAccessTokenWithCookie(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }
}