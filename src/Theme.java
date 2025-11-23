/**
 * Theme.java
 * Manages application themes with different color schemes
 */
public class Theme {
    private String name;
    private String primaryColor;
    private String secondaryColor;
    private String accentColor;
    private String gradientStart;
    private String gradientEnd;
    
    public Theme(String name, String primaryColor, String secondaryColor, 
                 String accentColor, String gradientStart, String gradientEnd) {
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.accentColor = accentColor;
        this.gradientStart = gradientStart;
        this.gradientEnd = gradientEnd;
    }
    
    // Getters
    public String getName() { return name; }
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public String getAccentColor() { return accentColor; }
    public String getGradientStart() { return gradientStart; }
    public String getGradientEnd() { return gradientEnd; }
    
    // Predefined themes
    public static final Theme OCEAN = new Theme(
        "Ocean Blue",
        "#00b4db",
        "#0083b0",
        "#00d4ff",
        "#0f2027",
        "#2c5364"
    );
    
    public static final Theme SUNSET = new Theme(
        "Sunset Orange",
        "#ff6b6b",
        "#ee5a6f",
        "#ff8787",
        "#ff6b6b",
        "#c44569"
    );
    
    public static final Theme FOREST = new Theme(
        "Forest Green",
        "#11998e",
        "#38ef7d",
        "#06d6a0",
        "#134e4a",
        "#14532d"
    );
    
    public static final Theme NIGHT = new Theme(
        "Midnight Purple",
        "#667eea",
        "#764ba2",
        "#8b5cf6",
        "#1e1b4b",
        "#312e81"
    );
    
    public static final Theme ROSE = new Theme(
        "Rose Pink",
        "#ec4899",
        "#be185d",
        "#f472b6",
        "#831843",
        "#500724"
    );
    
    public static final Theme CYBER = new Theme(
        "Cyber Teal",
        "#06b6d4",
        "#0891b2",
        "#22d3ee",
        "#164e63",
        "#083344"
    );
    
    public static final Theme[] ALL_THEMES = {
        OCEAN, SUNSET, FOREST, NIGHT, ROSE, CYBER
    };
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Gets theme by name
     */
    public static Theme getThemeByName(String name) {
        for (Theme theme : ALL_THEMES) {
            if (theme.getName().equals(name)) {
                return theme;
            }
        }
        return OCEAN; // Default
    }
}

