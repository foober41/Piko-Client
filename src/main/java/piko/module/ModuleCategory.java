package piko.module;

public enum ModuleCategory {

    HUD("HUD", "On screen information"),
    PVP("PvP", "Combat quality of life"),
    VISUAL("Visual", "Client side visuals"),
    PERFORMANCE("Performance", "Frame rate and memory"),
    GUI("GUI", "Interface and layout");

    private final String displayName;
    private final String description;

    ModuleCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
