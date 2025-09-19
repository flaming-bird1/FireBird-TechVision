-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_learning_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE personal_learning_platform;
SHOW VARIABLES LIKE 'character_set_database';

-- 创建邮箱表
-- 从1开始编号
ALTER TABLE email_verification
    AUTO_INCREMENT = 1;
DROP TABLE IF EXISTS email_verification;
CREATE TABLE IF NOT EXISTS email_verification
(
    email_id          INT PRIMARY KEY AUTO_INCREMENT COMMENT '邮箱验证记录唯一标识',
    email             VARCHAR(100) NOT NULL UNIQUE COMMENT '待验证邮箱（唯一约束）',
    verification_code VARCHAR(6)   NOT NULL COMMENT '6位数字验证码',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '验证码创建时间',
    expires_at        DATETIME     NOT NULL COMMENT '验证码过期时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '邮箱验证码记录表';

-- 用户表(存储个人信息)
ALTER TABLE users
    AUTO_INCREMENT = 1;
DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users
(
    user_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50) NOT NULL COMMENT '用户名',
    password    VARCHAR(20) NOT NULL COMMENT '密码',
    avatar_url  VARCHAR(255) COMMENT '头像链接',
    role        VARCHAR(20) DEFAULT 'user' COMMENT '角色: user, admin',
    bio         TEXT COMMENT '个人简介',
    email       VARCHAR(100) COMMENT '邮箱',
    blog_url    VARCHAR(255) COMMENT '博客链接',
    gitcode_url VARCHAR(255) COMMENT 'GitCode 个人主页链接',
    birthday    DATE COMMENT '出生日期（格式：YYYY-MM-DD）',
    gender      CHAR(2) COMMENT '性别',
    address     VARCHAR(100) COMMENT '用户住址',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


-- 用户空间配额表
ALTER TABLE user_quota
    AUTO_INCREMENT = 1;
DROP TABLE IF EXISTS user_quota;
CREATE TABLE user_quota
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT UNIQUE NOT NULL COMMENT '用户ID',
    total_quota BIGINT DEFAULT 5368709120 COMMENT '总空间（字节），默认5G',
    used_quota  BIGINT DEFAULT 0 COMMENT '已用空间（字节）',
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- 文件信息表
ALTER TABLE file_info
    AUTO_INCREMENT = 1;
DROP TABLE IF EXISTS file_info;
CREATE TABLE file_info
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    original_filename VARCHAR(200) NOT NULL COMMENT '原始文件名',
    newFilename       VARCHAR(200) NOT NULL COMMENT '唯一文件名',
    path              VARCHAR(500) NOT NULL COMMENT '服务器存储路径',
    file_size         BIGINT       NOT NULL COMMENT '文件大小（字节）',
    user_id           INT          NOT NULL COMMENT '上传者ID',
    folder_id         INT      DEFAULT 0 COMMENT '所属文件夹',
    upload_time       DATETIME DEFAULT CURRENT_TIMESTAMP,
    download_count    INT      DEFAULT 0 COMMENT '下载次数',
    remark            VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- 单词表
ALTER TABLE words
    AUTO_INCREMENT = 1;
DROP TABLE IF EXISTS words;
CREATE TABLE words
(
    id          INT AUTO_INCREMENT PRIMARY KEY COMMENT '唯一标识',
    word        VARCHAR(100) NOT NULL COMMENT '英文单词',
    translation TEXT         NOT NULL COMMENT '中文翻译',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='单词表';