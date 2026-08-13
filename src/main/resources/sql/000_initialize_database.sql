-- 创建数据库
CREATE DATABASE IF NOT EXISTS spring_boot_template
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
-- 查询数据库
SHOW databases;

-- 创建专用数据库账号
CREATE USER IF NOT EXISTS 'spring_boot_app'@'127.0.0.1'
IDENTIFIED by 'hua12345';

-- 赋予账号权限
GRANT ALL PRIVILEGES
ON spring_boot_template.*
TO 'spring_boot_app'@'127.0.0.1';

-- 检查权限
SHOW GRANTS FOR 'spring_boot_app'@'127.0.0.1';

