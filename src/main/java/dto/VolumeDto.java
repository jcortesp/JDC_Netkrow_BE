// src/main/java/com/netkrow/backend/dto/VolumeDto.java
package com.netkrow.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VolumeDto(
        LocalDate fecha,
        String equipo,
        String estado,
        long totalRemisiones,
        BigDecimal totalValor
) {}
