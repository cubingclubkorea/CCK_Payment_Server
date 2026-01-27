package com.cubingclub.payment.Registration.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CompetitionDTO {
    private Integer idx;
    private String compName;
    private LocalDate compDateStart;
    private LocalDate compDateEnd;
    private LocalDateTime joinDateStart;
    private LocalDateTime joinDateEnd;
    private Integer baseFee;
    private String location;
    private Integer maxParticipate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EventFeeDTO> eventFeeList;
}
