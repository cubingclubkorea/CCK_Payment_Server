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
    private String wcaId;
    private String emergencyPhone;
    private String emergencyContact;
    @NotEmpty
    private List<String> selectedEvents;

    public Registration toEntity(String name, String enName, String email, String phone, String cckId, LocalDate birthDate, Integer totalFee, Boolean needRfCard){
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
