package com.cubingclub.payment.Registration.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationUpdateEventsDTO {
    @NotNull
    Long registrationId;
    @NotEmpty
    List<String> selectedEvents;
}
