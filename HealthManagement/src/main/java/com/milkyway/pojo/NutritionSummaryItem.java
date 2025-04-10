package com.milkyway.pojo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class NutritionSummaryItem {
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty current;
    private final SimpleDoubleProperty goal;
    private final SimpleDoubleProperty remaining;

    public NutritionSummaryItem(String type, double current, double goal) {
        this.type = new SimpleStringProperty(type);
        this.current = new SimpleDoubleProperty(current);
        this.goal = new SimpleDoubleProperty(goal);
        this.remaining = new SimpleDoubleProperty(goal - current);
    }

    public String getType() { return type.get(); }
    public double getCurrent() { return current.get(); }
    public double getGoal() { return goal.get(); }
    public double getRemaining() { return remaining.get(); }

    public void setCurrent(double current) {
        this.current.set(current);
        this.remaining.set(goal.get() - current);
    }
}
