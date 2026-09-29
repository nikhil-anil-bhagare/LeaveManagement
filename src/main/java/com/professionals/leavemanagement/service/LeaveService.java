package com.professionals.leavemanagement.service;

import com.professionals.leavemanagement.dto.LeaveRequestDTO;
import com.professionals.leavemanagement.dto.LeaveResponseDTO;
import com.professionals.leavemanagement.dto.UpdateLeaveStatusDTO;

import java.util.List;

public interface LeaveService {

    List<LeaveResponseDTO> getAllLeaves();

    LeaveResponseDTO getLeaveById(Long id);

    LeaveResponseDTO createLeave(LeaveRequestDTO requestDTO);

    LeaveResponseDTO updateLeave(Long id, LeaveRequestDTO requestDTO);

    LeaveResponseDTO updateLeaveStatus(Long id, UpdateLeaveStatusDTO statusDTO);

    void deleteLeave(Long id);
}
