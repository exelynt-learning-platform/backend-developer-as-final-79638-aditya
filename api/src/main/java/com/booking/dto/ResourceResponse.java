package com.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ResourceResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean isActive;
}