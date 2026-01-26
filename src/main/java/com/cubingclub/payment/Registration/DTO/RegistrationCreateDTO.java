package com.cubingclub.payment.Registration.DTO;

import com.cubingclub.payment.Registration.Entity.Registration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationCreateDTO {
    @NotNull
    private Long competitionId;
    @NotBlank
    private String competitionName;
    @NotBlank
    private String name;
    private String enName;
    private String email;
    private String phone;
    @NotBlank
    private String cckId;
    private String wcaId;
    private LocalDate birthDate;
    private String emergencyPhone;
    private String emergencyContact;
    @NotEmpty
    private List<String> selectedEvents;
    private Integer totalFee;
    private Boolean needRfCard;

    public Registration toEntity(){
        return Registration.builder()
                .competitionId(competitionId)
                .competitionName(competitionName)
                .name(name)
                .enName(enName)
                .email(email)
                .phone(phone)
                .cckId(cckId)
                .wcaId(wcaId)
                .birthDate(birthDate)
                .emergencyPhone(emergencyPhone)
                .emergencyContact(emergencyContact)
                .selectedEvents(selectedEvents)
                .totalFee(totalFee)
                .needRfCard(needRfCard)
                .build();
    }
}
