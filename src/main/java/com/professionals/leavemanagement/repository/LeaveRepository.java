package com.professionals.leavemanagement.repository;

import com.professionals.leavemanagement.model.LeaveRequest;

import java.util.List;
import java.util.Optional;

public interface LeaveRepository {

    List<LeaveRequest> findAll();

    Optional<LeaveRequest> findById(Long id);

    LeaveRequest save(LeaveRequest leaveRequest);

    boolean deleteById(Long id);

    boolean existsById(Long id);
}
