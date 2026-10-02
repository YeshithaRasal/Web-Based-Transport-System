-- ===================================================================
-- Swift Go Lanka - Web-Based Transport Management System Database Dump
-- Database Name: swift_go_lanka
-- Compatible with MySQL 8.0+ / MySQL 9.0+
-- ===================================================================

CREATE DATABASE IF NOT EXISTS `swift_go_lanka` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `swift_go_lanka`;

-- Disable foreign key checks for clean recreation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `reports`;
DROP TABLE IF EXISTS `complaints`;
DROP TABLE IF EXISTS `feedback`;
DROP TABLE IF EXISTS `driver_commissions`;
DROP TABLE IF EXISTS `refunds`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `trips`;
DROP TABLE IF EXISTS `driver_assignments`;
DROP TABLE IF EXISTS `ride_bookings`;
DROP TABLE IF EXISTS `driver_profiles`;
DROP TABLE IF EXISTS `users`;

-- 1. Users Table
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(50) DEFAULT NULL,
  `role` VARCHAR(50) NOT NULL,
  `account_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Driver Profiles Table
CREATE TABLE `driver_profiles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL UNIQUE,
  `licence_number` VARCHAR(100) NOT NULL,
  `vehicle_number` VARCHAR(50) NOT NULL,
  `vehicle_type` VARCHAR(50) NOT NULL,
  `vehicle_model` VARCHAR(100) DEFAULT NULL,
  `vehicle_colour` VARCHAR(50) DEFAULT NULL,
  `verification_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
  `availability_status` VARCHAR(50) NOT NULL DEFAULT 'OFFLINE',
  `current_lat` DOUBLE DEFAULT NULL,
  `current_lng` DOUBLE DEFAULT NULL,
  `rating_avg` DOUBLE DEFAULT 5.0,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_driver_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Ride Bookings Table
CREATE TABLE `ride_bookings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `passenger_id` BIGINT NOT NULL,
  `pickup_location` VARCHAR(500) NOT NULL,
  `pickup_lat` DOUBLE DEFAULT NULL,
  `pickup_lng` DOUBLE DEFAULT NULL,
  `destination_location` VARCHAR(500) NOT NULL,
  `destination_lat` DOUBLE DEFAULT NULL,
  `destination_lng` DOUBLE DEFAULT NULL,
  `vehicle_type` VARCHAR(50) NOT NULL,
  `estimated_distance_km` DOUBLE DEFAULT NULL,
  `estimated_duration_min` INT DEFAULT NULL,
  `estimated_fare` DOUBLE DEFAULT NULL,
  `booking_status` VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_booking_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Driver Assignments Table
CREATE TABLE `driver_assignments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL,
  `driver_id` BIGINT NOT NULL,
  `assignment_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `responded_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_assign_booking` FOREIGN KEY (`booking_id`) REFERENCES `ride_bookings` (`id`),
  CONSTRAINT `fk_assign_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Trips Table
CREATE TABLE `trips` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `booking_id` BIGINT NOT NULL UNIQUE,
  `passenger_id` BIGINT NOT NULL,
  `driver_id` BIGINT NOT NULL,
  `pickup_location` VARCHAR(500) NOT NULL,
  `destination_location` VARCHAR(500) NOT NULL,
  `start_time` DATETIME DEFAULT NULL,
  `end_time` DATETIME DEFAULT NULL,
  `distance_km` DOUBLE DEFAULT NULL,
  `duration_min` INT DEFAULT NULL,
  `final_fare` DOUBLE DEFAULT NULL,
  `trip_status` VARCHAR(50) NOT NULL DEFAULT 'DRIVER_ARRIVING',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_trip_booking` FOREIGN KEY (`booking_id`) REFERENCES `ride_bookings` (`id`),
  CONSTRAINT `fk_trip_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_trip_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Payments Table
CREATE TABLE `payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL UNIQUE,
  `passenger_id` BIGINT NOT NULL,
  `amount` DOUBLE NOT NULL,
  `payment_method` VARCHAR(50) NOT NULL,
  `payment_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `transaction_reference` VARCHAR(100) NOT NULL UNIQUE,
  `paid_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_pay_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`),
  CONSTRAINT `fk_pay_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Refunds Table
CREATE TABLE `refunds` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_id` BIGINT NOT NULL,
  `passenger_id` BIGINT NOT NULL,
  `amount` DOUBLE NOT NULL,
  `reason` TEXT NOT NULL,
  `refund_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `requested_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` DATETIME DEFAULT NULL,
  `remarks` TEXT DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_ref_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`),
  CONSTRAINT `fk_ref_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Driver Commissions Table
CREATE TABLE `driver_commissions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL UNIQUE,
  `driver_id` BIGINT NOT NULL,
  `trip_fare` DOUBLE NOT NULL,
  `commission_rate_pct` DOUBLE NOT NULL,
  `driver_earnings` DOUBLE NOT NULL,
  `platform_fee` DOUBLE NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_comm_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`),
  CONSTRAINT `fk_comm_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Feedback Table
CREATE TABLE `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `passenger_id` BIGINT NOT NULL,
  `driver_id` BIGINT NOT NULL,
  `rating` INT NOT NULL,
  `comment` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_feed_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`),
  CONSTRAINT `fk_feed_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_feed_driver` FOREIGN KEY (`driver_id`) REFERENCES `driver_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Complaints Table
CREATE TABLE `complaints` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `trip_id` BIGINT DEFAULT NULL,
  `subject` VARCHAR(255) NOT NULL,
  `category` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `complaint_status` VARCHAR(50) NOT NULL DEFAULT 'OPEN',
  `resolution_notes` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_comp_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_comp_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Reports Table
CREATE TABLE `reports` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `report_type` VARCHAR(100) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `generated_by_user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_rep_user` FOREIGN KEY (`generated_by_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Notifications Table
CREATE TABLE `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `message` TEXT NOT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- ===================================================================
-- SEED DATA (All passwords BCrypt hashed for 'Password123')
-- Hash: $2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2
-- ===================================================================

INSERT INTO `users` (`id`, `full_name`, `email`, `password`, `phone`, `role`, `account_status`, `is_deleted`) VALUES
(1, 'System Administrator', 'admin@swiftgolanka.lk', '$2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2', '+94 77 111 2222', 'ADMIN', 'ACTIVE', 0),
(2, 'Operations Lead', 'ops@swiftgolanka.lk', '$2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2', '+94 77 222 3333', 'OPERATIONS_MANAGER', 'ACTIVE', 0),
(3, 'Support Agent', 'support@swiftgolanka.lk', '$2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2', '+94 77 333 4444', 'CUSTOMER_SUPPORT', 'ACTIVE', 0),
(4, 'Finance Officer', 'finance@swiftgolanka.lk', '$2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2', '+94 77 444 5555', 'FINANCE_OFFICER', 'ACTIVE', 0),
(5, 'Kamal Perera', 'passenger@swiftgolanka.lk', '$2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2', '+94 71 555 6666', 'PASSENGER', 'ACTIVE', 0),
(6, 'Sunil Shantha', 'driver@swiftgolanka.lk', '$2a$10$wE99q4l8hS05qN97hW/lQ.uPZqjU5q0ZfW/a/k6OQW3x0d41h.bK2', '+94 76 888 9999', 'DRIVER', 'ACTIVE', 0);

INSERT INTO `driver_profiles` (`id`, `user_id`, `licence_number`, `vehicle_number`, `vehicle_type`, `vehicle_model`, `vehicle_colour`, `verification_status`, `availability_status`, `current_lat`, `current_lng`, `rating_avg`) VALUES
(1, 6, 'DL-98765432', 'CAB-1234', 'CAR', 'Toyota Prius', 'Silver', 'VERIFIED', 'AVAILABLE', 6.9271, 79.8612, 4.8);

INSERT INTO `ride_bookings` (`id`, `passenger_id`, `pickup_location`, `pickup_lat`, `pickup_lng`, `destination_location`, `destination_lat`, `destination_lng`, `vehicle_type`, `estimated_distance_km`, `estimated_duration_min`, `estimated_fare`, `booking_status`) VALUES
(1, 5, 'Colombo Fort Railway Station', 6.9344, 79.8505, 'Galle Face Green, Colombo 03', 6.9271, 79.8447, 'CAR', 3.5, 12, 500.00, 'COMPLETED');

INSERT INTO `driver_assignments` (`id`, `booking_id`, `driver_id`, `assignment_status`, `assigned_at`, `responded_at`) VALUES
(1, 1, 1, 'ACCEPTED', NOW(), NOW());

INSERT INTO `trips` (`id`, `booking_id`, `passenger_id`, `driver_id`, `pickup_location`, `destination_location`, `start_time`, `end_time`, `distance_km`, `duration_min`, `final_fare`, `trip_status`) VALUES
(1, 1, 5, 1, 'Colombo Fort Railway Station', 'Galle Face Green, Colombo 03', NOW(), NOW(), 3.5, 13, 500.00, 'COMPLETED');

INSERT INTO `payments` (`id`, `trip_id`, `passenger_id`, `amount`, `payment_method`, `payment_status`, `transaction_reference`, `paid_at`) VALUES
(1, 1, 5, 500.00, 'CREDIT_CARD', 'PAID', 'TXN-SWIFT-982104', NOW());

INSERT INTO `driver_commissions` (`id`, `trip_id`, `driver_id`, `trip_fare`, `commission_rate_pct`, `driver_earnings`, `platform_fee`) VALUES
(1, 1, 1, 500.00, 15.00, 425.00, 75.00);

INSERT INTO `feedback` (`id`, `trip_id`, `passenger_id`, `driver_id`, `rating`, `comment`, `is_deleted`) VALUES
(1, 1, 5, 1, 5, 'Excellent driver! Prompt pickup and clean vehicle.', 0);
