package com.track.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class TrackingNumberRequest {
    @NotBlank(message = "Origin country ID is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Origin country ID must be in ISO 3166-1 alpha-2 format")
    private String originCountryId;

    @NotBlank(message = "Destination country ID is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Destination country ID must be in ISO 3166-1 alpha-2 format")
    private String destinationCountryId;

    @DecimalMin(value = "0.001", message = "Weight must be greater than 0")
    @DecimalMax(value = "999999.999", message = "Weight must be less than 1,000,000")
    private Double weight;

    private OffsetDateTime createdAt;

    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
            message = "Customer ID must be a valid UUID")
    private String customerId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer slug is required")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Customer slug must be in kebab-case format")
    private String customerSlug;
}