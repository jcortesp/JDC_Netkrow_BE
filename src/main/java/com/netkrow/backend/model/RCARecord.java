package com.netkrow.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rca_records")
public class RCARecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String errorCode; // Ejemplo: NPE-130, 500, etc

    @Column
    private String omsComponent; // Ej: "Sterling API", "Clase Java", etc

    @Column(columnDefinition = "TEXT")
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String resolutionSteps;

    @Column
    private String tags; // Comma separated (o como JSON array si prefieres)

    @Column
    private String exampleStacktrace; // Opcional

    // Getters y setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getOmsComponent() { return omsComponent; }
    public void setOmsComponent(String omsComponent) { this.omsComponent = omsComponent; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getResolutionSteps() { return resolutionSteps; }
    public void setResolutionSteps(String resolutionSteps) { this.resolutionSteps = resolutionSteps; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getExampleStacktrace() { return exampleStacktrace; }
    public void setExampleStacktrace(String exampleStacktrace) { this.exampleStacktrace = exampleStacktrace; }
}
