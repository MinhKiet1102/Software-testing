/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Admin
 */
@Entity
@Table(name = "user")
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
    @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id"),
    @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE u.username = :username"),
    @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM User u WHERE u.password = :password"),
    @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
    @NamedQuery(name = "User.findByGender", query = "SELECT u FROM User u WHERE u.gender = :gender"),
    @NamedQuery(name = "User.findByCurrentWeight", query = "SELECT u FROM User u WHERE u.currentWeight = :currentWeight"),
    @NamedQuery(name = "User.findByAge", query = "SELECT u FROM User u WHERE u.age = :age"),
    @NamedQuery(name = "User.findByHeight", query = "SELECT u FROM User u WHERE u.height = :height"),
    @NamedQuery(name = "User.findByRegistrationDate", query = "SELECT u FROM User u WHERE u.registrationDate = :registrationDate")})
public class User implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<History> historySet;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "username")
    private String username;
    @Basic(optional = false)
    @Column(name = "password")
    private String password;
    @Basic(optional = false)
    @Column(name = "email")
    private String email;
    @Column(name = "gender")
    private String gender;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "current_weight")
    private BigDecimal currentWeight;
    @Column(name = "age")
    private Integer age;
    @Column(name = "height")
    private Integer height;
    @Column(name = "registration_date")
    @Temporal(TemporalType.DATE)
    private Date registrationDate;
    @OneToMany(mappedBy = "userId")
    private Set<Meal> mealSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private Set<Exerciselog> exerciselogSet;
    @OneToMany(mappedBy = "userId")
    private Set<Target> targetSet;

    private static User currentUser;

    public User() {
    }

    public User(Integer id) {
        this.id = id;
    }

    public User(Integer id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(Integer id, String username, String password, String email, String gender, BigDecimal currentWeight, Integer age, Integer height, Date registrationDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.gender = gender;
        this.currentWeight = currentWeight;
        this.age = age;
        this.height = height;
        this.registrationDate = registrationDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public BigDecimal getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(BigDecimal currentWeight) {
        this.currentWeight = currentWeight;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Set<Meal> getMealSet() {
        return mealSet;
    }

    public void setMealSet(Set<Meal> mealSet) {
        this.mealSet = mealSet;
    }

    public Set<Exerciselog> getExerciselogSet() {
        return exerciselogSet;
    }

    public void setExerciselogSet(Set<Exerciselog> exerciselogSet) {
        this.exerciselogSet = exerciselogSet;
    }

    public Set<Target> getTargetSet() {
        return targetSet;
    }

    public void setTargetSet(Set<Target> targetSet) {
        this.targetSet = targetSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.milkyway.pojo.User[ id=" + id + " ]";
    }

    /**
     * @return the currentUser
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * @param aCurrentUser the currentUser to set
     */
    public static void setCurrentUser(User aCurrentUser) {
        currentUser = aCurrentUser;
    }

    public Set<History> getHistorySet() {
        return historySet;
    }

    public void setHistorySet(Set<History> historySet) {
        this.historySet = historySet;
    }

    public double calculateBMI() {
        if (this.height == null) {
            throw new IllegalStateException("Height is not initialized");
        }
        // BMI calculation: BMI = weight (kg) / (height (m))^2
        double heightInMeters = height / 100.0; // Convert height to meters
        return Math.round((currentWeight.doubleValue() / (heightInMeters * heightInMeters)) * 100.0) / 100.0;
    }

    public BigDecimal calculateOptimalWeight() {
        // Optimal weight calculation (e.g., BMI of 22)
        double optimalBMI = 22.0;
        double optimalWeight = optimalBMI * (height / 100.0) * (height / 100.0);

        BigDecimal optimalWeightBigDecimal = BigDecimal.valueOf(optimalWeight);
        return optimalWeightBigDecimal.setScale(2, RoundingMode.HALF_UP);
    }

    public String determineWeightStatus() {
        double bmi = calculateBMI();

        if (bmi < 18.5) {
            return "Thiếu cân";
        } else if (bmi >= 18.5 && bmi < 25) {
            return "Cân nặng bình thường";
        } else if (bmi >= 25 && bmi < 30) {
            return "Thừa cân";
        } else {
            return "Béo phì";
        }
    }

    public BigDecimal calculateWeightToLoseOrGain() {
        BigDecimal optimalWeight = calculateOptimalWeight();
        BigDecimal weightDifference = optimalWeight.subtract(currentWeight).abs();
        return weightDifference.setScale(2, RoundingMode.HALF_UP);
    }

    public String determineWeightChangeRecommendation() {
        double bmi = calculateBMI();

        // Xác định phạm vi BMI cho các khuyến nghị
        double normalWeightLowerThreshold = 18.5;

        if (bmi < normalWeightLowerThreshold) {
            return "tăng";
        } else {
            return "giảm";
        }
    }

    public int calculateCaloriesPerDay() {
        // Example: Tính toán lượng calo cần thiết để kiểm soát cân nặng

        // Lượng calo cơ bản cần thiết để duy trì cân nặng
        int baseCalories = 2000;

        // Điều chỉnh lượng calo dựa trên mục tiêu thay đổi cân nặng (giảm, duy trì, tăng)
        double weightChangeMultiplier = getWeightChangeMultiplier();
        int adjustedCalories = (int) (baseCalories * weightChangeMultiplier);

        // Điều chỉnh lượng calo dựa trên các yếu tố khác (giới tính, mức độ hoạt động, v.v.
        double genderMultiplier = getGenderMultiplier();
        double activityLevelMultiplier = 1;

        // Kết hợp tất cả các số nhân để có được lượng calo đã điều chỉnh cuối cùng
        int finalCalories = (int) (adjustedCalories * genderMultiplier * activityLevelMultiplier);

        return finalCalories;
    }

    private double getWeightChangeMultiplier() {
        String weightChangeRecommendation = determineWeightChangeRecommendation();

        // điều chỉnh dựa trên mục tiêu thay đổi cân nặng
        switch (weightChangeRecommendation) {
            case "tăng":
                return 1.2; // Tăng lượng calo để tăng cân
            case "giảm":
                return 0.8; // Giảm lượng calo để giảm cân
            default:
                return 1.0; // Duy trì lượng calo để duy trì cân nặng
        }
    }

    private double getGenderMultiplier() {
        // Điều chỉnh dựa trên giới tính
        if ("Nam".equalsIgnoreCase(gender)) {
            return 1.1; // Điều chỉnh cho giới tính nam
        } else {
            return 1.0; // Điều chỉnh cho giới tính nữ
        }
    }

}
