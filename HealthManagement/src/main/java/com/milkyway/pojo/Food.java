/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "food")
@NamedQueries({
    @NamedQuery(name = "Food.findAll", query = "SELECT f FROM Food f"),
    @NamedQuery(name = "Food.findByIdFood", query = "SELECT f FROM Food f WHERE f.idFood = :idFood"),
    @NamedQuery(name = "Food.findByFoodName", query = "SELECT f FROM Food f WHERE f.foodName = :foodName"),
    @NamedQuery(name = "Food.findByCalories", query = "SELECT f FROM Food f WHERE f.calories = :calories"),
    @NamedQuery(name = "Food.findByProtein", query = "SELECT f FROM Food f WHERE f.protein = :protein"),
    @NamedQuery(name = "Food.findByCarb", query = "SELECT f FROM Food f WHERE f.carb = :carb"),
    @NamedQuery(name = "Food.findByFat", query = "SELECT f FROM Food f WHERE f.fat = :fat")})
public class Food implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idFood")
    private Integer idFood;
    @Basic(optional = false)
    @Column(name = "foodName")
    private String foodName;
    @Basic(optional = false)
    @Column(name = "calories")
    private double calories;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "protein")
    private Double protein;
    @Column(name = "carb")
    private Double carb;
    @Column(name = "fat")
    private Double fat;
    @Column(name = "sodium")
    private Double sodium;
    @Column(name = "sugar")
    private Double sugar;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "food")
    private Set<MealFood> mealFoodSet;

    public Food() {
    }

    public Food(Integer idFood) {
        this.idFood = idFood;
    }

    public Food(Integer idFood, String foodName, double calories) {
        this.idFood = idFood;
        this.foodName = foodName;
        this.calories = calories;
    }

    public Integer getIdFood() {
        return idFood;
    }

    public void setIdFood(Integer idFood) {
        this.idFood = idFood;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getCarb() {
        return carb;
    }

    public void setCarb(Double carb) {
        this.carb = carb;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getSodium() {
        return sodium;
    }

    public void setSodium(Double sodium) {
        this.sodium = sodium;
    }

    public Double getSugar() {
        return sugar;
    }

    public void setSugar(Double sugar) {
        this.sugar = sugar;
    }

    
    public Set<MealFood> getMealFoodSet() {
        return mealFoodSet;
    }

    public void setMealFoodSet(Set<MealFood> mealFoodSet) {
        this.mealFoodSet = mealFoodSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idFood != null ? idFood.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Food)) {
            return false;
        }
        Food other = (Food) object;
        if ((this.idFood == null && other.idFood != null) || (this.idFood != null && !this.idFood.equals(other.idFood))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.Food[ idFood=" + idFood + " ]";
    }
    
}
