package com.gameboost.model;

public class OptimizationEntry {

    public enum RiskLevel { SAFE, MODERATE, RISKY, RESTART_REQUIRED }
    public enum Category   { MEMORY, CPU, NETWORK, PRIVACY, STORAGE, GPU, SYSTEM }

    private final String id;
    private final String title;
    private final String description;
    private final RiskLevel riskLevel;
    private final String riskTooltip;
    private final boolean revertable;
    private final Category category;

    private boolean enabled;   // toggle state — starts FALSE, smart-select turns on recommended ones
    private boolean applied;

    public OptimizationEntry(String id, String title, String description,
                             RiskLevel riskLevel, String riskTooltip,
                             boolean revertable, Category category) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.riskLevel   = riskLevel;
        this.riskTooltip = riskTooltip;
        this.revertable  = revertable;
        this.category    = category;
        this.enabled     = false;   // <-- DEFAULT OFF. Profile logic enables recommended ones.
        this.applied     = false;
    }

    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public String getRiskTooltip()  { return riskTooltip; }
    public boolean isRevertable()   { return revertable; }
    public Category getCategory()   { return category; }
    public boolean isEnabled()      { return enabled; }
    public boolean isApplied()      { return applied; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setApplied(boolean applied) { this.applied = applied; }

    public String getRiskLabel() {
        return switch (riskLevel) {
            case SAFE             -> "Safe";
            case MODERATE         -> "Moderate";
            case RISKY            -> "Risky";
            case RESTART_REQUIRED -> "Restart";
        };
    }

    public String getCategoryLabel() {
        return switch (category) {
            case MEMORY  -> "MEMORY";
            case CPU     -> "CPU";
            case NETWORK -> "NETWORK";
            case PRIVACY -> "PRIVACY";
            case STORAGE -> "STORAGE";
            case GPU     -> "GPU";
            case SYSTEM  -> "SYSTEM";
        };
    }
}
