-- Fix the movie_times table to use theater_id instead of movieId
-- Run this SQL script in your MySQL database before starting the backend

USE bookshow;

-- Drop the existing movie_times table to recreate it with correct structure
DROP TABLE IF EXISTS movie_times;

-- The table will be automatically recreated by Hibernate with the correct structure
-- when you restart the Spring Boot application

-- Optional: If you want to preserve existing data, use this approach instead:
-- 1. Export the data first
-- CREATE TABLE movie_times_backup AS SELECT * FROM movie_times;
-- 
-- 2. Drop and let Hibernate recreate
-- DROP TABLE movie_times;
-- 
-- 3. After backend starts, manually re-insert the data if needed
