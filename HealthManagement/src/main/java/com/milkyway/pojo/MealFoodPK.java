/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Admin
 */
@Embeddable
public class MealFoodPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "mealId")
    private int mealId;
    @Basic(optional = false)
    @Column(name = "foodId")
    private int foodId;

    public MealFoodPK() {
    }

    public MealFoodPK(int mealId, int foodId) {
        this.mealId = mealId;
        this.foodId = foodId;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) mealId;
        hash += (int) foodId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MealFoodPK)) {
            return false;
        }
        MealFoodPK other = (MealFoodPK) object;
        if (this.mealId != other.mealId) {
            return false;
        }
        if (this.foodId != other.foodId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.MealFoodPK[ mealId=" + mealId + ", foodId=" + foodId + " ]";
    }
    
}
