package com.inventory.product.exception;

import java.time.LocalDateTime;
public record ErrorDetails(
        LocalDateTime timestamp,
        String message,
        String details,
        int status
) {}