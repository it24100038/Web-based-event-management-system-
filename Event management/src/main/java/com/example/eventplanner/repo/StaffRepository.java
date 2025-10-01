// repo/StaffRepository.java
package com.example.eventplanner.repo;
import com.example.eventplanner.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StaffRepository extends JpaRepository<Staff,Long> {
    Optional<Staff> findByEmail(String email);
}
