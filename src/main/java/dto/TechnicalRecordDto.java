package com.netkrow.backend.dto;

public record TechnicalRecordDto(
        String equipo,
        String marca,
        String serial,
        String brazalete,
        String pilas,
        String revision,
        String mantenimiento,
        String limpieza,
        String calibracion,
        String notasDiagnostico
) {}