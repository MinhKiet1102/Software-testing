/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
    @NamedQuery(name = "Exercise.findByEffortLevel", query = "SELECT e FROM Exercise e WHERE e.effortLevel = :effortLevel"),
    @NamedQuery(name = "Exercise.findByDuration", query = "SELECT e FROM Exercise e WHERE e.duration = :duration"),
    @NamedQuery(name = "Exercise.findByEnergyBurn", query = "SELECT e FROM Exercise e WHERE e.energyBurn = :energyBurn")})
public class Exercise implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "idExercise")
    private String idExercise;
    @Basic(optional = false)
    @Column(name = "exerciseName")
    private String exerciseName;
    @Lob
    @Column(name = "imageExercise")
    private byte[] imageExercise;
    @Column(name = "effortLevel")
    private String effortLevel;
    @Basic(optional = false)
    @Column(name = "duration")
    @Temporal(TemporalType.TIME)
    private Date duration;
    @Basic(optional = false)
    @Column(name = "energyBurn")
    private double energyBurn;
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @ManyToOne
    private User userId;

    public Exercise() {
    }

    public Exercise(String idExercise) {
        this.idExercise = idExercise;
    }

    public Exercise(String idExercise, String exerciseName, Date duration, double energyBurn) {
        this.idExercise = idExercise;
        this.exerciseName = exerciseName;
        this.duration = duration;
        this.energyBurn = energyBurn;
    }

    public String getIdExercise() {
        return idExercise;
    }

    public void setIdExercise(String idExercise) {
        this.idExercise = idExercise;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public byte[] getImageExercise() {
        return imageExercise;
    }

    public void setImageExercise(byte[] imageExercise) {
        this.imageExercise = imageExercise;
    }

    public String getEffortLevel() {
        return effortLevel;
    }

    public void setEffortLevel(String effortLevel) {
        this.effortLevel = effortLevel;
    }

    public Date getDuration() {
        return duration;
    }

    public void setDuration(Date duration) {
        this.duration = duration;
    }

    public double getEnergyBurn() {
        return energyBurn;
    }

    public void setEnergyBurn(double energyBurn) {
        this.energyBurn = energyBurn;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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
