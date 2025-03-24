/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "meal")
@NamedQueries({
    @NamedQuery(name = "Meal.findAll", query = "SELECT m FROM Meal m"),
    @NamedQuery(name = "Meal.findByIdMeal", query = "SELECT m FROM Meal m WHERE m.idMeal = :idMeal"),
    @NamedQuery(name = "Meal.findByNameMeal", query = "SELECT m FROM Meal m WHERE m.nameMeal = :nameMeal"),
    @NamedQuery(name = "Meal.findByTotalCalories", query = "SELECT m FROM Meal m WHERE m.totalCalories = :totalCalories"),
    @NamedQuery(name = "Meal.findByDateOfMeal", query = "SELECT m FROM Meal m WHERE m.dateOfMeal = :dateOfMeal")})
public class Meal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "idMeal")
    private String idMeal;
    @Basic(optional = false)
    @Column(name = "nameMeal")
    private String nameMeal;
    @Basic(optional = false)
    @Column(name = "totalCalories")
    private double totalCalories;
    @Basic(optional = false)
    @Column(name = "dateOfMeal")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateOfMeal;
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @ManyToOne
    private User userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "meal")
    private Set<MealFood> mealFoodSet;

    public Meal() {
    }

    public Meal(String idMeal) {
        this.idMeal = idMeal;
    }

    public Meal(String idMeal, String nameMeal, double totalCalories, Date dateOfMeal) {
        this.idMeal = idMeal;
        this.nameMeal = nameMeal;
        this.totalCalories = totalCalories;
        this.dateOfMeal = dateOfMeal;
    }

    public String getIdMeal() {
        return idMeal;
    }

    public void setIdMeal(String idMeal) {
        this.idMeal = idMeal;
    }

    public String getNameMeal() {
        return nameMeal;
    }

    public void setNameMeal(String nameMeal) {
        this.nameMeal = nameMeal;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Date getDateOfMeal() {
        return dateOfMeal;
    }

    public void setDateOfMeal(Date dateOfMeal) {
        this.dateOfMeal = dateOfMeal;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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
        hash += (idMeal != null ? idMeal.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Meal)) {
            return false;
        }
        Meal other = (Meal) object;
        if ((this.idMeal == null && other.idMeal != null) || (this.idMeal != null && !this.idMeal.equals(other.idMeal))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.Meal[ idMeal=" + idMeal + " ]";
    }
    
}
