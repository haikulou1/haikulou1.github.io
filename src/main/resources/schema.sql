-- 用户表
CREATE TABLE IF NOT EXISTS user_account (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_name       VARCHAR(50)  NOT NULL COMMENT '用户名',
    password_hash   VARCHAR(100) NOT NULL COMMENT '密码加盐哈希（BCrypt）',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    fail_count      INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    lock_until      DATETIME     NULL DEFAULT NULL COMMENT '锁定截止时间，NULL表示未锁定',
    gmt_create      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';
