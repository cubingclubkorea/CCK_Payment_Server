# 예외 처리 시스템

## 목차
1. [개요](#개요)
2. [예외 처리 구조](#예외-처리-구조)
3. [에러 응답 형식](#에러-응답-형식)
4. [에러 코드 목록](#에러-코드-목록)
5. [사용 예시](#사용-예시)

---

## 개요

이 시스템은 통일된 예외 처리 및 에러 응답을 제공합니다.

**주요 기능:**
- 일관된 에러 응답 형식
- 커스텀 예외 클래스
- 에러 코드 기반 관리
- 글로벌 예외 핸들러

---

## 예외 처리 구조

### 1. ErrorResponseEntity

에러 응답을 표준화된 형식으로 반환하는 엔티티입니다.

**패키지:** `com.repul.superset.Exception.Entity`

**필드:**
```java
public class ErrorResponseEntity {
    private int status;      // HTTP 상태 코드 (예: 400, 401, 404)
    private String name;     // 에러 이름 (예: USER_NOT_EXIST)
    private String code;     // 에러 코드 (예: ACCOUNT-001)
    private String message;  // 에러 상세 메시지
}
```

**메서드:**
- `toResponseEntity(AccountErrorCode e)`: 에러 코드를 ResponseEntity로 변환

---

### 2. AccountErrorCode (Enum)

Account 관련 에러 코드를 정의하는 열거형입니다.

**패키지:** `com.repul.superset.Exception.Entity`

**구조:**
```java
public enum AccountErrorCode {
    ERROR_NAME(HttpStatus, "ERROR-CODE", "에러 메시지");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
```

---

### 3. AccountException

Account 관련 예외를 처리하는 커스텀 예외 클래스입니다.

**패키지:** `com.repul.superset.Exception.Exceptions`

**구조:**
```java
public class AccountException extends RuntimeException {
    AccountErrorCode accountErrorCode;
}
```

**사용 방법:**
```java
throw new AccountException(AccountErrorCode.USER_NOT_EXIST);
```

---

### 4. GlobalExceptionHandler

모든 예외를 가로채서 통일된 형식으로 반환하는 전역 예외 핸들러입니다.

**패키지:** `com.repul.superset.Exception`

**어노테이션:** `@RestControllerAdvice`

**처리 메서드:**
```java
@ExceptionHandler(AccountException.class)
protected ResponseEntity<ErrorResponseEntity> handleCustomException(AccountException e) {
    return ErrorResponseEntity.toResponseEntity(e.getAccountErrorCode());
}
```

---

## 에러 응답 형식

### 응답 스키마

모든 에러 응답은 다음 형식을 따릅니다.

| 필드      | 타입      | 설명                                |
|---------|---------|-----------------------------------|
| status  | Integer | HTTP 상태 코드 (400, 401, 404, 409 등) |
| name    | String  | 에러 이름 (Enum 이름)                   |
| code    | String  | 에러 코드 (ACCOUNT-001 형식)           |
| message | String  | 에러 상세 설명                          |

### 응답 예시

```json
{
  "status": 401,
  "name": "USER_NOT_EXIST",
  "code": "ACCOUNT-001",
  "message": "유저가 존재하지 않습니다."
}
```

---

## 에러 코드 목록

### Account 관련 에러 코드

#### 인증 실패 (401 Unauthorized)

| 에러 이름                | 코드          | 메시지                   | 설명                |
|----------------------|-------------|----------------------|-------------------|
| USER_NOT_EXIST       | ACCOUNT-001 | 유저가 존재하지 않습니다.       | 로그인 시 존재하지 않는 ID   |
| PASSWORD_INCORRECT   | ACCOUNT-002 | 비밀번호가 일치하지 않습니다.     | 로그인 시 비밀번호 불일치     |
| DISABLED_ACCOUNT     | ACCOUNT-005 | 비활성화된 계정입니다.         | 로그인 시 비활성화된 계정     |
| INVALID_ACCESS_TOKEN | ACCOUNT-006 | 유효하지 않은 액세스 토큰입니다.   | 액세스 토큰 검증 실패       |
| INVALID_REFRESH_TOKEN | ACCOUNT-007 | 유효하지 않은 리프레시 토큰입니다. | 리프레시 토큰 검증 실패 또는 블랙리스트 등록됨 |

**사용 시나리오:**
- 로그인 실패
- 토큰 검증 실패
- 비활성화된 계정 접근

---

#### 잘못된 요청 (400 Bad Request)

| 에러 이름                | 코드          | 메시지              | 설명                    |
|----------------------|-------------|------------------|------------------------|
| NEW_PASSWORD_NOT_MATCH | ACCOUNT-009 | 비밀번호 확인이 틀립니다. | 새 비밀번호와 비밀번호 확인이 일치하지 않음 |

**사용 시나리오:**
- 비밀번호 변경 시 새 비밀번호 확인 불일치

---

#### 리소스 없음 (404 Not Found)

| 에러 이름              | 코드          | 메시지              | 설명                      |
|--------------------|-------------|------------------|-------------------------|
| USER_NOT_EXIST_ADMIN | ACCOUNT-001 | 유저가 존재하지 않습니다. | 관리자 작업 시 존재하지 않는 사용자 ID |

**사용 시나리오:**
- 관리자가 사용자 수정/삭제 시 존재하지 않는 ID

---

#### 충돌 (409 Conflict)

| 에러 이름                  | 코드          | 메시지                  | 설명            |
|------------------------|-------------|----------------------|---------------|
| ID_DUPLICATE           | ACCOUNT-003 | 이미 존재하는 아이디입니다.     | 사용자 추가 시 ID 중복  |
| EMAIL_DUPLICATE        | ACCOUNT-004 | 이미 존재하는 이메일입니다.     | 사용자 추가 시 이메일 중복 |
| CONFLICT_ACCOUNT_DELETE | ACCOUNT-010 | 본인 계정을 삭제하고 있습니다. | 관리자가 자신의 계정 삭제 시도 |

**사용 시나리오:**
- 사용자 추가 시 중복 확인
- 관리자가 본인 계정 삭제 시도

---

#### 서버 오류 (500 Internal Server Error)

| 에러 이름        | 코드          | 메시지                | 설명            |
|--------------|-------------|--------------------|-----------------------|
| USERADD_FAIL | ACCOUNT-008 | 사용자 추가에 실패했습니다. | 사용자 추가 중 서버 내부 오류 발생 |

**사용 시나리오:**
- 데이터베이스 연결 오류
- 예상치 못한 서버 내부 오류

---

## 사용 예시

### Service에서 예외 발생

```java
@Service
public class AccountService {
    
    public JwtDTO login(LoginDTO loginDTO) {
        // 사용자 조회
        Account account = accountRepository.findById(loginDTO.getId())
            .orElseThrow(() -> new AccountException(AccountErrorCode.USER_NOT_EXIST));
        
        // 계정 활성화 확인
        if (!account.isActive()) {
            throw new AccountException(AccountErrorCode.DISABLED_ACCOUNT);
        }
        
        // 비밀번호 검증
        if (!encoder.matches(loginDTO.getPassword(), account.getPassword())) {
            throw new AccountException(AccountErrorCode.PASSWORD_INCORRECT);
        }
        
        // 로그인 성공 처리...
    }
}
```

---

## 에러 처리 흐름

```
클라이언트 요청
    ↓
Controller
    ↓
Service (예외 발생)
    ↓
throw new AccountException(ErrorCode)
    ↓
GlobalExceptionHandler 가로채기
    ↓
ErrorResponseEntity.toResponseEntity()
    ↓
통일된 JSON 응답 생성
    ↓
클라이언트에게 에러 응답 반환
```

---

## 새로운 에러 코드 추가 방법

### 1. AccountErrorCode에 추가

```java
public enum AccountErrorCode {
    // 기존 코드...
    
    NEW_ERROR(HttpStatus.BAD_REQUEST, "ACCOUNT-011", "새로운 에러 메시지");
}
```

### 2. Service에서 사용

```java
if (someCondition) {
    throw new AccountException(AccountErrorCode.NEW_ERROR);
}
```

### 3. 자동으로 처리됨

GlobalExceptionHandler가 자동으로 가로채서 표준 형식으로 반환합니다.

---

## 참고사항

- 모든 커스텀 예외는 `RuntimeException`을 상속받습니다.
- `@RestControllerAdvice`를 통해 전역적으로 예외를 처리합니다.
- HTTP 상태 코드는 에러 코드에 정의되어 있으며, 자동으로 설정됩니다.
- 에러 코드는 `ACCOUNT-XXX` 형식을 따릅니다.
- 향후 다른 모듈(Dashboard, Dataset 등)의 에러 코드도 같은 패턴으로 추가할 수 있습니다.