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
@Table(name = "target")
@NamedQueries({
    @NamedQuery(name = "Target.findAll", query = "SELECT t FROM Target t"),
    @NamedQuery(name = "Target.findByIdTarget", query = "SELECT t FROM Target t WHERE t.idTarget = :idTarget"),
    @NamedQuery(name = "Target.findByTargetName", query = "SELECT t FROM Target t WHERE t.targetName = :targetName"),
    @NamedQuery(name = "Target.findByDateCreated", query = "SELECT t FROM Target t WHERE t.dateCreated = :dateCreated"),
    @NamedQuery(name = "Target.findByStartDate", query = "SELECT t FROM Target t WHERE t.startDate = :startDate"),
    @NamedQuery(name = "Target.findByEndDate", query = "SELECT t FROM Target t WHERE t.endDate = :endDate"),
    @NamedQuery(name = "Target.findByTargetNumber", query = "SELECT t FROM Target t WHERE t.targetNumber = :targetNumber"),
    @NamedQuery(name = "Target.findByUnit", query = "SELECT t FROM Target t WHERE t.unit = :unit"),
    @NamedQuery(name = "Target.findByProgress", query = "SELECT t FROM Target t WHERE t.progress = :progress"),
    @NamedQuery(name = "Target.findByStatus", query = "SELECT t FROM Target t WHERE t.status = :status")})
public class Target implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "idTarget")
    private String idTarget;
    @Basic(optional = false)
    @Column(name = "targetName")
    private String targetName;
    @Basic(optional = false)
    @Column(name = "dateCreated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    @Basic(optional = false)
    @Column(name = "startDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Basic(optional = false)
    @Column(name = "endDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @Basic(optional = false)
    @Column(name = "targetNumber")
    private float targetNumber;
    @Basic(optional = false)
    @Column(name = "unit")
    private String unit;
    @Basic(optional = false)
    @Column(name = "progress")
    private float progress;
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @ManyToOne
    private User userId;

    public Target() {
    }

    public Target(String idTarget) {
        this.idTarget = idTarget;
    }

    public Target(String idTarget, String targetName, Date dateCreated, Date startDate, Date endDate, float targetNumber, String unit, float progress, String status) {
        this.idTarget = idTarget;
        this.targetName = targetName;
        this.dateCreated = dateCreated;
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetNumber = targetNumber;
        this.unit = unit;
        this.progress = progress;
        this.status = status;
    }

    public String getIdTarget() {
        return idTarget;
    }

    public void setIdTarget(String idTarget) {
        this.idTarget = idTarget;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public float getTargetNumber() {
        return targetNumber;
    }

    public void setTargetNumber(float targetNumber) {
        this.targetNumber = targetNumber;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        hash += (idTarget != null ? idTarget.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Target)) {
            return false;
        }
        Target other = (Target) object;
        if ((this.idTarget == null && other.idTarget != null) || (this.idTarget != null && !this.idTarget.equals(other.idTarget))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.Target[ idTarget=" + idTarget + " ]";
    }
    
}
