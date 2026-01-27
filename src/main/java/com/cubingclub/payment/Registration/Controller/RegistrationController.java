package com.cubingclub.payment.Registration.Controller;

import com.cubingclub.payment.Auth.DTO.JwtToken;
import com.cubingclub.payment.Registration.DTO.RegistrationCreateDTO;
import com.cubingclub.payment.Registration.DTO.RegistrationDTO;
import com.cubingclub.payment.Registration.DTO.RegistrationDetailedDTO;
import com.cubingclub.payment.Registration.DTO.RegistrationUpdateEventsDTO;
import com.cubingclub.payment.Registration.Entity.RegistrationStatus;
import com.cubingclub.payment.Registration.Service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;

    @GetMapping("/user")
    public ResponseEntity<Page<RegistrationDTO>> getRegistrationsByUser(@JwtToken String token, Pageable pageable) {
        return ResponseEntity.ok(registrationService.getRegistrationByUser(token, pageable));
    }

    @GetMapping("/comp/{compIdx}/pending")
    public ResponseEntity<Page<RegistrationDTO>> getRegistrationsByCompIdxPending(@PathVariable Long compIdx, Pageable pageable) {
        return ResponseEntity.ok(registrationService.getRegistrationByComp(compIdx, RegistrationStatus.PENDING, pageable));
    }

    @GetMapping("/comp/{compIdx}/confirmed")
    public ResponseEntity<Page<RegistrationDTO>> getRegistrationsByCompIdxConfirmed(@PathVariable Long compIdx, Pageable pageable) {
        return ResponseEntity.ok(registrationService.getRegistrationByComp(compIdx, RegistrationStatus.CONFIRMED, pageable));
    }

    @GetMapping("/comp/{compIdx}/cancelled")
    public ResponseEntity<Page<RegistrationDTO>> getRegistrationsByCompIdxCancelled(@PathVariable Long compIdx, Pageable pageable) {
        return ResponseEntity.ok(registrationService.getRegistrationByComp(compIdx, RegistrationStatus.CANCELLED, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationDetailedDTO> getRegistrationById(@PathVariable Long id, @JwtToken String token) throws Exception {
        return ResponseEntity.ok(registrationService.getRegistrationById(id, token));
    }

    @PostMapping("")
    public ResponseEntity<Void> createRegistration(@RequestBody RegistrationCreateDTO registrationCreateDTO, @JwtToken String token) {
        registrationService.createRegistration(registrationCreateDTO, token);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/events")
    public ResponseEntity<Void> updateRegistrationEvents(@RequestBody RegistrationUpdateEventsDTO registrationUpdateEventsDTO, @JwtToken String token) throws Exception {
        registrationService.updateRegistration(registrationUpdateEventsDTO, token);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveRegistration(@PathVariable Long id) {
        registrationService.approveRegistration(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long id) {
        registrationService.cancelRegistration(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<Void> refundRegistration(@PathVariable Long id) {
        registrationService.refundRegistration(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);
        return ResponseEntity.ok().build();
    }
}
