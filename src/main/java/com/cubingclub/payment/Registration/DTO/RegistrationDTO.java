package com.cubingclub.payment.Registration.DTO;

import com.cubingclub.payment.Registration.Entity.PaymentStatus;
import com.cubingclub.payment.Registration.Entity.Registration;
import com.cubingclub.payment.Registration.Entity.RegistrationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class RegistrationDTO {
    private Long id;
    private Long competitionId;
    private String competitionName;
    private String name;
    private String enName;
    private String cckId;
    private List<String> selectedEvents;
    private Integer totalFee;
    private PaymentStatus paymentStatus;
    private RegistrationStatus registrationStatus;
    private Boolean needRfCard;

    public RegistrationDTO(Registration entity){
        this.id=entity.getId();
        this.competitionId=entity.getCompetitionId();
        this.competitionName=entity.getCompetitionName();
        this.name=entity.getName();
        this.enName=entity.getEnName();
        this.cckId=entity.getCckId();
        this.selectedEvents=new ArrayList<>(entity.getSelectedEvents());
        this.totalFee=entity.getTotalFee();
        this.paymentStatus=entity.getPaymentStatus();
        this.registrationStatus=entity.getRegistrationStatus();
        this.needRfCard=entity.getNeedRfCard();
    }
}
