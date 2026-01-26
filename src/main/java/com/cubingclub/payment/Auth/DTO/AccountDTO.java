package com.cubingclub.payment.Auth.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {
    private String email;

    private String name;

    private String enName;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate birthDate;

    private UserRole position;

    private String state;

    private Gender gender;

    private String cckId;

    private String wcaId;

    private String phoneNum;
}
