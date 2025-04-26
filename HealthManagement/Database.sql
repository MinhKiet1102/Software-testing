-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost    Database: healthmanagementdb
-- ------------------------------------------------------
-- Server version	9.1.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `exercise`
--

DROP TABLE IF EXISTS `exercise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercise` (
  `idExercise` int NOT NULL AUTO_INCREMENT,
  `exerciseName` varchar(255) NOT NULL,
  `imageExercise` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `caloriesBurnedPerMin` double DEFAULT NULL,
  `userId` int DEFAULT NULL,
  PRIMARY KEY (`idExercise`),
  KEY `fk_exercise_user` (`userId`),
  CONSTRAINT `fk_exercise_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercise`
--

LOCK TABLES `exercise` WRITE;
/*!40000 ALTER TABLE `exercise` DISABLE KEYS */;
INSERT INTO `exercise` VALUES (1,'Khiêu Vũ','Dance.png',4.5,NULL),(2,'Đi bộ','Walking.png',5,NULL),(3,'Lướt Sóng','Surfing.png',6.2,NULL),(4,'Cardio','Cardio.png',7.1,NULL),(5,'Bơi Lội','Swimming.png',10.2,NULL),(6,'Đạp Xe','Cycling.png',4.6,NULL),(7,'Gym','Gym.png',6,NULL),(8,'Chạy Bộ','Running.png',11.3,NULL),(9,'Leo Núi',NULL,10.6,1),(10,'Yoga',NULL,8,1),(11,'Chạy nước rút',NULL,10,1),(12,'Nhảy xà',NULL,8.9,1);
/*!40000 ALTER TABLE `exercise` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exerciselog`
--

DROP TABLE IF EXISTS `exerciselog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exerciselog` (
  `idExLog` int NOT NULL AUTO_INCREMENT,
  `effortLevel` varchar(50) DEFAULT NULL,
  `duration` int NOT NULL,
  `datetime` date NOT NULL,
  `energyBurn` double NOT NULL,
  `userId` int NOT NULL,
  `exerciseId` int NOT NULL,
  PRIMARY KEY (`idExLog`),
  KEY `fk_exerciselog_exercise` (`exerciseId`),
  KEY `fk_exerciselog_user` (`userId`),
  CONSTRAINT `fk_exerciselog_exercise` FOREIGN KEY (`exerciseId`) REFERENCES `exercise` (`idExercise`) ON DELETE CASCADE,
  CONSTRAINT `fk_exerciselog_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exerciselog`
--

LOCK TABLES `exerciselog` WRITE;
/*!40000 ALTER TABLE `exerciselog` DISABLE KEYS */;
INSERT INTO `exerciselog` VALUES (3,'Cao',30,'2025-04-01',305.5,1,1),(5,'Trung bình',40,'2025-03-31',300,1,3),(6,'Thấp',60,'2025-03-29',185.8,1,4),(7,'Cao',25,'2025-03-29',255,1,1),(8,'Vừa',15,'2025-04-04',90,1,7),(9,'Vừa',15,'2025-04-05',69,1,6),(12,'Vừa',30,'2025-04-03',300,1,11);
/*!40000 ALTER TABLE `exerciselog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `food`
--

DROP TABLE IF EXISTS `food`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `food` (
  `idFood` int NOT NULL AUTO_INCREMENT,
  `foodName` varchar(255) NOT NULL,
  `calories` double NOT NULL,
  `protein` double DEFAULT NULL,
  `carb` double DEFAULT NULL,
  `fat` double DEFAULT NULL,
  `sodium` double DEFAULT NULL,
  `sugar` double DEFAULT NULL,
  PRIMARY KEY (`idFood`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `food`
--

LOCK TABLES `food` WRITE;
/*!40000 ALTER TABLE `food` DISABLE KEYS */;
INSERT INTO `food` VALUES (1,'Cơm trắng',130,2.7,28,0.3,2,0.1),(2,'Ức gà (luộc)',165,31,0,3.6,65,0),(3,'Rau muống (luộc)',20,2,3.5,0.2,60,0.5),(4,'Trứng gà (luộc)',155,13,1.1,11,130,1),(5,'Táo',52,0.3,14,0.2,1,10),(6,'Sữa tươi không đường',61,3.4,4.8,3.3,45,4.8),(7,'cháo',10,1,11,1,1,1),(8,'Bánh tráng',11,1,1,1,1,1),(9,'Gà',100,1,11,1,1,1),(10,'gà',300,1,1,1,1,1),(11,'phở',10000,1,1,1,1,1),(12,'cháo gà',10,1,1,1,1,1),(13,'heo',100,1,1,1,1,1),(14,'nước ép',200,1,1,1,1,1),(15,'gà',100,1,1,1,1,1);
/*!40000 ALTER TABLE `food` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `history_date` date DEFAULT NULL,
  `history_weight` decimal(5,2) DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `history_height` int DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `history_ibfk_1` (`user_id`),
  CONSTRAINT `history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
INSERT INTO `history` VALUES (1,'2025-03-27',56.00,1,160),(2,'2025-04-03',60.00,1,164),(3,'2025-04-04',60.00,1,170),(4,'2025-04-05',50.00,1,170),(5,'2025-04-06',60.00,1,160),(15,'2025-04-12',60.00,3,170);
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal`
--

DROP TABLE IF EXISTS `meal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meal` (
  `idMeal` int NOT NULL AUTO_INCREMENT,
  `nameMeal` enum('Breakfast','Lunch','Dinner','Snack') NOT NULL,
  `totalCalories` double NOT NULL,
  `dateOfMeal` datetime NOT NULL,
  `userId` int DEFAULT NULL,
  PRIMARY KEY (`idMeal`),
  KEY `userId` (`userId`),
  CONSTRAINT `meal_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal`
--

LOCK TABLES `meal` WRITE;
/*!40000 ALTER TABLE `meal` DISABLE KEYS */;
INSERT INTO `meal` VALUES (1,'Breakfast',60000,'2025-04-11 07:00:00',1),(2,'Lunch',10000,'2025-04-11 12:30:00',1),(3,'Breakfast',78,'2025-04-11 15:00:00',2),(4,'Breakfast',1040000,'2025-04-10 00:00:00',1),(5,'Breakfast',1000,'2025-04-12 00:00:00',1),(6,'Dinner',10000,'2025-04-12 00:00:00',1);
/*!40000 ALTER TABLE `meal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `meal_food`
--

DROP TABLE IF EXISTS `meal_food`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meal_food` (
  `mealId` int NOT NULL,
  `foodId` int NOT NULL,
  `unit` enum('g','ml','piece') NOT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`mealId`,`foodId`),
  KEY `foodId` (`foodId`),
  CONSTRAINT `meal_food_ibfk_1` FOREIGN KEY (`mealId`) REFERENCES `meal` (`idMeal`) ON DELETE CASCADE,
  CONSTRAINT `meal_food_ibfk_2` FOREIGN KEY (`foodId`) REFERENCES `food` (`idFood`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `meal_food`
--

LOCK TABLES `meal_food` WRITE;
/*!40000 ALTER TABLE `meal_food` DISABLE KEYS */;
INSERT INTO `meal_food` VALUES (1,4,'piece',1),(1,14,'ml',300),(2,1,'g',200),(2,3,'g',100),(2,15,'g',100),(3,5,'piece',1),(4,9,'g',100),(4,10,'g',100),(4,11,'g',100),(5,12,'g',100),(6,13,'g',100);
/*!40000 ALTER TABLE `meal_food` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nutrition_goals`
--

DROP TABLE IF EXISTS `nutrition_goals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nutrition_goals` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `nutrition_type` varchar(50) NOT NULL,
  `goal_value` double NOT NULL,
  `unit` varchar(10) NOT NULL,
  `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_nutrition_unique` (`user_id`,`nutrition_type`),
  CONSTRAINT `nutrition_goals_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nutrition_goals`
--

LOCK TABLES `nutrition_goals` WRITE;
/*!40000 ALTER TABLE `nutrition_goals` DISABLE KEYS */;
INSERT INTO `nutrition_goals` VALUES (1,1,'Calories',200000,'kcal','2025-04-11 09:08:26','2025-04-11 09:08:39'),(2,1,'Carbohydrate',275,'g','2025-04-11 09:08:26','2025-04-11 09:08:26'),(3,1,'Protein',50,'g','2025-04-11 09:08:26','2025-04-11 09:08:26'),(4,1,'Fat',65,'g','2025-04-11 09:08:26','2025-04-11 09:08:26'),(5,1,'Sodium',2300,'mg','2025-04-11 09:08:26','2025-04-11 09:08:26'),(6,1,'Sugar',50,'g','2025-04-11 09:08:26','2025-04-11 09:08:26');
/*!40000 ALTER TABLE `nutrition_goals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `target`
--

DROP TABLE IF EXISTS `target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `target` (
  `idTarget` int NOT NULL AUTO_INCREMENT,
  `targetName` varchar(500) NOT NULL,
  `dateCreated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `startDate` datetime NOT NULL,
  `endDate` datetime NOT NULL,
  `targetNumber` float NOT NULL,
  `unit` char(15) NOT NULL,
  `progress` float NOT NULL,
  `status` enum('Not Started','In Progress','Achieved','Failed','Cancelled') NOT NULL DEFAULT 'Not Started',
  `userId` int DEFAULT NULL,
  PRIMARY KEY (`idTarget`),
  KEY `userId` (`userId`),
  CONSTRAINT `target_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `target`
--

LOCK TABLES `target` WRITE;
/*!40000 ALTER TABLE `target` DISABLE KEYS */;
INSERT INTO `target` VALUES (1,'giảm 15kg','2025-03-30 00:00:00','2025-03-31 00:00:00','2025-04-05 00:00:00',11,'kg',11,'Achieved',1),(2,'eee','2025-04-03 00:00:00','2025-04-03 00:00:00','2025-04-04 00:00:00',10,'kg',0,'Failed',1),(3,'chạy 50.1km','2025-04-06 00:00:00','2025-04-24 00:00:00','2025-05-10 00:00:00',50.1,'km',30,'Not Started',1),(5,'ddddd','2025-04-06 00:00:00','2025-04-06 00:00:00','2025-04-06 00:00:00',5,'kg',0,'Cancelled',1),(6,'aaa','2025-04-06 00:00:00','2025-04-06 00:00:00','2025-04-08 00:00:00',10,'kg',10,'Achieved',1),(7,'bbb','2025-04-06 00:00:00','2025-04-06 00:00:00','2025-04-07 00:00:00',5,'cm',0,'Cancelled',1),(9,'Chạy nước rút','2025-04-08 00:00:00','2025-04-08 00:00:00','2025-04-30 00:00:00',30,'km',10,'In Progress',1),(10,'Chạy nước rút 17km','2025-04-12 00:00:00','2025-04-12 00:00:00','2025-04-13 00:00:00',30,'km',0,'Failed',1),(11,'tập gym','2025-04-12 00:00:00','2025-04-12 00:00:00','2025-04-13 00:00:00',20,'h',0,'In Progress',3);
/*!40000 ALTER TABLE `target` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `current_weight` decimal(5,2) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `height` int DEFAULT NULL,
  `registration_date` date DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'USER',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'miki','12345678','minhkietle421@gmail.com','Nam',60.00,20,175,'2025-04-04','USER'),(2,'admin','12345678','admin@gmail.com','Nam',56.00,21,164,'2025-03-27','ADMIN'),(3,'thanhno','12345678','thanhno0308@gmail.com','Nam',60.00,20,170,'2025-04-12','USER');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'healthmanagementdb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-20 22:59:19
