import java.io.*;
import java.util.Properties;

/**
 * Settings.java
 * Manages application settings and preferences
 */
public class Settings {
    private static final String SETTINGS_FILE = "settings.properties";
    private static Properties properties = new Properties();
    
    // Setting keys
    private static final String KEY_THEME = "theme";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    
    /**
     * Loads settings from file
     */
    public static void load() {
        try {
            File file = new File(SETTINGS_FILE);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                properties.load(fis);
                fis.close();
            } else {
                // Set defaults
                setDefaultSettings();
            }
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            setDefaultSettings();
        }
    }
    
    /**
     * Saves settings to file
     */
    public static void save() {
        try {
            FileOutputStream fos = new FileOutputStream(SETTINGS_FILE);
            properties.store(fos, "Student Task Manager Settings");
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    
    /**
     * Sets default settings
     */
    private static void setDefaultSettings() {
        properties.setProperty(KEY_THEME, Theme.OCEAN.getName());
        properties.setProperty(KEY_NOTIFICATIONS, "true");
    }
    
    // Theme settings
    public static Theme getTheme() {
        String themeName = properties.getProperty(KEY_THEME, Theme.OCEAN.getName());
        return Theme.getThemeByName(themeName);
    }
    
    public static void setTheme(Theme theme) {
        properties.setProperty(KEY_THEME, theme.getName());
        save();
    }
    
    // Notification settings
    public static boolean areNotificationsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_NOTIFICATIONS, "true"));
    }
    
    public static void setNotificationsEnabled(boolean enabled) {
        properties.setProperty(KEY_NOTIFICATIONS, String.valueOf(enabled));
        save();
    }
}

