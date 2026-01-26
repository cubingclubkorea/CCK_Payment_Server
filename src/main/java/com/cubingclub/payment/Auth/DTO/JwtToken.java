package com.cubingclub.payment.Auth.DTO;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Authorization 헤더에서 자동으로 JWT 토큰을 추출하기 위한 어노테이션
 * 사용 예: public ResponseEntity<?> method(@JwtToken String token)
 * "Bearer " 접두사는 자동으로 제거됩니다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JwtToken {
}
