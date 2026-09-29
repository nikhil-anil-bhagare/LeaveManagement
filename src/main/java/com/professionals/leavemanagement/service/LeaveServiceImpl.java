package com.professionals.leavemanagement.service;

import com.professionals.leavemanagement.config.LeaveManagementProperties;
import com.professionals.leavemanagement.dto.LeaveRequestDTO;
import com.professionals.leavemanagement.dto.LeaveResponseDTO;
import com.professionals.leavemanagement.dto.UpdateLeaveStatusDTO;
import com.professionals.leavemanagement.exception.InvalidLeaveRequestException;
import com.professionals.leavemanagement.exception.LeaveNotFoundException;
import com.professionals.leavemanagement.model.LeaveRequest;
import com.professionals.leavemanagement.model.LeaveStatus;
import com.professionals.leavemanagement.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeaveManagementProperties properties;

    public LeaveServiceImpl(LeaveRepository leaveRepository, LeaveManagementProperties properties) {
        this.leaveRepository = leaveRepository;
        this.properties = properties;
    }

    @Override
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public LeaveResponseDTO getLeaveById(Long id) {
        LeaveRequest leaveRequest = leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(id));
        return toResponseDTO(leaveRequest);
    }

    @Override
    public LeaveResponseDTO createLeave(LeaveRequestDTO requestDTO) {
        validateDateRange(requestDTO);
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(requestDTO.employeeId());
        leaveRequest.setLeaveType(requestDTO.leaveType());
        leaveRequest.setStartDate(requestDTO.startDate());
        leaveRequest.setEndDate(requestDTO.endDate());
        leaveRequest.setReason(requestDTO.reason());
        // Every new request starts PENDING; approval workflow is handled separately via updateLeaveStatus().
        leaveRequest.setStatus(LeaveStatus.PENDING);

        LeaveRequest saved = leaveRepository.save(leaveRequest);
        return toResponseDTO(saved);
    }

    @Override
    public LeaveResponseDTO updateLeave(Long id, LeaveRequestDTO requestDTO) {
        validateDateRange(requestDTO);
        LeaveRequest existing = leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(id));

        existing.setEmployeeId(requestDTO.employeeId());
        existing.setLeaveType(requestDTO.leaveType());
        existing.setStartDate(requestDTO.startDate());
        existing.setEndDate(requestDTO.endDate());
        existing.setReason(requestDTO.reason());

        LeaveRequest updated = leaveRepository.save(existing);
        return toResponseDTO(updated);
    }

    @Override
    public LeaveResponseDTO updateLeaveStatus(Long id, UpdateLeaveStatusDTO statusDTO) {
        LeaveRequest existing = leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(id));

        existing.setStatus(statusDTO.status());

        LeaveRequest updated = leaveRepository.save(existing);
        return toResponseDTO(updated);
    }

    @Override
    public void deleteLeave(Long id) {
        if (!leaveRepository.existsById(id)) {
            throw new LeaveNotFoundException(id);
        }
        leaveRepository.deleteById(id);
    }

    private void validateDateRange(LeaveRequestDTO requestDTO) {
        if (requestDTO.endDate().isBefore(requestDTO.startDate())) {
            throw new InvalidLeaveRequestException("endDate must not be before startDate");
        }
        long durationDays = ChronoUnit.DAYS.between(requestDTO.startDate(), requestDTO.endDate()) + 1;
        if (durationDays > properties.getMaxLeaveDurationDays()) {
            throw new InvalidLeaveRequestException(
                    "Leave duration exceeds the maximum allowed " + properties.getMaxLeaveDurationDays() + " days");
        }
    }

    private LeaveResponseDTO toResponseDTO(LeaveRequest leaveRequest) {
        return new LeaveResponseDTO(
                leaveRequest.getId(),
                leaveRequest.getEmployeeId(),
                leaveRequest.getLeaveType(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getReason(),
                leaveRequest.getStatus()
        );
    }
}
