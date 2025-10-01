// repo/NotificationRepository.java
package com.example.eventplanner.repo;
import com.example.eventplanner.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findTop20ByRecipientOrderByCreatedAtDesc(Staff recipient);
    long countByRecipientAndReadFlagFalse(Staff recipient);
}
