package com.milkyway.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity class for nutrition goals
 */
@Entity
@Table(name = "nutrition_goals")
public class NutritionGoal implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "nutrition_type")
    private String nutritionType;
    
    @Column(name = "goal_value")
    private double goalValue;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    
    @Column(name = "modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    public NutritionGoal() {
    }
    
    public NutritionGoal(User user, String nutritionType, double goalValue, String unit) {
        this.user = user;
        this.nutritionType = nutritionType;
        this.goalValue = goalValue;
        this.unit = unit;
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNutritionType() {
        return nutritionType;
    }

    public void setNutritionType(String nutritionType) {
        this.nutritionType = nutritionType;
    }

    public double getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(double goalValue) {
        this.goalValue = goalValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    public void setUserId(int userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }
    
    public int getUserId() {
        if (this.user != null) {
            return this.user.getId();
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "NutritionGoal{" + "id=" + id + ", nutritionType=" + nutritionType + 
                ", goalValue=" + goalValue + ", unit=" + unit + '}';
    }
}