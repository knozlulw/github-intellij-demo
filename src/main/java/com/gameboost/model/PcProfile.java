package com.gameboost.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Persisted to ~/.gameboost/pc-profile.json on first scan.
 * Loaded on subsequent launches — no re-scan needed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcProfile {

    public String cpuModel;
    public int cpuCores;
    public long ramTotalGb;
    public String gpuModel;
    public String storageType;   // "SSD" or "HDD"
    public String osVersion;
    public String osName;
    public long scanTimestamp;

    // Smart recommendations derived from specs
    public boolean lowRam;           // < 8 GB
    public boolean hasNvidiaGpu;
    public boolean hasAmdGpu;
    public boolean hasHdd;

    public PcProfile() {}

    /** Human-readable profile summary shown in the UI "PC Profile" card. */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (lowRam) sb.append("⚠ Low RAM detected — memory optimizations recommended\n");
        if (hasHdd) sb.append("⚠ HDD detected — temp cleanup especially beneficial\n");
        if (hasNvidiaGpu) sb.append("✓ NVIDIA GPU — GPU performance mode available\n");
        if (hasAmdGpu)    sb.append("✓ AMD GPU — GPU performance mode available\n");
        if (!hasNvidiaGpu && !hasAmdGpu) sb.append("ℹ Integrated/unknown GPU\n");
        return sb.toString().trim();
    }
}
