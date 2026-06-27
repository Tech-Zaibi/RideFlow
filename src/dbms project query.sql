USE master;
GO
DROP DATABASE IF EXISTS RideFlowDB;
GO
CREATE DATABASE RideFlowDB;
GO
USE RideFlowDB;
GO

CREATE TABLE Users (
    UserID       INT IDENTITY(1,1) PRIMARY KEY,
    Username     VARCHAR(50)  NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    Email        VARCHAR(100) NOT NULL UNIQUE,
    Phone        VARCHAR(20),
    Role         VARCHAR(10)  NOT NULL CHECK (Role IN ('Passenger', 'Driver')),
    CreatedAt    DATETIME     DEFAULT GETDATE()
);
GO

CREATE TABLE Passengers (
    PassengerID INT IDENTITY(1,1) PRIMARY KEY,
    UserID      INT NOT NULL UNIQUE,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
GO

CREATE TABLE Drivers (
    DriverID      INT IDENTITY(1,1) PRIMARY KEY,
    UserID        INT NOT NULL UNIQUE,
    LicenseNumber VARCHAR(50)  NOT NULL UNIQUE,
    VehicleType   VARCHAR(30)  NOT NULL CHECK (VehicleType IN (
                      'Car without AC', 'Car with AC', 'Premium', 'Bike')),
    IsAvailable   BIT          DEFAULT 1,
    Rating        DECIMAL(3,2) DEFAULT 5.00,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);
GO

CREATE TABLE ride_requests (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    passenger_username VARCHAR(50)   NOT NULL,
    pickup_location    VARCHAR(255)  NOT NULL,
    dropoff_location   VARCHAR(255)  NOT NULL,
    vehicle_type       VARCHAR(30)   NOT NULL,
    fare               DECIMAL(10,2),
    status             VARCHAR(20)   DEFAULT 'PENDING'
                           CHECK (status IN ('PENDING', 'ACCEPTED', 'COMPLETED')),
    driver_username    VARCHAR(50),
    request_time       DATETIME      DEFAULT GETDATE(),
    FOREIGN KEY (passenger_username) REFERENCES Users(Username),
    FOREIGN KEY (driver_username)    REFERENCES Users(Username)
);
GO

CREATE TABLE RideFeedback (
    feedback_id        INT IDENTITY(1,1) PRIMARY KEY,
    ride_id            INT          NOT NULL,
    passenger_username VARCHAR(100) NOT NULL,
    driver_username    VARCHAR(100) NOT NULL,
    rating             INT          CHECK (rating >= 1 AND rating <= 5),
    comments           VARCHAR(500),
    feedback_date      DATETIME     DEFAULT GETDATE(),
    FOREIGN KEY (ride_id) REFERENCES ride_requests(id) ON DELETE CASCADE
);
GO

CREATE VIEW PassengerDetails AS
SELECT
    p.PassengerID,
    u.Username,
    u.Email,
    u.Phone,
    u.CreatedAt,
    COUNT(r.id) AS TotalRides
FROM Passengers p
JOIN Users u ON p.UserID = u.UserID
LEFT JOIN ride_requests r ON r.passenger_username = u.Username
GROUP BY p.PassengerID, u.Username, u.Email, u.Phone, u.CreatedAt;
GO

CREATE VIEW DriverDetails AS
SELECT
    d.DriverID,
    u.Username,
    u.Email,
    u.Phone,
    d.LicenseNumber,
    d.VehicleType,
    d.IsAvailable,
    d.Rating,
    COUNT(r.id) AS TotalRidesCompleted
FROM Drivers d
JOIN Users u ON d.UserID = u.UserID
LEFT JOIN ride_requests r ON r.driver_username = u.Username
    AND r.status = 'COMPLETED'
GROUP BY d.DriverID, u.Username, u.Email, u.Phone,
         d.LicenseNumber, d.VehicleType, d.IsAvailable, d.Rating;
GO

USE RideFlowDB;
SELECT * FROM Users;
SELECT * FROM Passengers;
SELECT * FROM Drivers;
SELECT * FROM ride_requests;
SELECT * FROM RideFeedback;