package com.cubingclub.payment.Mail.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailSendDTO {
    @Email
    @NotEmpty
    private String email;
    private String title;
    private String content;
}
