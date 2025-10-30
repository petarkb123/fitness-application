package project.fitnessapplication.restday.dto;

import project.fitnessapplication.restday.model.RestDay;

import java.time.LocalDate;
import java.util.UUID;

public record RestDayDto(
        UUID id,
        LocalDate date,
        String notes
) {
    public static RestDayDto fromEntity(RestDay restDay) {
        return new RestDayDto(
                restDay.getId(),
                restDay.getDate(),
                restDay.getNotes()
        );
    }
}
