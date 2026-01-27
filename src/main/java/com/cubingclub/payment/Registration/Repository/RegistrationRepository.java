package com.cubingclub.payment.Registration.Repository;

import com.cubingclub.payment.Registration.Entity.Registration;
import com.cubingclub.payment.Registration.Entity.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Page<Registration> findByCompetitionIdAndRegistrationStatusOrderByCreatedAtDesc(Long competitionId, RegistrationStatus registrationStatus, Pageable pageable);

    Page<Registration> findByCckIdOrderByCreatedAtDesc(String cckId, Pageable pageable);

    Boolean existsByCckIdAndCompetitionIdAndRegistrationStatusNot(String cckId, Long competitionId, RegistrationStatus registrationStatus);

    Optional<Registration> findByIdAndCckId(Long id, String cckId);
}
