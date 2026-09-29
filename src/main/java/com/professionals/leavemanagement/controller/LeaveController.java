package com.professionals.leavemanagement.controller;

import com.professionals.leavemanagement.dto.ApiResponse;
import com.professionals.leavemanagement.dto.LeaveRequestDTO;
import com.professionals.leavemanagement.dto.LeaveResponseDTO;
import com.professionals.leavemanagement.dto.UpdateLeaveStatusDTO;
import com.professionals.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveResponseDTO>>> getAllLeaves() {
        List<LeaveResponseDTO> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success(leaves, "Leave requests retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> getLeaveById(@PathVariable Long id) {
        LeaveResponseDTO leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(ApiResponse.success(leave, "Leave request retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> createLeave(@Valid @RequestBody LeaveRequestDTO requestDTO) {
        LeaveResponseDTO created = leaveService.createLeave(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Leave request created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> updateLeave(@PathVariable Long id,
                                                                       @Valid @RequestBody LeaveRequestDTO requestDTO) {
        LeaveResponseDTO updated = leaveService.updateLeave(id, requestDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave request updated successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> updateLeaveStatus(@PathVariable Long id,
                                                                             @Valid @RequestBody UpdateLeaveStatusDTO statusDTO) {
        LeaveResponseDTO updated = leaveService.updateLeaveStatus(id, statusDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave request status updated successfully"));
    }

    // Deliberately not wrapped in ApiResponse: a 204 response must not carry a body per HTTP semantics.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}
