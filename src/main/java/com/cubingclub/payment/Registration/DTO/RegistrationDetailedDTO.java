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
public class RegistrationDetailedDTO {
    private Long id;
    private Long competitionId;
    private String competitionName;
    private String name;
    private String enName;
    private String email;
    private String phone;
    private String cckId;
    private String wcaId;
    private LocalDate birthDate;
    private String emergencyPhone;
    private String emergencyContact;
    private List<String> selectedEvents;
    private Integer totalFee;
    private PaymentStatus paymentStatus;
    private RegistrationStatus registrationStatus;
    private Boolean needRfCard;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RegistrationDetailedDTO(Registration entity){
        this.id=entity.getId();
        this.competitionId=entity.getCompetitionId();
        this.competitionName=entity.getCompetitionName();
        this.name=entity.getName();
        this.enName=entity.getEnName();
        this.email=entity.getEmail();
        this.phone=entity.getPhone();
        this.cckId=entity.getCckId();
        this.wcaId=entity.getWcaId();
        this.birthDate=entity.getBirthDate();
        this.emergencyPhone=entity.getEmergencyPhone();
        this.emergencyContact=entity.getEmergencyContact();
        this.selectedEvents=entity.getSelectedEvents();
        this.totalFee=entity.getTotalFee();
        this.registrationStatus=entity.getRegistrationStatus();
        this.paymentStatus=entity.getPaymentStatus();
        this.selectedEvents=new ArrayList<>(entity.getSelectedEvents());
        this.needRfCard=entity.getNeedRfCard();
        this.createdAt=entity.getCreatedAt();
        this.updatedAt=entity.getUpdatedAt();
    }
}
