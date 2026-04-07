package com.gameboost.service;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.util.List;

/**
 * Provides live system stats for the home screen health dashboard.
 * Poll this on a background thread (e.g. every 2 seconds).
 */
public class SystemStatsService {

    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();
    private long[] prevTicks;

    public SystemStatsService() {
        // First tick snapshot — CPU % requires two readings
        prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
    }

    /** Returns CPU usage as 0.0–100.0 */
    public double getCpuUsagePercent() {
        long[] ticks = hal.getProcessor().getSystemCpuLoadTicks();
        double load = hal.getProcessor().getSystemCpuLoadBetweenTicks(prevTicks) * 100.0;
        prevTicks = ticks;
        return Math.min(100.0, Math.max(0.0, load));
    }

    /** Returns used RAM in GB */
    public double getUsedRamGb() {
        GlobalMemory mem = hal.getMemory();
        long used = mem.getTotal() - mem.getAvailable();
        return used / (1024.0 * 1024.0 * 1024.0);
    }

    /** Returns total RAM in GB */
    public double getTotalRamGb() {
        return hal.getMemory().getTotal() / (1024.0 * 1024.0 * 1024.0);
    }

    /** Returns free disk space in GB on the system drive (C:\) */
    public double getFreeDiskGb() {
        java.io.File c = new java.io.File("C:\\");
        return c.getFreeSpace() / (1024.0 * 1024.0 * 1024.0);
    }

    /** Returns total disk space in GB on C:\ */
    public double getTotalDiskGb() {
        java.io.File c = new java.io.File("C:\\");
        return c.getTotalSpace() / (1024.0 * 1024.0 * 1024.0);
    }

    /**
     * Returns CPU temperature in Celsius, or -1 if not available.
     * Note: OSHI temp support varies by hardware/driver.
     */
    public double getCpuTemperature() {
        try {
            Sensors sensors = hal.getSensors();
            double temp = sensors.getCpuTemperature();
            return temp > 0 ? temp : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
