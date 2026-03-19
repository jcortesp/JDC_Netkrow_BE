package com.netkrow.backend.dto;

public class DropRequest {
    private boolean cobrarRevision;
    private Double revisionValue;

    public boolean isCobrarRevision() { return cobrarRevision; }
    public void setCobrarRevision(boolean cobrarRevision) { this.cobrarRevision = cobrarRevision; }

    public Double getRevisionValue() { return revisionValue; }
    public void setRevisionValue(Double revisionValue) { this.revisionValue = revisionValue; }
}
