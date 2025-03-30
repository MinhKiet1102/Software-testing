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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(name = "exerciselog")
@NamedQueries({
    @NamedQuery(name = "Exerciselog.findAll", query = "SELECT e FROM Exerciselog e"),
    @NamedQuery(name = "Exerciselog.findByIdExLog", query = "SELECT e FROM Exerciselog e WHERE e.idExLog = :idExLog"),
    @NamedQuery(name = "Exerciselog.findByEffortLevel", query = "SELECT e FROM Exerciselog e WHERE e.effortLevel = :effortLevel"),
    @NamedQuery(name = "Exerciselog.findByDuration", query = "SELECT e FROM Exerciselog e WHERE e.duration = :duration"),
    @NamedQuery(name = "Exerciselog.findByEnergyBurn", query = "SELECT e FROM Exerciselog e WHERE e.energyBurn = :energyBurn")})
public class Exerciselog implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idExLog")
    private Integer idExLog;
    @Column(name = "effortLevel")
    private String effortLevel;
    @Basic(optional = false)
    @Column(name = "duration")
    @Temporal(TemporalType.TIME)
    private int duration;
    @Basic(optional = false)
    @Column(name = "datetime")
    @Temporal(TemporalType.DATE)
    private Date datetime;
    @Basic(optional = false)
    @Column(name = "energyBurn")
    private double energyBurn;
    @JoinColumn(name = "exerciseId", referencedColumnName = "idExercise")
    @ManyToOne(optional = false)
    private Exercise exerciseId;
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User userId;

    public Exerciselog() {
    }

    public Exerciselog(Integer idExLog) {
        this.idExLog = idExLog;
    }

    public Exerciselog(Integer idExLog, int duration, double energyBurn, Date datetime) {
        this.idExLog = idExLog;
        this.duration = duration;
        this.energyBurn = energyBurn;
        this.datetime = datetime;
    }

    public Integer getIdExLog() {
        return idExLog;
    }

    public void setIdExLog(Integer idExLog) {
        this.idExLog = idExLog;
    }

    public String getEffortLevel() {
        return effortLevel;
    }

    public void setEffortLevel(String effortLevel) {
        this.effortLevel = effortLevel;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }


    public double getEnergyBurn() {
        return energyBurn;
    }

    public void setEnergyBurn(double energyBurn) {
        this.energyBurn = energyBurn;
    }

    public Exercise getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Exercise exerciseId) {
        this.exerciseId = exerciseId;
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
        hash += (idExLog != null ? idExLog.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Exerciselog)) {
            return false;
        }
        Exerciselog other = (Exerciselog) object;
        if ((this.idExLog == null && other.idExLog != null) || (this.idExLog != null && !this.idExLog.equals(other.idExLog))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.Exerciselog[ idExLog=" + idExLog + " ]";
    }
}
