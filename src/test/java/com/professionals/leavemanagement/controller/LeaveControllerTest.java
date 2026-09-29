package com.professionals.leavemanagement.controller;

import com.professionals.leavemanagement.dto.LeaveRequestDTO;
import com.professionals.leavemanagement.dto.LeaveResponseDTO;
import com.professionals.leavemanagement.dto.UpdateLeaveStatusDTO;
import com.professionals.leavemanagement.exception.LeaveNotFoundException;
import com.professionals.leavemanagement.model.LeaveStatus;
import com.professionals.leavemanagement.model.LeaveType;
import com.professionals.leavemanagement.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveService leaveService;

    private LeaveResponseDTO sampleResponse(Long id) {
        return new LeaveResponseDTO(
                id, "EMP001", LeaveType.SICK,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "Not feeling well", LeaveStatus.PENDING
        );
    }

    private LeaveRequestDTO sampleRequest() {
        return new LeaveRequestDTO(
                "EMP001", LeaveType.SICK,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "Not feeling well"
        );
    }

    @Test
    void getAllLeaves_returnsOkWithList() throws Exception {
        when(leaveService.getAllLeaves()).thenReturn(List.of(sampleResponse(1L), sampleResponse(2L)));

        mockMvc.perform(get("/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getLeaveById_whenExists_returnsOk() throws Exception {
        when(leaveService.getLeaveById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/leaves/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.employeeId").value("EMP001"));
    }

    @Test
    void getLeaveById_whenNotFound_returns404() throws Exception {
        when(leaveService.getLeaveById(99L)).thenThrow(new LeaveNotFoundException(99L));

        mockMvc.perform(get("/leaves/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Leave request not found with id: 99"))
                .andExpect(jsonPath("$.errorCode").value("LEAVE_NOT_FOUND"));
    }

    @Test
    void createLeave_withValidPayload_returns201() throws Exception {
        when(leaveService.createLeave(any(LeaveRequestDTO.class))).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createLeave_withMissingFields_returns400() throws Exception {
        LeaveRequestDTO invalid = new LeaveRequestDTO(null, null, null, null, null);

        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void updateLeave_whenExists_returnsOk() throws Exception {
        when(leaveService.updateLeave(eq(1L), any(LeaveRequestDTO.class))).thenReturn(sampleResponse(1L));

        mockMvc.perform(put("/leaves/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void updateLeave_whenNotFound_returns404() throws Exception {
        when(leaveService.updateLeave(eq(99L), any(LeaveRequestDTO.class)))
                .thenThrow(new LeaveNotFoundException(99L));

        mockMvc.perform(put("/leaves/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateLeaveStatus_whenExists_returnsOk() throws Exception {
        LeaveResponseDTO approved = new LeaveResponseDTO(
                1L, "EMP001", LeaveType.SICK,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "Not feeling well", LeaveStatus.APPROVED
        );
        when(leaveService.updateLeaveStatus(eq(1L), any(UpdateLeaveStatusDTO.class))).thenReturn(approved);

        mockMvc.perform(patch("/leaves/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLeaveStatusDTO(LeaveStatus.APPROVED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void updateLeaveStatus_withMissingStatus_returns400() throws Exception {
        mockMvc.perform(patch("/leaves/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLeaveStatusDTO(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    @Test
    void updateLeaveStatus_whenNotFound_returns404() throws Exception {
        when(leaveService.updateLeaveStatus(eq(99L), any(UpdateLeaveStatusDTO.class)))
                .thenThrow(new LeaveNotFoundException(99L));

        mockMvc.perform(patch("/leaves/{id}/status", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLeaveStatusDTO(LeaveStatus.REJECTED))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("LEAVE_NOT_FOUND"));
    }

    @Test
    void deleteLeave_whenExists_returns204() throws Exception {
        doNothing().when(leaveService).deleteLeave(1L);

        mockMvc.perform(delete("/leaves/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLeave_whenNotFound_returns404() throws Exception {
        doThrow(new LeaveNotFoundException(99L)).when(leaveService).deleteLeave(99L);

        mockMvc.perform(delete("/leaves/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
