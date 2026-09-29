package com.professionals.leavemanagement.repository;

import com.professionals.leavemanagement.model.LeaveRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryLeaveRepository implements LeaveRepository {

    // ConcurrentHashMap + AtomicLong: safe for concurrent request handling without external synchronization.
    private final Map<Long, LeaveRequest> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public List<LeaveRequest> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public Optional<LeaveRequest> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public LeaveRequest save(LeaveRequest leaveRequest) {
        if (leaveRequest.getId() == null) {
            leaveRequest.setId(idGenerator.incrementAndGet());
        }
        store.put(leaveRequest.getId(), leaveRequest);
        return leaveRequest;
    }

    @Override
    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
