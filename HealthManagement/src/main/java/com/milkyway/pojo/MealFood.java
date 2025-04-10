/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "meal_food")
@NamedQueries({
    @NamedQuery(name = "MealFood.findAll", query = "SELECT m FROM MealFood m"),
    @NamedQuery(name = "MealFood.findByMealId", query = "SELECT m FROM MealFood m WHERE m.mealFoodPK.mealId = :mealId"),
    @NamedQuery(name = "MealFood.findByFoodId", query = "SELECT m FROM MealFood m WHERE m.mealFoodPK.foodId = :foodId"),
    @NamedQuery(name = "MealFood.findByUnit", query = "SELECT m FROM MealFood m WHERE m.unit = :unit"),
    @NamedQuery(name = "MealFood.findByQuantity", query = "SELECT m FROM MealFood m WHERE m.quantity = :quantity")})
public class MealFood implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected MealFoodPK mealFoodPK;
    @Basic(optional = false)
    @Column(name = "unit")
    private String unit;
    @Basic(optional = false)
    @Column(name = "quantity")
    private int quantity;
    @JoinColumn(name = "foodId", referencedColumnName = "idFood", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Food food;
    @JoinColumn(name = "mealId", referencedColumnName = "idMeal", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Meal meal;

    public MealFood() {
    }

    public MealFood(MealFoodPK mealFoodPK) {
        this.mealFoodPK = mealFoodPK;
    }

    public MealFood(MealFoodPK mealFoodPK, String unit, int quantity) {
        this.mealFoodPK = mealFoodPK;
        this.unit = unit;
        this.quantity = quantity;
    }

    public MealFood(int mealId, int foodId) {
        this.mealFoodPK = new MealFoodPK(mealId, foodId);
    }

    public MealFoodPK getMealFoodPK() {
        return mealFoodPK;
    }

    public void setMealFoodPK(MealFoodPK mealFoodPK) {
        this.mealFoodPK = mealFoodPK;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mealFoodPK != null ? mealFoodPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MealFood)) {
            return false;
        }
        MealFood other = (MealFood) object;
        if ((this.mealFoodPK == null && other.mealFoodPK != null) || (this.mealFoodPK != null && !this.mealFoodPK.equals(other.mealFoodPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.MealFood[ mealFoodPK=" + mealFoodPK + " ]";
    }
    
}
