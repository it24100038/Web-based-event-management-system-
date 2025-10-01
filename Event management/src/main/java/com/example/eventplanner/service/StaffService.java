package com.example.eventplanner.service;

import com.example.eventplanner.domain.Staff;
import com.example.eventplanner.repo.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository repo;

    // --- existing method used by PlannerController ---
    @Transactional(readOnly = true)
    public Staff byEmail(String email) {
        return repo.findByEmail(email).orElseThrow();
    }

    // --- minimal helpers for Admin dashboard (safe to add) ---
    @Transactional(readOnly = true)
    public List<Staff> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Staff findById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Transactional
    public Staff save(Staff staff) {
        return repo.save(staff);
    }
}
