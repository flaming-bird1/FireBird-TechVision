-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_learning_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE personal_learning_platform;
SHOW VARIABLES LIKE 'character_set_database';

-- 创建邮箱表
DROP TABLE IF EXISTS email_verification;
CREATE TABLE IF NOT EXISTS email_verification
(
    email_id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '邮箱验证记录唯一标识',
    email             VARCHAR(100) NOT NULL UNIQUE COMMENT '待验证邮箱（唯一约束）',
    verification_code VARCHAR(6)   NOT NULL COMMENT '6位数字验证码',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '验证码创建时间',
    expires_at        DATETIME NOT NULL COMMENT '验证码过期时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '邮箱验证码记录表';

-- 用户表(存储个人信息)
DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users
(
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL,
    password    VARCHAR(20) NOT NULL,
    avatar_url  VARCHAR(255),
    title       VARCHAR(100) COMMENT '头衔/职位',
    bio         TEXT COMMENT '个人简介',
    email       VARCHAR(100),
    blog_url    VARCHAR(255),
    gitcode_url VARCHAR(255) COMMENT 'GitCode 个人主页链接',
    birthday    DATE COMMENT '出生日期（格式：YYYY-MM-DD）',
    gender      CHAR(2) COMMENT '性别',
    address     VARCHAR(100) COMMENT '用户住址',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;