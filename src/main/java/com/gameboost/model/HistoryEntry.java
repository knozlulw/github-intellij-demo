package com.gameboost.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryEntry {

    public String optimizationId;
    public String optimizationTitle;
    public String result;       // "SUCCESS" or "FAILED: <reason>"
    public long timestamp;
    public boolean reverted;

    public HistoryEntry() {}

    public HistoryEntry(String id, String title, String result) {
        this.optimizationId = id;
        this.optimizationTitle = title;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
        this.reverted = false;
    }

    public String getFormattedTime() {
        LocalDateTime dt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"));
    }
}
