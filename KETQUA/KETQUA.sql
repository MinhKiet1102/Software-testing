-- File: KETQUA.sql
-- Database for soccer team management

-- 1. Create database
USE master;
GO

-- Drop database if it exists
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'QLTRANDAU')
BEGIN
    ALTER DATABASE QLTRANDAU SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLTRANDAU;
END
GO

CREATE DATABASE QLTRANDAU;
GO

USE QLTRANDAU;
GO

-- 2. Create tables for global schema
CREATE TABLE DOIBONG (
    MaDB VARCHAR(10) PRIMARY KEY,
    TenDB NVARCHAR(100) NOT NULL,
    CLB VARCHAR(10) NOT NULL
);

CREATE TABLE CAUTHU (
    MaCT VARCHAR(10) PRIMARY KEY,
    TenCT NVARCHAR(100) NOT NULL,
    MaDB VARCHAR(10) REFERENCES DOIBONG(MaDB)
);

CREATE TABLE TRANDAU (
    MaTD VARCHAR(10) PRIMARY KEY,
    MaDB1 VARCHAR(10) REFERENCES DOIBONG(MaDB),
    MaDB2 VARCHAR(10) REFERENCES DOIBONG(MaDB),
    TrongTai NVARCHAR(100) NOT NULL,
    SanDau VARCHAR(10) NOT NULL
);

CREATE TABLE THAMGIA (
    MaTD VARCHAR(10) REFERENCES TRANDAU(MaTD),
    MaCT VARCHAR(10) REFERENCES CAUTHU(MaCT),
    SoTrai INT NOT NULL,
    PRIMARY KEY (MaTD, MaCT)
);

-- 3. Insert data (5 rows for each table)
-- DOIBONG data
INSERT INTO DOIBONG (MaDB, TenDB, CLB) VALUES
    ('DB001', N'Manchester United', 'CLB1'),
    ('DB002', N'Real Madrid', 'CLB2'),
    ('DB003', N'Barcelona', 'CLB1'),
    ('DB004', N'Bayern Munich', 'CLB2'),
    ('DB005', N'Liverpool', 'CLB1');

-- CAUTHU data
INSERT INTO CAUTHU (MaCT, TenCT, MaDB) VALUES
    ('CT001', N'Cristiano Ronaldo', 'DB002'),
    ('CT002', N'Lionel Messi', 'DB003'),
    ('CT003', N'Robert Lewandowski', 'DB004'),
    ('CT004', N'Bruno Fernandes', 'DB001'),
    ('CT005', N'Mohamed Salah', 'DB005'),
    ('CT006', N'Harry Kane', 'DB001'),
    ('CT007', N'Karim Benzema', 'DB002'),
    ('CT008', N'Thomas Müller', 'DB004');

-- TRANDAU data
INSERT INTO TRANDAU (MaTD, MaDB1, MaDB2, TrongTai, SanDau) VALUES
    ('TD001', 'DB001', 'DB002', N'Howard Webb', 'SD1'),
    ('TD002', 'DB003', 'DB004', N'Pierluigi Collina', 'SD2'),
    ('TD003', 'DB005', 'DB001', N'Felix Brych', 'SD1'),
    ('TD004', 'DB002', 'DB003', N'Mark Clattenburg', 'SD2'),
    ('TD005', 'DB004', 'DB005', N'Björn Kuipers', 'SD1');

-- THAMGIA data
INSERT INTO THAMGIA (MaTD, MaCT, SoTrai) VALUES
    ('TD001', 'CT004', 1),
    ('TD001', 'CT001', 2),
    ('TD002', 'CT002', 2),
    ('TD002', 'CT003', 1),
    ('TD003', 'CT005', 3),
    ('TD003', 'CT006', 0),
    ('TD004', 'CT001', 1),
    ('TD004', 'CT002', 1),
    ('TD005', 'CT003', 2),
    ('TD005', 'CT005', 1);

-- 4. Creating data fragments according to requirements

-- a. DOIBONG fragments based on CLB values
CREATE TABLE DOIBONG_CLB1 (
    MaDB VARCHAR(10) PRIMARY KEY,
    TenDB NVARCHAR(100) NOT NULL,
    CLB VARCHAR(10) NOT NULL
);

CREATE TABLE DOIBONG_CLB2 (
    MaDB VARCHAR(10) PRIMARY KEY,
    TenDB NVARCHAR(100) NOT NULL,
    CLB VARCHAR(10) NOT NULL
);

-- Insert data into DOIBONG fragments
INSERT INTO DOIBONG_CLB1
SELECT * FROM DOIBONG WHERE CLB = 'CLB1';

INSERT INTO DOIBONG_CLB2
SELECT * FROM DOIBONG WHERE CLB = 'CLB2';

-- b. CAUTHU fragments based on DOIBONG (MaDB)
CREATE TABLE CAUTHU_CLB1 (
    MaCT VARCHAR(10) PRIMARY KEY,
    TenCT NVARCHAR(100) NOT NULL,
    MaDB VARCHAR(10) REFERENCES DOIBONG_CLB1(MaDB)
);

CREATE TABLE CAUTHU_CLB2 (
    MaCT VARCHAR(10) PRIMARY KEY,
    TenCT NVARCHAR(100) NOT NULL,
    MaDB VARCHAR(10) REFERENCES DOIBONG_CLB2(MaDB)
);

-- Insert data into CAUTHU fragments
INSERT INTO CAUTHU_CLB1
SELECT C.*
FROM CAUTHU C
JOIN DOIBONG_CLB1 D ON C.MaDB = D.MaDB;

INSERT INTO CAUTHU_CLB2
SELECT C.*
FROM CAUTHU C
JOIN DOIBONG_CLB2 D ON C.MaDB = D.MaDB;

-- c. TRANDAU fragments based on SanDau values
CREATE TABLE TRANDAU_SD1 (
    MaTD VARCHAR(10) PRIMARY KEY,
    MaDB1 VARCHAR(10),
    MaDB2 VARCHAR(10),
    TrongTai NVARCHAR(100) NOT NULL,
    SanDau VARCHAR(10) NOT NULL
);

CREATE TABLE TRANDAU_SD2 (
    MaTD VARCHAR(10) PRIMARY KEY,
    MaDB1 VARCHAR(10),
    MaDB2 VARCHAR(10),
    TrongTai NVARCHAR(100) NOT NULL,
    SanDau VARCHAR(10) NOT NULL
);

-- Insert data into TRANDAU fragments
INSERT INTO TRANDAU_SD1
SELECT * FROM TRANDAU WHERE SanDau = 'SD1';

INSERT INTO TRANDAU_SD2
SELECT * FROM TRANDAU WHERE SanDau = 'SD2';

-- d. THAMGIA fragments based on TRANDAU (MaTD)
CREATE TABLE THAMGIA_SD1 (
    MaTD VARCHAR(10) REFERENCES TRANDAU_SD1(MaTD),
    MaCT VARCHAR(10),
    SoTrai INT NOT NULL,
    PRIMARY KEY (MaTD, MaCT)
);

CREATE TABLE THAMGIA_SD2 (
    MaTD VARCHAR(10) REFERENCES TRANDAU_SD2(MaTD),
    MaCT VARCHAR(10),
    SoTrai INT NOT NULL,
    PRIMARY KEY (MaTD, MaCT)
);

-- Insert data into THAMGIA fragments
INSERT INTO THAMGIA_SD1
SELECT TG.*
FROM THAMGIA TG
JOIN TRANDAU_SD1 TD ON TG.MaTD = TD.MaTD;

INSERT INTO THAMGIA_SD2
SELECT TG.*
FROM THAMGIA TG
JOIN TRANDAU_SD2 TD ON TG.MaTD = TD.MaTD;

-- 5. Create a stored procedure for player transfer between teams
GO
CREATE PROCEDURE ChuyenCauThu
    @MaCT VARCHAR(10),
    @MaDBMoi VARCHAR(10)
AS
BEGIN
    BEGIN TRY
        -- Begin transaction
        BEGIN TRANSACTION;

        -- Get current team information of the player
        DECLARE @MaDBCu VARCHAR(10);
        DECLARE @CLBCu VARCHAR(10);
        DECLARE @CLBMoi VARCHAR(10);

        -- Get current team and club of the player
        SELECT @MaDBCu = MaDB 
        FROM CAUTHU
        WHERE MaCT = @MaCT;

        IF @MaDBCu IS NULL
        BEGIN
            RAISERROR('Cầu thủ không tồn tại', 16, 1);
            ROLLBACK;
            RETURN;
        END

        -- Make sure new team exists
        IF NOT EXISTS (SELECT 1 FROM DOIBONG WHERE MaDB = @MaDBMoi)
        BEGIN
            RAISERROR('Đội bóng mới không tồn tại', 16, 1);
            ROLLBACK;
            RETURN;
        END

        -- Get club information for both teams
        SELECT @CLBCu = CLB FROM DOIBONG WHERE MaDB = @MaDBCu;
        SELECT @CLBMoi = CLB FROM DOIBONG WHERE MaDB = @MaDBMoi;

        -- Update player in global schema
        UPDATE CAUTHU
        SET MaDB = @MaDBMoi
        WHERE MaCT = @MaCT;

        -- Handle fragments based on club changes
        -- Case 1: Player stays in the same club type
        IF @CLBCu = @CLBMoi
        BEGIN
            IF @CLBCu = 'CLB1'
            BEGIN
                UPDATE CAUTHU_CLB1
                SET MaDB = @MaDBMoi
                WHERE MaCT = @MaCT;
            END
            ELSE -- CLB2
            BEGIN
                UPDATE CAUTHU_CLB2
                SET MaDB = @MaDBMoi
                WHERE MaCT = @MaCT;
            END
        END
        -- Case 2: Player changes from CLB1 to CLB2
        ELSE IF @CLBCu = 'CLB1' AND @CLBMoi = 'CLB2'
        BEGIN
            -- Delete from CLB1 fragment
            DELETE FROM CAUTHU_CLB1 WHERE MaCT = @MaCT;
            
            -- Insert into CLB2 fragment
            INSERT INTO CAUTHU_CLB2 (MaCT, TenCT, MaDB)
            SELECT MaCT, TenCT, @MaDBMoi
            FROM CAUTHU
            WHERE MaCT = @MaCT;
        END
        -- Case 3: Player changes from CLB2 to CLB1
        ELSE IF @CLBCu = 'CLB2' AND @CLBMoi = 'CLB1'
        BEGIN
            -- Delete from CLB2 fragment
            DELETE FROM CAUTHU_CLB2 WHERE MaCT = @MaCT;
            
            -- Insert into CLB1 fragment
            INSERT INTO CAUTHU_CLB1 (MaCT, TenCT, MaDB)
            SELECT MaCT, TenCT, @MaDBMoi
            FROM CAUTHU
            WHERE MaCT = @MaCT;
        END

        COMMIT;
        PRINT 'Cầu thủ ' + @MaCT + ' đã chuyển từ đội ' + @MaDBCu + ' sang đội ' + @MaDBMoi + ' thành công';
    END TRY
    BEGIN CATCH
        -- If an error occurs, rollback the transaction
        IF @@TRANCOUNT > 0
            ROLLBACK;
        
        -- Print error information
        PRINT 'Lỗi: ' + ERROR_MESSAGE();
    END CATCH
END;
GO

-- 6. Test cases
-- Test case 1: Transfer a player from CLB1 team to another CLB1 team
PRINT '--- TEST CASE 1: TRANSFER PLAYER FROM CLB1 TO ANOTHER CLB1 TEAM ---';
PRINT 'Before transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT004';

SELECT 'CAUTHU_CLB1' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB1 C
WHERE C.MaCT = 'CT004';

EXEC ChuyenCauThu 'CT004', 'DB003'; -- Transfer from Manchester United (CLB1) to Barcelona (CLB1)

PRINT 'After transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT004';

SELECT 'CAUTHU_CLB1' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB1 C
WHERE C.MaCT = 'CT004';

-- Test case 2: Transfer a player from CLB1 team to CLB2 team
PRINT '--- TEST CASE 2: TRANSFER PLAYER FROM CLB1 TO CLB2 TEAM ---';
PRINT 'Before transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT005';

SELECT 'CAUTHU_CLB1' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB1 C
WHERE C.MaCT = 'CT005';

SELECT 'CAUTHU_CLB2' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB2 C
WHERE C.MaCT = 'CT005';

EXEC ChuyenCauThu 'CT005', 'DB004'; -- Transfer from Liverpool (CLB1) to Bayern Munich (CLB2)

PRINT 'After transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT005';

SELECT 'CAUTHU_CLB1' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB1 C
WHERE C.MaCT = 'CT005';

SELECT 'CAUTHU_CLB2' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB2 C
WHERE C.MaCT = 'CT005';

-- Test case 3: Transfer a player from CLB2 team to CLB1 team
PRINT '--- TEST CASE 3: TRANSFER PLAYER FROM CLB2 TO CLB1 TEAM ---';
PRINT 'Before transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT001';

SELECT 'CAUTHU_CLB1' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB1 C
WHERE C.MaCT = 'CT001';

SELECT 'CAUTHU_CLB2' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB2 C
WHERE C.MaCT = 'CT001';

EXEC ChuyenCauThu 'CT001', 'DB005'; -- Transfer from Real Madrid (CLB2) to Liverpool (CLB1)

PRINT 'After transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT001';

SELECT 'CAUTHU_CLB1' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB1 C
WHERE C.MaCT = 'CT001';

SELECT 'CAUTHU_CLB2' AS [Table], C.MaCT, C.TenCT, C.MaDB
FROM CAUTHU_CLB2 C
WHERE C.MaCT = 'CT001';

-- Test case 4: Transfer a player within the same team (should be no change)
PRINT '--- TEST CASE 4: TRANSFER PLAYER TO THE SAME TEAM ---';
PRINT 'Before transfer:';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT003';

EXEC ChuyenCauThu 'CT003', 'DB004'; -- Transfer to the same team (Bayern Munich)

PRINT 'After transfer (should be no change):';
SELECT 'Global Schema' AS [Table], C.MaCT, C.TenCT, C.MaDB, D.CLB
FROM CAUTHU C
JOIN DOIBONG D ON C.MaDB = D.MaDB
WHERE C.MaCT = 'CT003';

-- Test case 5: Invalid player ID
PRINT '--- TEST CASE 5: INVALID PLAYER ID ---';
EXEC ChuyenCauThu 'CT999', 'DB001'; -- Invalid player ID

-- Test case 6: Invalid team ID
PRINT '--- TEST CASE 6: INVALID TEAM ID ---';
EXEC ChuyenCauThu 'CT002', 'DB999'; -- Invalid team ID

-- Display final data in all tables
PRINT '--- FINAL DATA IN ALL TABLES ---';
PRINT 'CAUTHU (Global Schema):';
SELECT * FROM CAUTHU;

PRINT 'CAUTHU_CLB1:';
SELECT * FROM CAUTHU_CLB1;

PRINT 'CAUTHU_CLB2:';
SELECT * FROM CAUTHU_CLB2;