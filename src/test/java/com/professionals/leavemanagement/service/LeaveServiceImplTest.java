package com.professionals.leavemanagement.service;

import com.professionals.leavemanagement.config.LeaveManagementProperties;
import com.professionals.leavemanagement.dto.LeaveRequestDTO;
import com.professionals.leavemanagement.dto.LeaveResponseDTO;
import com.professionals.leavemanagement.dto.UpdateLeaveStatusDTO;
import com.professionals.leavemanagement.exception.InvalidLeaveRequestException;
import com.professionals.leavemanagement.exception.LeaveNotFoundException;
import com.professionals.leavemanagement.model.LeaveRequest;
import com.professionals.leavemanagement.model.LeaveStatus;
import com.professionals.leavemanagement.model.LeaveType;
import com.professionals.leavemanagement.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRepository leaveRepository;

    private LeaveServiceImpl leaveService;

    @BeforeEach
    void setUp() {
        LeaveManagementProperties properties = new LeaveManagementProperties();
        properties.setMaxLeaveDurationDays(30);
        leaveService = new LeaveServiceImpl(leaveRepository, properties);
    }

    private LeaveRequestDTO validRequestDTO() {
        return new LeaveRequestDTO(
                "EMP001",
                LeaveType.SICK,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "Not feeling well"
        );
    }

    private LeaveRequest sampleLeaveRequest(Long id) {
        return new LeaveRequest(
                id,
                "EMP001",
                LeaveType.SICK,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "Not feeling well",
                LeaveStatus.PENDING
        );
    }

    @Test
    void getAllLeaves_returnsMappedResponseList() {
        when(leaveRepository.findAll()).thenReturn(List.of(sampleLeaveRequest(1L), sampleLeaveRequest(2L)));

        List<LeaveResponseDTO> result = leaveService.getAllLeaves();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(2L);
    }

    @Test
    void getLeaveById_whenExists_returnsResponse() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(sampleLeaveRequest(1L)));

        LeaveResponseDTO result = leaveService.getLeaveById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo("EMP001");
    }

    @Test
    void getLeaveById_whenNotFound_throwsException() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.getLeaveById(99L))
                .isInstanceOf(LeaveNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createLeave_savesWithPendingStatus() {
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        LeaveResponseDTO result = leaveService.createLeave(validRequestDTO());

        ArgumentCaptor<LeaveRequest> captor = ArgumentCaptor.forClass(LeaveRequest.class);
        verify(leaveRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
    }

    @Test
    void createLeave_whenEndDateBeforeStartDate_throwsException() {
        LeaveRequestDTO invalid = new LeaveRequestDTO(
                "EMP001", LeaveType.SICK,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(1),
                "Invalid range"
        );

        assertThatThrownBy(() -> leaveService.createLeave(invalid))
                .isInstanceOf(InvalidLeaveRequestException.class);

        verify(leaveRepository, never()).save(any());
    }

    @Test
    void createLeave_whenDurationExceedsMax_throwsException() {
        LeaveRequestDTO tooLong = new LeaveRequestDTO(
                "EMP001", LeaveType.SICK,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(40),
                "Too long"
        );

        assertThatThrownBy(() -> leaveService.createLeave(tooLong))
                .isInstanceOf(InvalidLeaveRequestException.class)
                .hasMessageContaining("maximum");
    }

    @Test
    void updateLeave_whenExists_updatesFields() {
        LeaveRequest existing = sampleLeaveRequest(1L);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestDTO update = new LeaveRequestDTO(
                "EMP002", LeaveType.CASUAL,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(4),
                "Updated reason"
        );

        LeaveResponseDTO result = leaveService.updateLeave(1L, update);

        assertThat(result.employeeId()).isEqualTo("EMP002");
        assertThat(result.leaveType()).isEqualTo(LeaveType.CASUAL);
        assertThat(result.reason()).isEqualTo("Updated reason");
        assertThat(result.status()).isEqualTo(LeaveStatus.PENDING);
    }

    @Test
    void updateLeave_whenNotFound_throwsException() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.updateLeave(99L, validRequestDTO()))
                .isInstanceOf(LeaveNotFoundException.class);
    }

    @Test
    void updateLeaveStatus_whenExists_updatesStatus() {
        LeaveRequest existing = sampleLeaveRequest(1L);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveResponseDTO result = leaveService.updateLeaveStatus(1L, new UpdateLeaveStatusDTO(LeaveStatus.APPROVED));

        assertThat(result.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(result.employeeId()).isEqualTo("EMP001");
    }

    @Test
    void updateLeaveStatus_whenNotFound_throwsException() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.updateLeaveStatus(99L, new UpdateLeaveStatusDTO(LeaveStatus.REJECTED)))
                .isInstanceOf(LeaveNotFoundException.class);
    }

    @Test
    void deleteLeave_whenExists_deletesSuccessfully() {
        when(leaveRepository.existsById(1L)).thenReturn(true);

        leaveService.deleteLeave(1L);

        verify(leaveRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteLeave_whenNotFound_throwsException() {
        when(leaveRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> leaveService.deleteLeave(99L))
                .isInstanceOf(LeaveNotFoundException.class);

        verify(leaveRepository, never()).deleteById(any());
    }
}
