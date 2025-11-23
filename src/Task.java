import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Task.java - Enhanced
 * Complete task class with tags, due dates, and full management features
 */
public class Task {
    private String id;
    private String description;
    private LocalDateTime createdAt;
    private boolean completed;
    private LocalDateTime completedAt;
    private String tag;
    private LocalDateTime dueDate;
    private int displayOrder; // For drag & drop ordering
    
    // Formatter for displaying timestamps
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Available tags
    public static final String TAG_NONE = "None";
    public static final String TAG_SCHOOL = "School";
    public static final String TAG_PERSONAL = "Personal";
    public static final String TAG_WORK = "Work";
    public static final String TAG_URGENT = "Urgent";
    public static final String TAG_HEALTH = "Health";
    public static final String TAG_SHOPPING = "Shopping";
    public static final String TAG_OTHER = "Other";
    
    public static final String[] ALL_TAGS = {
        TAG_NONE, TAG_SCHOOL, TAG_PERSONAL, TAG_WORK, 
        TAG_URGENT, TAG_HEALTH, TAG_SHOPPING, TAG_OTHER
    };
    
    /**
     * Constructor - creates a new task with current timestamp
     * @param description The task description
     */
    public Task(String description) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.completed = false;
        this.completedAt = null;
        this.tag = TAG_NONE;
        this.dueDate = null;
        this.displayOrder = 0;
    }
    
    /**
     * Full constructor - used when loading from file
     */
    public Task(String id, String description, LocalDateTime createdAt, boolean completed, 
                LocalDateTime completedAt, String tag, LocalDateTime dueDate, int displayOrder) {
        this.id = id;
        this.description = description;
        this.createdAt = createdAt;
        this.completed = completed;
        this.completedAt = completedAt;
        this.tag = tag != null ? tag : TAG_NONE;
        this.dueDate = dueDate;
        this.displayOrder = displayOrder;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public int getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    /**
     * Checks if task is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null || completed) {
            return false;
        }
        return LocalDateTime.now().isAfter(dueDate);
    }
    
    /**
     * Checks if due date is approaching (within 24 hours)
     */
    public boolean isDueSoon() {
        if (dueDate == null || completed) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        return now.isBefore(dueDate) && tomorrow.isAfter(dueDate);
    }
    
    /**
     * Toggles the completion status
     */
    public void toggleCompleted() {
        setCompleted(!completed);
    }
    
    /**
     * Returns the task description
     */
    @Override
    public String toString() {
        return description;
    }
    
    /**
     * Returns formatted string for file storage
     * Format: id|description|createdAt|completed|completedAt|tag|dueDate|displayOrder
     */
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|");
        sb.append(description).append("|");
        sb.append(createdAt.format(FORMATTER)).append("|");
        sb.append(completed).append("|");
        sb.append(completedAt != null ? completedAt.format(FORMATTER) : "null").append("|");
        sb.append(tag).append("|");
        sb.append(dueDate != null ? dueDate.format(FORMATTER) : "null").append("|");
        sb.append(displayOrder);
        return sb.toString();
    }
    
    /**
     * Creates a Task from a file string with backward compatibility
     */
    public static Task fromFileString(String fileString) {
        try {
            String[] parts = fileString.split("\\|");
            
            // New format with tags and due dates: id|description|createdAt|completed|completedAt|tag|dueDate|displayOrder
            if (parts.length >= 8) {
                String id = parts[0];
                String desc = parts[1];
                LocalDateTime createdAt = LocalDateTime.parse(parts[2], FORMATTER);
                boolean completed = Boolean.parseBoolean(parts[3]);
                LocalDateTime completedAt = parts[4].equals("null") ? null : LocalDateTime.parse(parts[4], FORMATTER);
                String tag = parts[5];
                LocalDateTime dueDate = parts[6].equals("null") ? null : LocalDateTime.parse(parts[6], FORMATTER);
                int displayOrder = Integer.parseInt(parts[7]);
                return new Task(id, desc, createdAt, completed, completedAt, tag, dueDate, displayOrder);
            }
            // Old format: id|description|createdAt|completed|completedAt
            else if (parts.length >= 5) {
                String id = parts[0];
                String desc = parts[1];
                LocalDateTime createdAt = LocalDateTime.parse(parts[2], FORMATTER);
                boolean completed = Boolean.parseBoolean(parts[3]);
                LocalDateTime completedAt = parts[4].equals("null") ? null : LocalDateTime.parse(parts[4], FORMATTER);
                return new Task(id, desc, createdAt, completed, completedAt, TAG_NONE, null, 0);
            }
            // Very old format: description|timestamp
            else if (parts.length >= 2) {
                String desc = parts[0];
                LocalDateTime timestamp = LocalDateTime.parse(parts[1], FORMATTER);
                Task task = new Task(desc);
                task.createdAt = timestamp;
                return task;
            }
            // Ancient format: just description
            else if (parts.length == 1) {
                return new Task(parts[0]);
            }
        } catch (Exception e) {
            System.err.println("Error parsing task: " + e.getMessage());
            return null;
        }
        return null;
    }
    
    /**
     * Gets the color for this task's tag
     */
    public String getTagColor() {
        switch (tag) {
            case TAG_SCHOOL: return "#3b82f6"; // Blue
            case TAG_PERSONAL: return "#8b5cf6"; // Purple
            case TAG_WORK: return "#f59e0b"; // Orange
            case TAG_URGENT: return "#ef4444"; // Red
            case TAG_HEALTH: return "#10b981"; // Green
            case TAG_SHOPPING: return "#ec4899"; // Pink
            case TAG_OTHER: return "#6b7280"; // Gray
            default: return "#9ca3af"; // Light gray
        }
    }
}
