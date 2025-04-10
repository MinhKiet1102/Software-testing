/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.milkyway.interfaces;

import com.milkyway.pojo.Exerciselog;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ASUS
 */
public interface ExerciseLogInterface {
    List<Exerciselog> findAll();

    List<Exerciselog> findByUserId(int userId);

    void save(Exerciselog userExercise);
    List<Exerciselog> findByUserIdAndDate(int userId, Date exerciseDate);

    void deleteByUserIdAndExerciseIdAndExerciseDate(int userId, int exerciseId, Date exerciseDate);

    double calculateTotalBurnedCalories(int userId, Date exerciseDate); 
}
