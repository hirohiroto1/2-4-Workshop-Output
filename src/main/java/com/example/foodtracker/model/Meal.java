package com.example.foodtracker.model;

public class Meal {
    private int id;
    private String mealName;
    private String mealType;
    private int calories;
    private String dateConsumed;

    public Meal(int id, String mealName, String mealType, int calories, String dateConsumed) {
        this.id = id;
        this.mealName = mealName;
        this.mealType = mealType;
        this.calories = calories;
        this.dateConsumed = dateConsumed;
    }

    public int getId() { return id; }
    public String getMealName() { return mealName; }
    public String getMealType() { return mealType; }
    public int getCalories() { return calories; }
    public String getDateConsumed() { return dateConsumed; }
}