-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_learning_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE personal_learning_platform;

-- 用户表(存储个人信息)
CREATE TABLE IF NOT EXISTS users
(
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50) NOT NULL,
    avatar_url VARCHAR(255),
    title      VARCHAR(100) COMMENT '头衔/职位',
    bio        TEXT COMMENT '个人简介',
    email      VARCHAR(100),
    github_url VARCHAR(255),
    blog_url   VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
ALTER TABLE users
    ADD COLUMN password VARCHAR(20) NOT NULL