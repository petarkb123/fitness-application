package project.fitnessapplication.restday.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRestDayRequest(
        @NotNull LocalDate date,
        String notes
) {}
