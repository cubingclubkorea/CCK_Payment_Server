package com.cubingclub.payment.Auth.DTO;

/**
 * JWT 토큰과 UID를 담는 컨텍스트 클래스
 */
public record AuthContext(String token, String uid) {
}
