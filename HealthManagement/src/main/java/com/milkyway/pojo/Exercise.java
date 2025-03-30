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
@Table(name = "exercise")
@NamedQueries({
    @NamedQuery(name = "Exercise.findAll", query = "SELECT e FROM Exercise e"),
    @NamedQuery(name = "Exercise.findByIdExercise", query = "SELECT e FROM Exercise e WHERE e.idExercise = :idExercise"),
    @NamedQuery(name = "Exercise.findByExerciseName", query = "SELECT e FROM Exercise e WHERE e.exerciseName = :exerciseName"),
    @NamedQuery(name = "Exercise.findByImageExercise", query = "SELECT e FROM Exercise e WHERE e.imageExercise = :imageExercise"),
    @NamedQuery(name = "Exercise.findByCaloriesBurnedPerMin", query = "SELECT e FROM Exercise e WHERE e.caloriesBurnedPerMin = :caloriesBurnedPerMin")})
public class Exercise implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idExercise")
    private Integer idExercise;
    @Basic(optional = false)
    @Column(name = "exerciseName")
    private String exerciseName;
    @Column(name = "imageExercise")
    private String imageExercise;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "caloriesBurnedPerMin")
    private Float caloriesBurnedPerMin;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "exerciseId")
    private Set<Exerciselog> exerciselogSet;

    public Exercise() {
    }

    public Exercise(Integer idExercise) {
        this.idExercise = idExercise;
    }

    public Exercise(Integer idExercise, String exerciseName) {
        this.idExercise = idExercise;
        this.exerciseName = exerciseName;
    }

    public Exercise(Integer idExercise, String exerciseName, String imageExercise, float caloriesBurnedPerMin) {
        this.idExercise = idExercise;
        this.exerciseName = exerciseName;
        this.imageExercise = imageExercise;
        this.caloriesBurnedPerMin = caloriesBurnedPerMin;
    }
    
    public Integer getIdExercise() {
        return idExercise;
    }

    public void setIdExercise(Integer idExercise) {
        this.idExercise = idExercise;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getImageExercise() {
        return imageExercise;
    }

    public void setImageExercise(String imageExercise) {
        this.imageExercise = imageExercise;
    }

    public Float getCaloriesBurnedPerMin() {
        return caloriesBurnedPerMin;
    }

    public void setCaloriesBurnedPerMin(Float caloriesBurnedPerMin) {
        this.caloriesBurnedPerMin = caloriesBurnedPerMin;
    }

    public Set<Exerciselog> getExerciselogSet() {
        return exerciselogSet;
    }

    public void setExerciselogSet(Set<Exerciselog> exerciselogSet) {
        this.exerciselogSet = exerciselogSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idExercise != null ? idExercise.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Exercise)) {
            return false;
        }
        Exercise other = (Exercise) object;
        if ((this.idExercise == null && other.idExercise != null) || (this.idExercise != null && !this.idExercise.equals(other.idExercise))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.Exercise[ idExercise=" + idExercise + " ]";
    }
    
}
