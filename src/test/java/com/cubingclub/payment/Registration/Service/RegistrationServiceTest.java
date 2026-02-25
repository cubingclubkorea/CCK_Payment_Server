package com.cubingclub.payment.Registration.Service;

import com.cubingclub.payment.Auth.DTO.AccountDTO;
import com.cubingclub.payment.Auth.Service.AuthService;
import com.cubingclub.payment.Exception.Entity.ErrorCode;
import com.cubingclub.payment.Exception.Exceptions.CustomizedException;
import com.cubingclub.payment.Mail.Service.MailAsyncService;
import com.cubingclub.payment.Registration.Component.ClientComponent;
import com.cubingclub.payment.Registration.DTO.*;
import com.cubingclub.payment.Registration.Entity.PaymentStatus;
import com.cubingclub.payment.Registration.Entity.Registration;
import com.cubingclub.payment.Registration.Entity.RegistrationStatus;
import com.cubingclub.payment.Registration.Repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrationService 테스트")
class RegistrationServiceTest {

    @Mock
    private ClientComponent clientComponent;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AuthService authService;

    @Mock
    private MailAsyncService mailAsyncService;

    @Mock
    private SpringTemplateEngine springTemplateEngine;

    @InjectMocks
    private RegistrationService registrationService;

    private AccountDTO accountDTO;
    private CompetitionDTO competitionDTO;
    private RegistrationCreateDTO registrationCreateDTO;
    private Registration registration;

    @BeforeEach
    void setUp() {
        // AccountDTO 설정
        accountDTO = new AccountDTO();
        accountDTO.setCckId("test-cck-id");
        accountDTO.setName("홍길동");
        accountDTO.setEnName("Hong Gildong");
        accountDTO.setEmail("test@example.com");
        accountDTO.setPhoneNum("010-1234-5678");
        accountDTO.setBirthDate(LocalDate.of(2000, 1, 1));

        // EventFeeDTO 설정
        EventFeeDTO eventFeeDTO = new EventFeeDTO();
        eventFeeDTO.setIdx(1);
        eventFeeDTO.setEventName("333");
        eventFeeDTO.setEventFee(5000);

        EventFeeDTO eventFeeDTO2 = new EventFeeDTO();
        eventFeeDTO2.setIdx(2);
        eventFeeDTO2.setEventName("222");
        eventFeeDTO2.setEventFee(3000);

        // CompetitionDTO 설정
        competitionDTO = new CompetitionDTO();
        competitionDTO.setIdx(1);
        competitionDTO.setCompName("Test Competition");
        competitionDTO.setCompDateStart(LocalDate.of(2026, 3, 1));
        competitionDTO.setCompDateEnd(LocalDate.of(2026, 3, 2));
        competitionDTO.setJoinDateStart(LocalDateTime.of(2026, 2, 1, 0, 0));
        competitionDTO.setJoinDateEnd(LocalDateTime.of(2026, 2, 28, 23, 59));
        competitionDTO.setBaseFee(10000);
        competitionDTO.setLocation("Seoul");
        competitionDTO.setMaxParticipate(100);
        competitionDTO.setEventFeeList(Arrays.asList(eventFeeDTO, eventFeeDTO2));

        // RegistrationCreateDTO 설정
        registrationCreateDTO = new RegistrationCreateDTO();
        registrationCreateDTO.setCompetitionId(1L);
        registrationCreateDTO.setCompetitionName("Test Competition");
        registrationCreateDTO.setPhone("010-1234-5678");
        registrationCreateDTO.setSelectedEvents(Arrays.asList("333", "222"));

        // Registration 설정
        registration = Registration.builder()
                .id(1L)
                .competitionId(1L)
                .competitionName("Test Competition")
                .name("홍길동")
                .enName("Hong Gildong")
                .email("test@example.com")
                .phone("010-1234-5678")
                .cckId("test-cck-id")
                .birthDate(LocalDate.of(2000, 1, 1))
                .selectedEvents(new ArrayList<>(Arrays.asList("333", "222")))
                .totalFee(18000)
                .paymentStatus(PaymentStatus.PENDING)
                .registrationStatus(RegistrationStatus.PENDING)
                .needRfCard(false)
                .build();
    }

    @Test
    @DisplayName("대회별 등록 목록 조회")
    void getRegistrationByComp_Success() {
        // given
        Long competitionId = 1L;
        RegistrationStatus status = RegistrationStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);
        List<Registration> registrations = Collections.singletonList(registration);
        Page<Registration> page = new PageImpl<>(registrations, pageable, 1);

        when(registrationRepository.findByCompetitionIdAndRegistrationStatusOrderByCreatedAtDesc(
                competitionId, status, pageable)).thenReturn(page);

        // when
        Page<RegistrationDTO> result = registrationService.getRegistrationByComp(competitionId, status, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(registrationRepository).findByCompetitionIdAndRegistrationStatusOrderByCreatedAtDesc(
                competitionId, status, pageable);
    }

    @Test
    @DisplayName("사용자별 등록 목록 조회")
    void getRegistrationByUser_Success() {
        // given
        String token = "test-token";
        Pageable pageable = PageRequest.of(0, 10);
        List<Registration> registrations = Collections.singletonList(registration);
        Page<Registration> page = new PageImpl<>(registrations, pageable, 1);

        when(authService.getMyInfo(token)).thenReturn(accountDTO);
        when(registrationRepository.findByCckIdOrderByCreatedAtDesc(accountDTO.getCckId(), pageable))
                .thenReturn(page);

        // when
        Page<RegistrationDTO> result = registrationService.getRegistrationByUser(token, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(authService).getMyInfo(token);
        verify(registrationRepository).findByCckIdOrderByCreatedAtDesc(accountDTO.getCckId(), pageable);
    }

    @Test
    @DisplayName("등록 상세 정보 조회 성공")
    void getRegistrationById_Success() throws Exception {
        // given
        Long id = 1L;
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LWNjay1pZCJ9.signature";

        when(authService.extractCckId(token)).thenReturn("test-cck-id");
        when(registrationRepository.findByIdAndCckId(id, "test-cck-id"))
                .thenReturn(Optional.of(registration));

        // when
        RegistrationDetailedDTO result = registrationService.getRegistrationById(id, token);

        // then
        assertThat(result).isNotNull();
        verify(authService).extractCckId(token);
        verify(registrationRepository).findByIdAndCckId(id, "test-cck-id");
    }

    @Test
    @DisplayName("등록 상세 정보 조회 실패 - 등록 정보 없음")
    void getRegistrationById_NotFound() throws Exception {
        // given
        Long id = 1L;
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LWNjay1pZCJ9.signature";

        when(authService.extractCckId(token)).thenReturn("test-cck-id");
        when(registrationRepository.findByIdAndCckId(id, "test-cck-id"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> registrationService.getRegistrationById(id, token))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_NOT_FOUND);
    }

    @Test
    @DisplayName("등록 생성 성공")
    void createRegistration_Success() {
        // given
        String token = "test-token";

        when(authService.getMyInfo(token)).thenReturn(accountDTO);
        when(clientComponent.getCompetitionDTO(1L)).thenReturn(competitionDTO);
        when(registrationRepository.findByCompetitionIdAndRegistrationStatusNot(
                1L, RegistrationStatus.CANCELLED)).thenReturn(Collections.emptyList());
        when(registrationRepository.existsByCckIdAndCompetitionIdAndRegistrationStatusNot(
                accountDTO.getCckId(), 1L, RegistrationStatus.CANCELLED)).thenReturn(false);
        when(authService.existRfid(accountDTO.getCckId(), token)).thenReturn(true);
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);
        when(springTemplateEngine.process(anyString(), any())).thenReturn("HTML content");
        doNothing().when(mailAsyncService).sendEmail(anyString(), anyString(), anyString());

        // when
        registrationService.createRegistration(registrationCreateDTO, token);

        // then
        verify(authService).getMyInfo(token);
        verify(clientComponent, atLeast(1)).getCompetitionDTO(1L);
        verify(registrationRepository).save(any(Registration.class));
        verify(mailAsyncService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("등록 생성 실패 - 정원 초과")
    void createRegistration_MaxParticipate() {
        // given
        String token = "test-token";
        competitionDTO.setMaxParticipate(0);

        when(authService.getMyInfo(token)).thenReturn(accountDTO);
        when(clientComponent.getCompetitionDTO(1L)).thenReturn(competitionDTO);
        when(registrationRepository.findByCompetitionIdAndRegistrationStatusNot(
                1L, RegistrationStatus.CANCELLED)).thenReturn(Collections.singletonList(registration));

        // when & then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationCreateDTO, token))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_MAX_PARTICIPATE);
    }

    @Test
    @DisplayName("등록 생성 실패 - 이미 등록함")
    void createRegistration_AlreadyExists() {
        // given
        String token = "test-token";

        when(authService.getMyInfo(token)).thenReturn(accountDTO);
        when(clientComponent.getCompetitionDTO(1L)).thenReturn(competitionDTO);
        when(registrationRepository.findByCompetitionIdAndRegistrationStatusNot(
                1L, RegistrationStatus.CANCELLED)).thenReturn(Collections.emptyList());
        when(registrationRepository.existsByCckIdAndCompetitionIdAndRegistrationStatusNot(
                accountDTO.getCckId(), 1L, RegistrationStatus.CANCELLED)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationCreateDTO, token))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("등록 생성 실패 - 등록 기간 아님")
    void createRegistration_NotInTime() {
        // given
        String token = "test-token";
        competitionDTO.setJoinDateStart(LocalDateTime.of(2026, 3, 1, 0, 0));
        competitionDTO.setJoinDateEnd(LocalDateTime.of(2026, 3, 31, 23, 59));

        when(authService.getMyInfo(token)).thenReturn(accountDTO);
        when(clientComponent.getCompetitionDTO(1L)).thenReturn(competitionDTO);
        when(registrationRepository.findByCompetitionIdAndRegistrationStatusNot(
                1L, RegistrationStatus.CANCELLED)).thenReturn(Collections.emptyList());
        when(registrationRepository.existsByCckIdAndCompetitionIdAndRegistrationStatusNot(
                accountDTO.getCckId(), 1L, RegistrationStatus.CANCELLED)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationCreateDTO, token))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_NOT_IN_TIME);
    }

    @Test
    @DisplayName("등록 생성 실패 - 잘못된 종목 선택")
    void createRegistration_InvalidEvents() {
        // given
        String token = "test-token";
        registrationCreateDTO.setSelectedEvents(Arrays.asList("333", "444"));

        when(authService.getMyInfo(token)).thenReturn(accountDTO);
        when(clientComponent.getCompetitionDTO(1L)).thenReturn(competitionDTO);
        when(registrationRepository.findByCompetitionIdAndRegistrationStatusNot(
                1L, RegistrationStatus.CANCELLED)).thenReturn(Collections.emptyList());
        when(registrationRepository.existsByCckIdAndCompetitionIdAndRegistrationStatusNot(
                accountDTO.getCckId(), 1L, RegistrationStatus.CANCELLED)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> registrationService.createRegistration(registrationCreateDTO, token))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_INVALID_SELECTED_EVENTS);
    }

    @Test
    @DisplayName("등록 정보 업데이트 성공")
    void updateRegistration_Success() throws Exception {
        // given
        RegistrationUpdateEventsDTO updateDTO = new RegistrationUpdateEventsDTO();
        updateDTO.setRegistrationId(1L);
        updateDTO.setSelectedEvents(List.of("333"));
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LWNjay1pZCJ9.signature";

        when(authService.extractCckId(token)).thenReturn("test-cck-id");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(clientComponent.getCompetitionDTO(1L)).thenReturn(competitionDTO);

        // when
        registrationService.updateRegistration(updateDTO, token);

        // then
        verify(registrationRepository).findById(1L);
        verify(clientComponent).getCompetitionDTO(1L);
    }

    @Test
    @DisplayName("등록 정보 업데이트 실패 - 권한 없음")
    void updateRegistration_Forbidden() throws Exception {
        // given
        RegistrationUpdateEventsDTO updateDTO = new RegistrationUpdateEventsDTO();
        updateDTO.setRegistrationId(1L);
        updateDTO.setSelectedEvents(List.of("333"));
        String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvdGhlci1jY2staWQifQ.signature";

        when(authService.extractCckId(token)).thenReturn("other-cck-id");
        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));

        // when & then
        assertThatThrownBy(() -> registrationService.updateRegistration(updateDTO, token))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_FORBIDDEN);
    }

    @Test
    @DisplayName("등록 승인 성공")
    void approveRegistration_Success() {
        // given
        Long id = 1L;
        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when
        registrationService.approveRegistration(id);

        // then
        verify(registrationRepository).findById(id);
    }

    @Test
    @DisplayName("등록 승인 실패 - 잘못된 상태 전환")
    void approveRegistration_InvalidStateTransition() {
        // given
        Long id = 1L;
        registration.updatePaymentStatus(PaymentStatus.CANCELLED);

        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when & then
        assertThatThrownBy(() -> registrationService.approveRegistration(id))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_INVALID_PAYMENT_STATUS_FLOW);
    }

    @Test
    @DisplayName("등록 취소 성공")
    void cancelRegistration_Success() {
        // given
        Long id = 1L;
        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when
        registrationService.cancelRegistration(id);

        // then
        verify(registrationRepository).findById(id);
    }

    @Test
    @DisplayName("환불 처리 성공")
    void refundRegistration_Success() {
        // given
        Long id = 1L;
        registration.updatePaymentStatus(PaymentStatus.CANCELLED);
        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when
        registrationService.refundRegistration(id);

        // then
        verify(registrationRepository).findById(id);
    }

    @Test
    @DisplayName("환불 처리 실패 - 잘못된 상태")
    void refundRegistration_InvalidState() {
        // given
        Long id = 1L;
        registration.updatePaymentStatus(PaymentStatus.PENDING);
        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when & then
        assertThatThrownBy(() -> registrationService.refundRegistration(id))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_INVALID_PAYMENT_STATUS_FLOW);
    }

    @Test
    @DisplayName("등록 삭제 성공")
    void deleteRegistration_Success() {
        // given
        Long id = 1L;
        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when
        registrationService.deleteRegistration(id);

        // then
        verify(registrationRepository).findById(id);
        verify(registrationRepository).deleteById(id);
    }

    @Test
    @DisplayName("등록 삭제 실패 - 이미 결제됨")
    void deleteRegistration_AlreadyPaid() {
        // given
        Long id = 1L;
        registration.updatePaymentStatus(PaymentStatus.PAID);
        when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        // when & then
        assertThatThrownBy(() -> registrationService.deleteRegistration(id))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_ALREADY_PAID);
    }

    @Test
    @DisplayName("등록 삭제 실패 - 등록 정보 없음")
    void deleteRegistration_NotFound() {
        // given
        Long id = 1L;
        when(registrationRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> registrationService.deleteRegistration(id))
                .isInstanceOf(CustomizedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REGISTRATION_NOT_FOUND);
    }
}
