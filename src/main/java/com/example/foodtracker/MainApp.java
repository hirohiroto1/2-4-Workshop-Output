package com.example.foodtracker;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.example.foodtracker.utils.DatabaseConnection;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        // ==========================================
        // SCENE 1: LOGIN WINDOW
        // ==========================================
        VBox loginRoot = new VBox(20);
        loginRoot.setPadding(new Insets(40));
        loginRoot.setAlignment(Pos.CENTER);
        loginRoot.setStyle("-fx-background-color: #f4f4f4;");

        VBox loginCard = new VBox(15);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(35));
        loginCard.setMaxWidth(380);
        loginCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12,0,0,4);"
        );

        Label title = new Label("Benedict's Food Tracker");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #222;");

        Label subtitle = new Label("Log in to track your daily meals");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setPrefHeight(42);
        username.setMaxWidth(300);

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setPrefHeight(42);
        password.setMaxWidth(300);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(42);
        loginButton.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8;");

        loginCard.getChildren().addAll(title, subtitle, username, password, loginButton);
        loginRoot.getChildren().add(loginCard);

        Scene loginScene = new Scene(loginRoot, 500, 500);

        // ==========================================
        // SCENE 2: DASHBOARD WINDOW
        // ==========================================
        loginButton.setOnAction(e -> {
            HBox dashboardRoot = new HBox(25);
            dashboardRoot.setPadding(new Insets(25));
            dashboardRoot.setStyle("-fx-background-color: #f4f4f4;");

            // --- Left Panel: Input Form ---
            VBox leftPanel = new VBox(15);
            leftPanel.setPrefWidth(300);

            Label dashboardTitle = new Label("Daily Meals");
            dashboardTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #222;");

            TextField mealNameField = new TextField();
            mealNameField.setPromptText("e.g., Grilled Chicken Salad");
            mealNameField.setPrefHeight(40);

            ComboBox<String> mealTypeBox = new ComboBox<>();
            mealTypeBox.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
            mealTypeBox.setPromptText("Select Meal Type");
            mealTypeBox.setPrefHeight(40);
            mealTypeBox.setPrefWidth(300);

            TextField caloriesField = new TextField();
            caloriesField.setPromptText("Estimated Calories");
            caloriesField.setPrefHeight(40);

            DatePicker datePicker = new DatePicker();
            datePicker.setPrefHeight(40);
            datePicker.setPrefWidth(300);

            Button addButton = new Button("Add Meal");
            addButton.setPrefWidth(300);
            addButton.setPrefHeight(42);
            addButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

            Button deleteButton = new Button("Delete Selected Meal");
            deleteButton.setPrefWidth(300);
            deleteButton.setPrefHeight(42);
            deleteButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

            leftPanel.getChildren().addAll(
                    dashboardTitle,
                    new Label("Meal Name"), mealNameField,
                    new Label("Meal Type"), mealTypeBox,
                    new Label("Calories"), caloriesField,
                    new Label("Date Consumed"), datePicker,
                    addButton, deleteButton
            );

            // --- Right Panel: Data Table ---
            VBox rightPanel = new VBox(10);
            TableView<String[]> table = new TableView<>();

            TableColumn<String[], String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

            TableColumn<String[], String> nameCol = new TableColumn<>("Meal Name");
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

            TableColumn<String[], String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

            TableColumn<String[], String> calCol = new TableColumn<>("Calories");
            calCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
            
            TableColumn<String[], String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

            table.getColumns().addAll(idCol, nameCol, typeCol, calCol, dateCol);
            
            idCol.setPrefWidth(50);
            nameCol.setPrefWidth(200);
            typeCol.setPrefWidth(120);
            calCol.setPrefWidth(100);
            dateCol.setPrefWidth(120);
            
            table.setPrefWidth(600);
            table.setPrefHeight(520);

            rightPanel.getChildren().addAll(new Label("Recorded History"), table);
            dashboardRoot.getChildren().addAll(leftPanel, rightPanel);

            ObservableList<String[]> data = FXCollections.observableArrayList();

            // Load Data from Database
            Runnable loadTable = () -> {
                try {
                    data.clear();
                    Connection conn = DatabaseConnection.connect();
                    String sql = "SELECT * FROM meals ORDER BY date_consumed DESC, id DESC";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        data.add(new String[]{
                                rs.getString("id"),
                                rs.getString("meal_name"),
                                rs.getString("meal_type"),
                                rs.getString("calories"),
                                rs.getString("date_consumed")
                        });
                    }
                    table.setItems(data);
                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            };

            loadTable.run(); // Initial Load

            // Add Record Action
            addButton.setOnAction(event -> {
                try {
                    Connection conn = DatabaseConnection.connect();
                    String sql = "INSERT INTO meals(meal_name, meal_type, calories, date_consumed) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    
                    pstmt.setString(1, mealNameField.getText());
                    pstmt.setString(2, mealTypeBox.getValue());
                    pstmt.setInt(3, Integer.parseInt(caloriesField.getText())); // Ensures number format
                    pstmt.setDate(4, java.sql.Date.valueOf(datePicker.getValue())); 
                    
                    pstmt.executeUpdate();
                    conn.close();

                    // Clear Fields
                    mealNameField.clear();
                    caloriesField.clear();
                    mealTypeBox.setValue(null);
                    datePicker.setValue(null);

                    loadTable.run(); // Refresh Table
                } catch (Exception ex) {
                    System.out.println("Error adding task. Check number formatting and dates.");
                    ex.printStackTrace();
                }
            });

            // Delete Record Action
            deleteButton.setOnAction(event -> {
                try {
                    String[] selected = table.getSelectionModel().getSelectedItem();
                    if (selected == null) return;

                    Connection conn = DatabaseConnection.connect();
                    String sql = "DELETE FROM meals WHERE id=?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(selected[0]));
                    pstmt.executeUpdate();
                    conn.close();

                    loadTable.run(); // Refresh Table
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Scene dashboardScene = new Scene(dashboardRoot, 1000, 600);
            stage.setScene(dashboardScene);
        });

        stage.setTitle("Food Tracker Application");
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}