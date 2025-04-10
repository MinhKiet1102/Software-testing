/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author votra
 */
public class FoodItem {
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty calo;
    private final SimpleDoubleProperty carb;
    private final SimpleDoubleProperty fat;
    private final SimpleDoubleProperty protein;
    private final SimpleDoubleProperty sodium;
    private final SimpleDoubleProperty sugar;

    public FoodItem(String name, int calo, double carb, double fat, double protein, int sodium, double sugar) {
        this.name = new SimpleStringProperty(name);
        this.calo = new SimpleDoubleProperty(calo);
        this.carb = new SimpleDoubleProperty(carb);
        this.fat = new SimpleDoubleProperty(fat);
        this.protein = new SimpleDoubleProperty(protein);
        this.sodium = new SimpleDoubleProperty(sodium);
        this.sugar = new SimpleDoubleProperty(sugar);
    }

    public String getName() { return name.get(); }
    public double getCalo() { return calo.get(); }
    public double getCarb() { return carb.get(); }
    public double getFat() { return fat.get(); }
    public double getProtein() { return protein.get(); }
    public double getSodium() { return sodium.get(); }
    public double getSugar() { return sugar.get(); }
}


