-- ============================================================
-- Manga Reader Database Schema
-- Run with: mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS manga_reader_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE manga_reader_db;

-- -------------------------------------------------------
-- Users (auth-ready: single local user by default)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  DEFAULT NULL,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Default local user (no password)
INSERT IGNORE INTO users (id, username) VALUES (1, 'local');

-- -------------------------------------------------------
-- Mangas (one row per manga folder)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS mangas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    folder_path VARCHAR(1000) NOT NULL,
    cover_path  VARCHAR(1000) DEFAULT NULL,
    is_favorite BOOLEAN       DEFAULT FALSE,
    added_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_folder (folder_path(500))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- Chapters (one row per PDF file)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS chapters (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    manga_id       INT           NOT NULL,
    chapter_number FLOAT         NOT NULL,
    title          VARCHAR(255)  DEFAULT NULL,
    file_path      VARCHAR(1000) NOT NULL,
    page_count     INT           DEFAULT 0,
    UNIQUE KEY uq_file (file_path(500)),
    FOREIGN KEY (manga_id) REFERENCES mangas(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -------------------------------------------------------
-- Reading Progress (upserted on every page turn)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS reading_progress (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT       NOT NULL,
    manga_id    INT       NOT NULL,
    chapter_id  INT       NOT NULL,
    page        INT       NOT NULL DEFAULT 0,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_manga (user_id, manga_id),
    FOREIGN KEY (user_id)   REFERENCES users(id),
    FOREIGN KEY (manga_id)  REFERENCES mangas(id)  ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
