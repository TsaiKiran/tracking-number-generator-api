package com.track.controller;

import com.track.dto.TrackingNumberRequest;
import com.track.dto.TrackingNumberResponse;
import com.track.service.TrackingNumberGenerator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1")
@Validated
@Slf4j
public class TrackingNumberController {

    private final TrackingNumberGenerator trackingNumberGenerator;

    @Autowired
    public TrackingNumberController(TrackingNumberGenerator trackingNumberGenerator) {
        this.trackingNumberGenerator = trackingNumberGenerator;
    }

    @GetMapping("/next-tracking-number")
    public TrackingNumberResponse generateTrackingNumber(
            @RequestParam @Pattern(regexp = "^[A-Z]{2}$", message = "Origin country ID must be in ISO 3166-1 alpha-2 format")
            String originCountryId,

            @RequestParam @Pattern(regexp = "^[A-Z]{2}$", message = "Destination country ID must be in ISO 3166-1 alpha-2 format")
            String destinationCountryId,

            @RequestParam(required = false)
            @DecimalMin(value = "0.001", message = "Weight must be greater than 0")
            Double weight,

            @RequestParam(required = false)
            OffsetDateTime createdAt,

            @RequestParam(required = false)
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                    message = "Customer ID must be a valid UUID")
            String customerId,

            @RequestParam
                    @NotBlank(message = "Name cannot be blank")
            String customerName,

            @RequestParam
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Customer slug should be in kebab-case format")
            String customerSlug) {

        TrackingNumberRequest request = TrackingNumberRequest.builder()
                .originCountryId(originCountryId)
                .destinationCountryId(destinationCountryId)
                .weight(weight)
                .createdAt(createdAt)
                .customerId(customerId)
                .customerName(customerName)
                .customerSlug(customerSlug)
                .build();


        String trackingNumber = trackingNumberGenerator.generateTrackingNumber(request);

        log.info(trackingNumber);
        return TrackingNumberResponse.builder()
                .trackingNumber(trackingNumber)
                .createdAt(OffsetDateTime.now())
                .customerSlug(customerSlug)
                .build();
    }
}