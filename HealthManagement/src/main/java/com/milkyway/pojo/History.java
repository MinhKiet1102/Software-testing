/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "history")
@NamedQueries({
    @NamedQuery(name = "History.findAll", query = "SELECT h FROM History h"),
    @NamedQuery(name = "History.findByHistoryId", query = "SELECT h FROM History h WHERE h.historyId = :historyId"),
    @NamedQuery(name = "History.findByHistoryDate", query = "SELECT h FROM History h WHERE h.historyDate = :historyDate"),
    @NamedQuery(name = "History.findByHistoryWeight", query = "SELECT h FROM History h WHERE h.historyWeight = :historyWeight"),
    @NamedQuery(name = "History.findByHistoryHeight", query = "SELECT h FROM History h WHERE h.historyHeight = :historyHeight")})
public class History implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "history_id")
    private Integer historyId;
    @Column(name = "history_date")
    @Temporal(TemporalType.DATE)
    private Date historyDate;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "history_weight")
    private BigDecimal historyWeight;
    @Column(name = "history_height")
    private Integer historyHeight;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne
    private User userId;

    public History() {
    }
    
    public History(Integer historyId, Date historyDate, BigDecimal historyWeight, Integer historyHeight) {
        this.historyId = historyId;
        this.historyDate = historyDate;
        this.historyWeight = historyWeight;
        this.historyHeight = historyHeight;
    }

    public History(Integer historyId) {
        this.historyId = historyId;
    }

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Date getHistoryDate() {
        return historyDate;
    }

    public void setHistoryDate(Date historyDate) {
        this.historyDate = historyDate;
    }

    public BigDecimal getHistoryWeight() {
        return historyWeight;
    }

    public void setHistoryWeight(BigDecimal historyWeight) {
        this.historyWeight = historyWeight;
    }

    public Integer getHistoryHeight() {
        return historyHeight;
    }

    public void setHistoryHeight(Integer historyHeight) {
        this.historyHeight = historyHeight;
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
        hash += (historyId != null ? historyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof History)) {
            return false;
        }
        History other = (History) object;
        if ((this.historyId == null && other.historyId != null) || (this.historyId != null && !this.historyId.equals(other.historyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.History[ historyId=" + historyId + " ]";
    }
    
}
