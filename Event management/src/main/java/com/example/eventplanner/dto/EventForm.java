// dto/EventForm.java
package com.example.eventplanner.dto;

import com.example.eventplanner.domain.enums.EventCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter
public class EventForm {
    @NotBlank private String title;
    @NotNull  private LocalDate eventDate;
    @NotBlank private String venue;
    @NotNull  private EventCategory category;
    @NotBlank @Size(max=2000) private String description;
}
