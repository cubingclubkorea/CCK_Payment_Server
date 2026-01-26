package com.cubingclub.payment.Registration.Service;

import com.cubingclub.payment.Exception.Entity.ErrorCode;
import com.cubingclub.payment.Exception.Exceptions.CustomizedException;
import com.cubingclub.payment.Registration.Component.ClientComponent;
import com.cubingclub.payment.Registration.DTO.*;
import com.cubingclub.payment.Registration.Entity.PaymentStatus;
import com.cubingclub.payment.Registration.Entity.Registration;
import com.cubingclub.payment.Registration.Entity.RegistrationStatus;
import com.cubingclub.payment.Registration.Repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {
    private final ClientComponent clientComponent;
    private final RegistrationRepository registrationRepository;

    public Page<RegistrationDTO> getRegistrationByComp(Long competitionId, RegistrationStatus registrationStatus, Pageable pageable) {
        return registrationRepository.findByCompetitionIdAndRegistrationStatusOrderByCreatedAtDesc(competitionId, registrationStatus, pageable)
                .map(RegistrationDTO::new);
    }

    public Page<RegistrationDTO> getRegistrationByUser(String cckId, Pageable pageable) {
        return registrationRepository.findByCckIdOrderByCreatedAtDesc(cckId, pageable)
                .map(RegistrationDTO::new);
    }

    public RegistrationDetailedDTO getRegistrationById(Long id) {
        return new RegistrationDetailedDTO(registrationRepository.findById(id).orElseThrow(()->new CustomizedException(ErrorCode.REGISTRATION_NOT_FOUND)));
    }

    public void createRegistration(RegistrationCreateDTO registrationCreateDTO){
        if(isRegistered(registrationCreateDTO.getCckId(), registrationCreateDTO.getCompetitionId())){
            throw new CustomizedException(ErrorCode.REGISTRATION_ALREADY_EXISTS);
        }
        registrationCreateDTO.setTotalFee(getTotalFee(registrationCreateDTO.getCompetitionId(), registrationCreateDTO.getSelectedEvents()));
        registrationRepository.save(registrationCreateDTO.toEntity());
    }

    public void updateRegistration(RegistrationUpdateEventsDTO registrationUpdateEventsDTO){
        Registration registration=registrationRepository.findById(registrationUpdateEventsDTO.getRegistrationId()).orElseThrow(()->new CustomizedException(ErrorCode.REGISTRATION_NOT_FOUND));
        registration.updateSelectedEvents(registrationUpdateEventsDTO.getSelectedEvents(), getTotalFee(registration.getCompetitionId(), registrationUpdateEventsDTO.getSelectedEvents()));
    }

    public void approveRegistration(Long id){
        Registration registration=registrationRepository.findById(id).orElseThrow(()->new CustomizedException(ErrorCode.REGISTRATION_NOT_FOUND));
        if(registration.getRegistrationStatus()!=RegistrationStatus.PENDING && registration.getPaymentStatus()!=PaymentStatus.PENDING){
            throw new CustomizedException(ErrorCode.REGISTRATION_ALREADY_APPROVED);
        }
        registration.updatePaymentStatus(PaymentStatus.PAID);
        registration.updateRegistrationStatus(RegistrationStatus.CONFIRMED);
    }

    public void cancelRegistration(Long id){
        Registration registration=registrationRepository.findById(id).orElseThrow(()->new CustomizedException(ErrorCode.REGISTRATION_NOT_FOUND));
        registration.updatePaymentStatus(PaymentStatus.CANCELLED);
        registration.updateRegistrationStatus(RegistrationStatus.CANCELLED);
    }

    public void refundRegistration(Long id){
        Registration registration=registrationRepository.findById(id).orElseThrow(()->new CustomizedException(ErrorCode.REGISTRATION_NOT_FOUND));
        registration.updatePaymentStatus(PaymentStatus.REFUNDED);
    }

    public void deleteRegistration(Long id){
        Registration registration=registrationRepository.findById(id).orElseThrow(()->new CustomizedException(ErrorCode.REGISTRATION_NOT_FOUND));
        if(registration.getPaymentStatus() == PaymentStatus.PAID ){
            throw new CustomizedException(ErrorCode.REGISTRATION_ALREADY_PAID);
        }
        registrationRepository.deleteById(id);
    }

    private Boolean isRegistered(String cckId, Long competitionId){
        return registrationRepository.existsByCckIdAndCompetitionIdAndRegistrationStatusNot(cckId, competitionId, RegistrationStatus.CANCELLED);
    }

    private Integer getTotalFee(Long competitionId, List<String> selectedEvents){
        CompetitionDTO competition = clientComponent.getCompetitionDTO(competitionId);

        Set<String> availableEvents = competition.getEventFeeList().stream()
                .map(EventFeeDTO::getEventName)
                .collect(Collectors.toSet());

        List<String> invalidEvents = selectedEvents.stream()
                .filter(event -> !availableEvents.contains(event))
                .toList();

        if (!invalidEvents.isEmpty()) {
            throw new CustomizedException(ErrorCode.REGISTRATION_INVALID_SELECTED_EVENTS);
        }

        return competition.getEventFeeList().stream()
                .filter(fee -> selectedEvents.contains(fee.getEventName()))
                .mapToInt(EventFeeDTO::getEventFee)
                .sum();
    }
}
