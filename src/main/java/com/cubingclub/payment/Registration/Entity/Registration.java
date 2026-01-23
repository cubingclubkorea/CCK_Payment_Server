package com.cubingclub.payment.Registration.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String enName;
    private String email;
    private String phone;
    private String cckId;
    private String wcaId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String emergencyPhone;
    private String emergencyContact;
    private Long competitionId;
    private String competitionName;
    @ElementCollection(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @Builder.Default
    private List<String> selectedEvents=new ArrayList<>();
    private Integer totalFee;
    private PaymentStatus paymentStatus;
    private RegistrationStatus registrationStatus;
    private Boolean needRfCard;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updatePaymentStatus(PaymentStatus paymentStatus){
        this.paymentStatus=paymentStatus;
    }

    public void updateRegistrationStatus(RegistrationStatus registrationStatus){
        this.registrationStatus=registrationStatus;
    }
}
