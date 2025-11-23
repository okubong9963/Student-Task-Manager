import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Main.java - Ultimate Student Task Manager
 * Complete productivity app with navigation, themes, search, drag & drop, notifications
 */
public class Main extends Application {
    
    // Core data
    private ObservableList<Task> allTasks;
    private ObservableList<Task> filteredTasks;
    
    // Main UI components
    private Stage primaryStage;
    private BorderPane mainContainer;
    private VBox navigationBar;
    private StackPane contentArea;
    
    // Screens
    private VBox dashboardScreen;
    private VBox addTaskScreen;
    private VBox settingsScreen;
    
    // Dashboard components
    private TextField searchField;
    private ComboBox<String> tagFilter;
    private ListView<Task> taskListView;
    private Label totalTasksLabel;
    private Label completedTasksLabel;
    private Label pendingTasksLabel;
    private ProgressBar progressBar;
    
    // Add Task components
    private TextField taskDescField;
    private ComboBox<String> tagSelector;
    private DatePicker dueDatePicker;
    private CheckBox enableDueDateCheckBox;
    
    // Settings components
    private ComboBox<Theme> themeSelector;
    private CheckBox notificationsCheckBox;
    
    // Current state
    private Theme currentTheme;
    private String currentScreen = "dashboard";
    private Timer notificationTimer;
    
    // Date formatters
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("✨ Student Task Manager Pro");
        
        // Load settings
        Settings.load();
        currentTheme = Settings.getTheme();
        
        // Initialize data
        allTasks = FXCollections.observableArrayList();
        filteredTasks = FXCollections.observableArrayList();
        
        // Build UI
        buildUI();
        
        // Create scene
        Scene scene = new Scene(mainContainer, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        
        // Apply theme after scene is set
        applyTheme();
        
        primaryStage.show();
        
        // Auto-load tasks
        autoLoadTasks();
        
        // Start notification checker
        startNotificationTimer();
        
        // Show dashboard
        showScreen("dashboard");
    }
    
    /**
     * Builds the complete UI with navigation
     */
    private void buildUI() {
        mainContainer = new BorderPane();
        mainContainer.getStyleClass().add("main-container");
        
        // Navigation bar (left side)
        navigationBar = buildNavigationBar();
        mainContainer.setLeft(navigationBar);
        
        // Content area (center)
        contentArea = new StackPane();
        mainContainer.setCenter(contentArea);
        
        // Build all screens
        dashboardScreen = buildDashboardScreen();
        addTaskScreen = buildAddTaskScreen();
        settingsScreen = buildSettingsScreen();
    }
    
    /**
     * Builds the navigation bar
     */
    private VBox buildNavigationBar() {
        VBox navBar = new VBox(10);
        navBar.getStyleClass().add("nav-bar");
        navBar.setPadding(new Insets(20));
        navBar.setPrefWidth(200);
        
        Label appTitle = new Label("Task Manager");
        appTitle.getStyleClass().add("nav-title");
        
        Button dashboardBtn = createNavButton("📊 Dashboard", "dashboard");
        Button addTaskBtn = createNavButton("➕ Add Task", "addtask");
        Button settingsBtn = createNavButton("⚙️ Settings", "settings");
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Label versionLabel = new Label("v2.0 Pro");
        versionLabel.getStyleClass().add("version-label");
        
        navBar.getChildren().addAll(
            appTitle,
            new Separator(),
            dashboardBtn,
            addTaskBtn,
            settingsBtn,
            spacer,
            versionLabel
        );
        
        return navBar;
    }
    
    /**
     * Creates a navigation button
     */
    private Button createNavButton(String text, String screenId) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> showScreen(screenId));
        return btn;
    }
    
    /**
     * Shows a specific screen with fade animation
     */
    private void showScreen(String screenId) {
        currentScreen = screenId;
        
        VBox screen = null;
        switch (screenId) {
            case "dashboard":
                screen = dashboardScreen;
                updateDashboard();
                break;
            case "addtask":
                screen = addTaskScreen;
                clearAddTaskForm();
                break;
            case "settings":
                screen = settingsScreen;
                break;
        }
        
        if (screen != null) {
            // Fade animation
            FadeTransition fade = new FadeTransition(Duration.millis(300), screen);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(screen);
            fade.play();
        }
    }
    
    /**
     * Builds the dashboard screen
     */
    private VBox buildDashboardScreen() {
        VBox screen = new VBox(20);
        screen.setPadding(new Insets(25));
        
        // Header
        Label header = new Label("📊 Dashboard");
        header.getStyleClass().add("screen-header");
        
        // Statistics cards
        HBox statsBox = buildStatsSection();
        
        // Search and filter section
        HBox searchBox = buildSearchSection();
        
        // Task list
        VBox taskListSection = buildTaskListSection();
        VBox.setVgrow(taskListSection, Priority.ALWAYS);
        
        // Action buttons
        HBox actionBar = buildDashboardActions();
        
        screen.getChildren().addAll(header, statsBox, searchBox, taskListSection, actionBar);
        
        return screen;
    }
    
    /**
     * Builds statistics section
     */
    private HBox buildStatsSection() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);
        
        VBox totalCard = createStatCard("0", "TOTAL", "stat-total");
        totalTasksLabel = (Label) ((VBox) totalCard.getChildren().get(0)).getChildren().get(0);
        
        VBox completedCard = createStatCard("0", "COMPLETED", "stat-completed");
        completedTasksLabel = (Label) ((VBox) completedCard.getChildren().get(0)).getChildren().get(0);
        
        VBox pendingCard = createStatCard("0", "PENDING", "stat-pending");
        pendingTasksLabel = (Label) ((VBox) pendingCard.getChildren().get(0)).getChildren().get(0);
        
        // Progress bar card
        VBox progressCard = new VBox(10);
        progressCard.getStyleClass().addAll("stat-card", "stat-progress");
        progressCard.setAlignment(Pos.CENTER);
        
        Label progressLabel = new Label("PROGRESS");
        progressLabel.getStyleClass().add("stat-label");
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(150);
        progressBar.getStyleClass().add("progress-bar");
        
        Label progressText = new Label("0%");
        progressText.getStyleClass().add("progress-text");
        
        progressCard.getChildren().addAll(progressLabel, progressBar, progressText);
        
        statsBox.getChildren().addAll(totalCard, completedCard, pendingCard, progressCard);
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(completedCard, Priority.ALWAYS);
        HBox.setHgrow(pendingCard, Priority.ALWAYS);
        HBox.setHgrow(progressCard, Priority.ALWAYS);
        
        return statsBox;
    }
    
    /**
     * Creates a stat card
     */
    private VBox createStatCard(String number, String label, String styleClass) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("stat-card", styleClass);
        
        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        
        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("stat-number");
        
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");
        
        content.getChildren().addAll(numberLabel, textLabel);
        card.getChildren().add(content);
        
        return card;
    }
    
    /**
     * Builds search and filter section
     */
    private HBox buildSearchSection() {
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.getStyleClass().add("search-container");
        
        Label searchIcon = new Label("🔍");
        searchIcon.getStyleClass().add("search-icon");
        
        searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, newVal) -> filterTasks());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Label filterLabel = new Label("Filter:");
        filterLabel.getStyleClass().add("filter-label");
        
        tagFilter = new ComboBox<>();
        tagFilter.getItems().add("All Tags");
        tagFilter.getItems().addAll(Task.ALL_TAGS);
        tagFilter.setValue("All Tags");
        tagFilter.getStyleClass().add("tag-filter");
        tagFilter.setOnAction(e -> filterTasks());
        
        searchBox.getChildren().addAll(searchIcon, searchField, filterLabel, tagFilter);
        
        return searchBox;
    }
    
    /**
     * Builds task list section
     */
    private VBox buildTaskListSection() {
        VBox listContainer = new VBox(10);
        listContainer.getStyleClass().add("task-list-container");
        VBox.setVgrow(listContainer, Priority.ALWAYS);
        
        Label listLabel = new Label("Your Tasks");
        listLabel.getStyleClass().add("section-title");
        
        taskListView = new ListView<>(filteredTasks);
        taskListView.getStyleClass().add("task-list-view");
        taskListView.setCellFactory(lv -> new DraggableTaskCell());
        taskListView.setPlaceholder(new Label("No tasks found. Add your first task! 🎯"));
        VBox.setVgrow(taskListView, Priority.ALWAYS);
        
        listContainer.getChildren().addAll(listLabel, taskListView);
        
        return listContainer;
    }
    
    /**
     * Builds dashboard action buttons
     */
    private HBox buildDashboardActions() {
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.getStyleClass().add("action-bar");
        
        Button saveButton = new Button("💾 Save Tasks");
        saveButton.getStyleClass().add("button-success");
        saveButton.setOnAction(e -> handleSaveTasks());
        
        Button loadButton = new Button("📂 Load Tasks");
        loadButton.getStyleClass().add("button-secondary");
        loadButton.setOnAction(e -> handleLoadTasks());
        
        Button clearCompletedButton = new Button("🗑️ Clear Completed");
        clearCompletedButton.getStyleClass().add("button-danger");
        clearCompletedButton.setOnAction(e -> handleClearCompleted());
        
        actionBar.getChildren().addAll(saveButton, loadButton, clearCompletedButton);
        
        return actionBar;
    }
    
    /**
     * Custom draggable task cell
     */
    private class DraggableTaskCell extends ListCell<Task> {
        private HBox content;
        private VBox taskInfo;
        private Label taskDescription;
        private Label taskDetails;
        private Label tagBadge;
        private Label dueBadge;
        private HBox buttonBox;
        
        public DraggableTaskCell() {
            super();
            
            // Task info
            taskInfo = new VBox(5);
            taskDescription = new Label();
            taskDetails = new Label();
            taskDetails.getStyleClass().add("task-timestamp");
            
            HBox badgeBox = new HBox(5);
            tagBadge = new Label();
            dueBadge = new Label();
            badgeBox.getChildren().addAll(tagBadge, dueBadge);
            
            taskInfo.getChildren().addAll(taskDescription, taskDetails, badgeBox);
            
            // Buttons
            buttonBox = new HBox(5);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button completeButton = new Button();
            completeButton.getStyleClass().addAll("task-button", "task-button-complete");
            
            Button editButton = new Button("✏️");
            editButton.getStyleClass().addAll("task-button", "task-button-edit");
            
            Button deleteButton = new Button("🗑️");
            deleteButton.getStyleClass().addAll("task-button", "task-button-delete");
            
            buttonBox.getChildren().addAll(completeButton, editButton, deleteButton);
            
            // Main content
            content = new HBox(15);
            content.setAlignment(Pos.CENTER_LEFT);
            content.getChildren().addAll(taskInfo, buttonBox);
            HBox.setHgrow(taskInfo, Priority.ALWAYS);
            content.getStyleClass().add("task-item");
            
            // Drag and drop
            setupDragAndDrop();
        }
        
        private void setupDragAndDrop() {
            setOnDragDetected(event -> {
                if (getItem() == null) return;
                
                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem().getId());
                dragboard.setContent(content);
                event.consume();
            });
            
            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            
            setOnDragEntered(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                    setOpacity(0.3);
                }
            });
            
            setOnDragExited(event -> {
                setOpacity(1);
            });
            
            setOnDragDropped(event -> {
                if (getItem() == null) return;
                
                Dragboard db = event.getDragboard();
                boolean success = false;
                
                if (db.hasString()) {
                    String draggedId = db.getString();
                    Task draggedTask = allTasks.stream()
                        .filter(t -> t.getId().equals(draggedId))
                        .findFirst()
                        .orElse(null);
                    
                    if (draggedTask != null) {
                        int draggedIdx = allTasks.indexOf(draggedTask);
                        int thisIdx = allTasks.indexOf(getItem());
                        
                        allTasks.remove(draggedIdx);
                        allTasks.add(thisIdx, draggedTask);
                        
                        // Update display orders
                        for (int i = 0; i < allTasks.size(); i++) {
                            allTasks.get(i).setDisplayOrder(i);
                        }
                        
                        filterTasks();
                        success = true;
                    }
                }
                
                event.setDropCompleted(success);
                event.consume();
            });
        }
        
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);
            
            if (empty || task == null) {
                setGraphic(null);
            } else {
                // Update description
                if (task.isCompleted()) {
                    taskDescription.setText(task.getDescription());
                    taskDescription.getStyleClass().clear();
                    taskDescription.getStyleClass().add("task-content-completed");
                } else {
                    taskDescription.setText(task.getDescription());
                    taskDescription.getStyleClass().clear();
                    taskDescription.getStyleClass().add("task-content");
                }
                
                // Update details
                String details = "Created: " + task.getCreatedAt().format(DISPLAY_FORMATTER);
                if (task.isCompleted() && task.getCompletedAt() != null) {
                    details += " • Completed: " + task.getCompletedAt().format(DISPLAY_FORMATTER);
                }
                taskDetails.setText(details);
                
                // Update tag badge
                if (!task.getTag().equals(Task.TAG_NONE)) {
                    tagBadge.setText("🏷️ " + task.getTag());
                    tagBadge.getStyleClass().clear();
                    tagBadge.getStyleClass().add("tag-badge");
                    tagBadge.setStyle("-fx-background-color: " + task.getTagColor() + "20; -fx-text-fill: " + task.getTagColor() + ";");
                    tagBadge.setVisible(true);
                } else {
                    tagBadge.setVisible(false);
                }
                
                // Update due date badge
                if (task.getDueDate() != null && !task.isCompleted()) {
                    if (task.isOverdue()) {
                        dueBadge.setText("⚠️ OVERDUE");
                        dueBadge.getStyleClass().clear();
                        dueBadge.getStyleClass().add("due-badge-overdue");
                    } else if (task.isDueSoon()) {
                        dueBadge.setText("⏰ Due Soon");
                        dueBadge.getStyleClass().clear();
                        dueBadge.getStyleClass().add("due-badge-soon");
                    } else {
                        dueBadge.setText("📅 Due: " + task.getDueDate().format(DATE_FORMATTER));
                        dueBadge.getStyleClass().clear();
                        dueBadge.getStyleClass().add("due-badge-normal");
                    }
                    dueBadge.setVisible(true);
                } else {
                    dueBadge.setVisible(false);
                }
                
                // Update buttons
                Button completeBtn = (Button) buttonBox.getChildren().get(0);
                Button editBtn = (Button) buttonBox.getChildren().get(1);
                Button deleteBtn = (Button) buttonBox.getChildren().get(2);
                
                completeBtn.setText(task.isCompleted() ? "↩️" : "✓");
                completeBtn.setOnAction(e -> {
                    task.toggleCompleted();
                    taskListView.refresh();
                    updateDashboard();
                });
                
                editBtn.setOnAction(e -> handleEditTask(task));
                deleteBtn.setOnAction(e -> handleDeleteTask(task));
                
                setGraphic(content);
            }
        }
    }
    
    /**
     * Builds the Add Task screen
     */
    private VBox buildAddTaskScreen() {
        VBox screen = new VBox(20);
        screen.setPadding(new Insets(25));
        
        Label header = new Label("➕ Add New Task");
        header.getStyleClass().add("screen-header");
        
        VBox formContainer = new VBox(15);
        formContainer.getStyleClass().add("form-container");
        formContainer.setMaxWidth(600);
        formContainer.setAlignment(Pos.TOP_CENTER);
        
        // Task description
        Label descLabel = new Label("Task Description *");
        descLabel.getStyleClass().add("form-label");
        
        taskDescField = new TextField();
        taskDescField.setPromptText("What do you need to do?");
        taskDescField.getStyleClass().add("form-input");
        
        // Tag selector
        Label tagLabel = new Label("Category / Tag");
        tagLabel.getStyleClass().add("form-label");
        
        tagSelector = new ComboBox<>();
        tagSelector.getItems().addAll(Task.ALL_TAGS);
        tagSelector.setValue(Task.TAG_NONE);
        tagSelector.getStyleClass().add("form-input");
        tagSelector.setMaxWidth(Double.MAX_VALUE);
        
        // Due date
        enableDueDateCheckBox = new CheckBox("Set Due Date (Optional)");
        enableDueDateCheckBox.getStyleClass().add("form-checkbox");
        
        dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Select due date");
        dueDatePicker.getStyleClass().add("form-input");
        dueDatePicker.setMaxWidth(Double.MAX_VALUE);
        dueDatePicker.setDisable(true);
        
        enableDueDateCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
            dueDatePicker.setDisable(!newVal);
        });
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addButton = new Button("✅ Add Task");
        addButton.getStyleClass().add("button-primary");
        addButton.setOnAction(e -> handleAddTaskFromForm());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-secondary");
        cancelButton.setOnAction(e -> showScreen("dashboard"));
        
        buttonBox.getChildren().addAll(addButton, cancelButton);
        
        formContainer.getChildren().addAll(
            descLabel, taskDescField,
            tagLabel, tagSelector,
            enableDueDateCheckBox, dueDatePicker,
            buttonBox
        );
        
        screen.getChildren().addAll(header, formContainer);
        screen.setAlignment(Pos.TOP_CENTER);
        
        return screen;
    }
    
    /**
     * Builds the Settings screen
     */
    private VBox buildSettingsScreen() {
        VBox screen = new VBox(20);
        screen.setPadding(new Insets(25));
        
        Label header = new Label("⚙️ Settings");
        header.getStyleClass().add("screen-header");
        
        VBox settingsContainer = new VBox(20);
        settingsContainer.getStyleClass().add("settings-container");
        settingsContainer.setMaxWidth(600);
        
        // Theme section
        VBox themeSection = new VBox(10);
        themeSection.getStyleClass().add("settings-section");
        
        Label themeLabel = new Label("🎨 Theme");
        themeLabel.getStyleClass().add("settings-section-title");
        
        themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(Theme.ALL_THEMES);
        themeSelector.setValue(currentTheme);
        themeSelector.getStyleClass().add("form-input");
        themeSelector.setMaxWidth(Double.MAX_VALUE);
        themeSelector.setOnAction(e -> handleThemeChange());
        
        themeSection.getChildren().addAll(themeLabel, themeSelector);
        
        // Notifications section
        VBox notifSection = new VBox(10);
        notifSection.getStyleClass().add("settings-section");
        
        Label notifLabel = new Label("🔔 Notifications");
        notifLabel.getStyleClass().add("settings-section-title");
        
        notificationsCheckBox = new CheckBox("Enable desktop notifications for overdue tasks");
        notificationsCheckBox.setSelected(Settings.areNotificationsEnabled());
        notificationsCheckBox.getStyleClass().add("form-checkbox");
        notificationsCheckBox.setOnAction(e -> {
            Settings.setNotificationsEnabled(notificationsCheckBox.isSelected());
        });
        
        notifSection.getChildren().addAll(notifLabel, notificationsCheckBox);
        
        // About section
        VBox aboutSection = new VBox(10);
        aboutSection.getStyleClass().add("settings-section");
        
        Label aboutLabel = new Label("ℹ️ About");
        aboutLabel.getStyleClass().add("settings-section-title");
        
        Label aboutText = new Label(
            "Student Task Manager Pro v2.0\n" +
            "A beautiful, feature-rich task management application\n" +
            "Built with JavaFX"
        );
        aboutText.getStyleClass().add("about-text");
        
        aboutSection.getChildren().addAll(aboutLabel, aboutText);
        
        settingsContainer.getChildren().addAll(themeSection, notifSection, aboutSection);
        
        screen.getChildren().addAll(header, settingsContainer);
        screen.setAlignment(Pos.TOP_CENTER);
        
        return screen;
    }
    
    /**
     * Filters tasks based on search and tag filter
     */
    private void filterTasks() {
        String searchText = searchField.getText().toLowerCase();
        String selectedTag = tagFilter.getValue();
        
        List<Task> filtered = allTasks.stream()
            .filter(task -> {
                // Search filter
                boolean matchesSearch = searchText.isEmpty() || 
                    task.getDescription().toLowerCase().contains(searchText);
                
                // Tag filter
                boolean matchesTag = selectedTag.equals("All Tags") || 
                    task.getTag().equals(selectedTag);
                
                return matchesSearch && matchesTag;
            })
            .collect(Collectors.toList());
        
        filteredTasks.clear();
        filteredTasks.addAll(filtered);
    }
    
    /**
     * Updates dashboard statistics
     */
    private void updateDashboard() {
        int total = allTasks.size();
        long completed = allTasks.stream().filter(Task::isCompleted).count();
        long pending = total - completed;
        
        totalTasksLabel.setText(String.valueOf(total));
        completedTasksLabel.setText(String.valueOf(completed));
        pendingTasksLabel.setText(String.valueOf(pending));
        
        // Update progress bar
        double progress = total > 0 ? (double) completed / total : 0;
        progressBar.setProgress(progress);
        
        // Update progress text
        Label progressText = (Label) ((VBox) progressBar.getParent()).getChildren().get(2);
        progressText.setText(String.format("%.0f%%", progress * 100));
        
        filterTasks();
    }
    
    /**
     * Handles adding task from form
     */
    private void handleAddTaskFromForm() {
        String description = taskDescField.getText().trim();
        
        if (description.isEmpty()) {
            showErrorAlert("Invalid Input", "Task description cannot be empty!", 
                          "Please enter a valid task description.");
            return;
        }
        
        Task newTask = new Task(description);
        newTask.setTag(tagSelector.getValue());
        
        if (enableDueDateCheckBox.isSelected() && dueDatePicker.getValue() != null) {
            LocalDateTime dueDateTime = dueDatePicker.getValue().atTime(23, 59);
            newTask.setDueDate(dueDateTime);
        }
        
        newTask.setDisplayOrder(allTasks.size());
        allTasks.add(newTask);
        
        showInfoAlert("Success", "Task Added!", "Your task has been added successfully.");
        showScreen("dashboard");
    }
    
    /**
     * Clears the add task form
     */
    private void clearAddTaskForm() {
        taskDescField.clear();
        tagSelector.setValue(Task.TAG_NONE);
        enableDueDateCheckBox.setSelected(false);
        dueDatePicker.setValue(null);
        dueDatePicker.setDisable(true);
    }
    
    /**
     * Handles editing a task
     */
    private void handleEditTask(Task task) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit your task");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        TextField descField = new TextField(task.getDescription());
        descField.setPromptText("Task description");
        
        ComboBox<String> tagCombo = new ComboBox<>();
        tagCombo.getItems().addAll(Task.ALL_TAGS);
        tagCombo.setValue(task.getTag());
        
        CheckBox dueDateCheck = new CheckBox("Set Due Date");
        dueDateCheck.setSelected(task.getDueDate() != null);
        
        DatePicker datePicker = new DatePicker();
        if (task.getDueDate() != null) {
            datePicker.setValue(task.getDueDate().toLocalDate());
        }
        datePicker.setDisable(task.getDueDate() == null);
        
        dueDateCheck.selectedProperty().addListener((obs, old, newVal) -> {
            datePicker.setDisable(!newVal);
        });
        
        content.getChildren().addAll(
            new Label("Description:"), descField,
            new Label("Tag:"), tagCombo,
            dueDateCheck, datePicker
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                task.setDescription(descField.getText().trim());
                task.setTag(tagCombo.getValue());
                
                if (dueDateCheck.isSelected() && datePicker.getValue() != null) {
                    task.setDueDate(datePicker.getValue().atTime(23, 59));
                } else {
                    task.setDueDate(null);
                }
                
                return task;
            }
            return null;
        });
        
        Optional<Task> result = dialog.showAndWait();
        if (result.isPresent()) {
            taskListView.refresh();
            updateDashboard();
        }
    }
    
    /**
     * Handles deleting a task
     */
    private void handleDeleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText(task.getDescription());
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allTasks.remove(task);
            updateDashboard();
        }
    }
    
    /**
     * Handles clearing completed tasks
     */
    private void handleClearCompleted() {
        long completedCount = allTasks.stream().filter(Task::isCompleted).count();
        
        if (completedCount == 0) {
            showInfoAlert("No Completed Tasks", "There are no completed tasks to clear.", 
                         "Mark some tasks as complete first.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Completed Tasks");
        alert.setHeaderText("Delete all completed tasks?");
        alert.setContentText("This will remove " + completedCount + " completed task(s).");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            allTasks.removeIf(Task::isCompleted);
            updateDashboard();
        }
    }
    
    /**
     * Handles saving tasks
     */
    private void handleSaveTasks() {
        if (allTasks.isEmpty()) {
            showWarningAlert("No Tasks", "There are no tasks to save.", 
                           "Add some tasks before saving.");
            return;
        }
        
        try {
            FileHelper.saveTasks(allTasks);
            showInfoAlert("Success", "Tasks saved successfully!", 
                         allTasks.size() + " task(s) saved to tasks.txt");
            
        } catch (IOException e) {
            showErrorAlert("Save Error", "Failed to save tasks!", 
                          "Error: " + e.getMessage());
        }
    }
    
    /**
     * Handles loading tasks
     */
    private void handleLoadTasks() {
        if (!allTasks.isEmpty()) {
            Optional<ButtonType> result = showConfirmationAlert(
                "Load Tasks", 
                "This will replace all current tasks.",
                "Are you sure you want to load tasks from file?"
            );
            
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        try {
            List<Task> loadedTasks = FileHelper.loadTasks();
            
            if (loadedTasks.isEmpty()) {
                if (!FileHelper.fileExists()) {
                    showWarningAlert("File Not Found", "No saved tasks found.", 
                                   "The file tasks.txt does not exist yet.");
                } else {
                    showInfoAlert("Empty File", "No tasks in file.", 
                                "The tasks.txt file is empty.");
                }
                allTasks.clear();
                updateDashboard();
                return;
            }
            
            allTasks.clear();
            allTasks.addAll(loadedTasks);
            updateDashboard();
            
            showInfoAlert("Success", "Tasks loaded successfully!", 
                         loadedTasks.size() + " task(s) loaded from tasks.txt");
            
        } catch (IOException e) {
            showErrorAlert("Load Error", "Failed to load tasks!", 
                          "Error: " + e.getMessage());
        }
    }
    
    /**
     * Auto-loads tasks on startup
     */
    private void autoLoadTasks() {
        try {
            List<Task> loadedTasks = FileHelper.loadTasks();
            
            if (!loadedTasks.isEmpty()) {
                allTasks.addAll(loadedTasks);
                updateDashboard();
            }
            
        } catch (IOException e) {
            System.err.println("Auto-load warning: " + e.getMessage());
        }
    }
    
    /**
     * Handles theme change
     */
    private void handleThemeChange() {
        currentTheme = themeSelector.getValue();
        Settings.setTheme(currentTheme);
        applyTheme();
    }
    
    /**
     * Applies the current theme dynamically by creating a temporary CSS file
     */
    private void applyTheme() {
        // Update main container gradient
        String gradientStyle = String.format(
            "-fx-background-color: linear-gradient(to bottom right, %s, %s);",
            currentTheme.getGradientStart(),
            currentTheme.getGradientEnd()
        );
        mainContainer.setStyle(gradientStyle);
        
        // Create dynamic CSS content
        String dynamicCSS = String.format(
            ".button-primary { " +
            "  -fx-background-color: linear-gradient(to bottom right, %s, %s) !important; " +
            "  -fx-effect: dropshadow(gaussian, derive(%s, -20%%), 10, 0, 0, 4); " +
            "} " +
            ".button-primary:hover { " +
            "  -fx-background-color: linear-gradient(to bottom right, %s, %s) !important; " +
            "  -fx-effect: dropshadow(gaussian, derive(%s, -20%%), 15, 0, 0, 6); " +
            "} " +
            ".button-secondary { " +
            "  -fx-text-fill: %s !important; " +
            "  -fx-border-color: %s !important; " +
            "} " +
            ".button-secondary:hover { " +
            "  -fx-background-color: %s !important; " +
            "} " +
            ".stat-total .stat-number { -fx-text-fill: %s !important; } " +
            ".search-field:focused { " +
            "  -fx-border-color: %s !important; " +
            "} " +
            ".task-list-view .list-cell:hover { " +
            "  -fx-border-color: %s !important; " +
            "} " +
            ".task-list-view .list-cell:selected { " +
            "  -fx-border-color: %s !important; " +
            "} " +
            ".task-button { -fx-text-fill: %s !important; } " +
            ".form-input:focused { " +
            "  -fx-border-color: %s !important; " +
            "} " +
            ".dialog-pane .header-panel { " +
            "  -fx-background-color: linear-gradient(to bottom right, %s, %s) !important; " +
            "}",
            currentTheme.getPrimaryColor(), currentTheme.getSecondaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getSecondaryColor(), darkenColor(currentTheme.getSecondaryColor()),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(),
            currentTheme.getPrimaryColor(), currentTheme.getSecondaryColor()
        );
        
        // Write to temporary CSS file
        try {
            java.io.FileWriter writer = new java.io.FileWriter("theme-override.css");
            writer.write(dynamicCSS);
            writer.close();
            
            // Remove old theme stylesheet if exists
            primaryStage.getScene().getStylesheets().removeIf(s -> s.contains("theme-override.css"));
            
            // Add new theme stylesheet
            primaryStage.getScene().getStylesheets().add(new java.io.File("theme-override.css").toURI().toString());
            
        } catch (Exception e) {
            System.err.println("Error applying theme: " + e.getMessage());
        }
    }
    
    /**
     * Darkens a color for hover effects
     */
    private String darkenColor(String color) {
        if (color.startsWith("#") && color.length() == 7) {
            try {
                int r = Integer.parseInt(color.substring(1, 3), 16);
                int g = Integer.parseInt(color.substring(3, 5), 16);
                int b = Integer.parseInt(color.substring(5, 7), 16);
                
                r = Math.max(0, r - 30);
                g = Math.max(0, g - 30);
                b = Math.max(0, b - 30);
                
                return String.format("#%02x%02x%02x", r, g, b);
            } catch (Exception e) {
                return color;
            }
        }
        return color;
    }
    
    /**
     * Starts notification timer to check for overdue tasks
     */
    private void startNotificationTimer() {
        notificationTimer = new Timer(true);
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Settings.areNotificationsEnabled()) {
                    checkForOverdueTasks();
                }
            }
        }, 60000, 300000); // Check every 5 minutes
    }
    
    /**
     * Checks for overdue tasks and shows notifications
     */
    private void checkForOverdueTasks() {
        List<Task> overdueTasks = allTasks.stream()
            .filter(task -> !task.isCompleted() && task.isOverdue())
            .collect(Collectors.toList());
        
        if (!overdueTasks.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Overdue Tasks");
                alert.setHeaderText("You have " + overdueTasks.size() + " overdue task(s)!");
                
                StringBuilder content = new StringBuilder();
                for (Task task : overdueTasks) {
                    content.append("• ").append(task.getDescription()).append("\n");
                }
                alert.setContentText(content.toString());
                alert.show();
            });
        }
    }
    
    /**
     * Alert helper methods
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showWarningAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private Optional<ButtonType> showConfirmationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
    
    @Override
    public void stop() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

