import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHelper.java
 * Handles all file operations for saving and loading tasks.
 * Uses BufferedReader and BufferedWriter as required.
 * Implements proper error handling to prevent crashes.
 */
public class FileHelper {
    private static final String FILE_NAME = "tasks.txt";
    
    /**
     * Saves a list of tasks to the file
     * Each task is stored on a new line
     * @param tasks List of tasks to save
     * @throws IOException if file operations fail
     */
    public static void saveTasks(List<Task> tasks) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_NAME));
            
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
            
            writer.flush();
        } finally {
            // Ensure writer is closed even if exception occurs
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // Log but don't throw - already closing
                    System.err.println("Error closing writer: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Loads tasks from the file
     * Handles missing file, empty file, and corrupted lines gracefully
     * @return List of tasks (empty list if file doesn't exist or is empty)
     * @throws IOException if file operations fail (except FileNotFoundException)
     */
    public static List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        File file = new File(FILE_NAME);
        
        // Handle file not found - return empty list (not an error)
        if (!file.exists()) {
            return tasks;
        }
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                // Try to parse the task
                Task task = Task.fromFileString(line);
                if (task != null) {
                    tasks.add(task);
                } else {
                    // Log corrupted line but continue loading other tasks
                    System.err.println("Warning: Corrupted line " + lineNumber + " in " + FILE_NAME + ": " + line);
                }
            }
        } finally {
            // Ensure reader is closed
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
            }
        }
        
        return tasks;
    }
    
    /**
     * Checks if the tasks file exists
     * @return true if file exists, false otherwise
     */
    public static boolean fileExists() {
        return new File(FILE_NAME).exists();
    }
}

