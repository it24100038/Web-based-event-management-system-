// repo/EventRepository.java
package com.example.eventplanner.repo;
import java.util.Optional;
import java.util.List;
import com.example.eventplanner.domain.*;
import com.example.eventplanner.domain.enums.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event,Long> {
    // NEW: for My Events default list
    List<Event> findByPlannerOrderByCreatedAtDesc(Staff planner);

    // NEW: secure lookup
    Optional<Event> findByIdAndPlanner(Long id, Staff planner);

        List<Event> findTop10ByPlannerOrderByCreatedAtDesc(Staff planner);
    List<Event> findByPlannerAndStatus(Staff planner, EventStatus status);

    @Query("""
    select e from Event e
    where e.planner = :planner
      and (:status is null or e.status = :status)
      and (:category is null or e.category = :category)
      and (:fromDate is null or e.eventDate >= :fromDate)
    order by e.createdAt desc
  """)
    List<Event> filter(@Param("planner") Staff planner,
                       @Param("status") EventStatus status,
                       @Param("category") EventCategory category,
                       @Param("fromDate") LocalDate fromDate);

    long countByPlanner(Staff planner);
    long countByPlannerAndStatus(Staff planner, EventStatus status);
}
