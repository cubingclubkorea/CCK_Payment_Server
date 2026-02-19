package com.cubingclub.payment.Registration.Entity;

import com.cubingclub.payment.Crypto.CryptoConverter;
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
    @Convert(converter = CryptoConverter.class)
    private String email;
    @Convert(converter = CryptoConverter.class)
    private String phone;
    @Column(nullable = false)
    private String cckId;
    private String wcaId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    @Convert(converter = CryptoConverter.class)
    private String emergencyPhone;
    private String emergencyContact;
    @Column(nullable = false)
    private Long competitionId;
    private String competitionName;
    @ElementCollection(fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    @Builder.Default
    private List<String> selectedEvents=new ArrayList<>();
    @Column(nullable = false)
    private Integer totalFee;
    @Builder.Default
    private PaymentStatus paymentStatus=PaymentStatus.PENDING;
    @Builder.Default
    private RegistrationStatus registrationStatus=RegistrationStatus.PENDING;
    @Builder.Default
    private Boolean needRfCard=true;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateSelectedEvents(List<String> selectedEvents, Integer totalFee){
        this.selectedEvents.clear();
        this.selectedEvents.addAll(selectedEvents);
        this.totalFee=totalFee;
    }

    public void updateNeedRfCard(Boolean needRfCard){
        this.needRfCard=needRfCard;
    }

    public void updatePaymentStatus(PaymentStatus paymentStatus){
        this.paymentStatus=paymentStatus;
    }

    public void updateRegistrationStatus(RegistrationStatus registrationStatus){
        this.registrationStatus=registrationStatus;
    }
}
