package com.antdigital.auth.model.entity;

import java.time.LocalDateTime;

/**
 * 用户账号数据对象，对应 user_account 表。
 */
public class UserDO {

    /** 主键ID */
    private Long id;

    /** 用户名 */
    private String userName;

    /** 密码加盐哈希（BCrypt） */
    private String passwordHash;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 连续登录失败次数 */
    private Integer failCount;

    /** 锁定截止时间，NULL 表示未锁定 */
    private LocalDateTime lockUntil;

    /** 创建时间 */
    private LocalDateTime gmtCreate;

    /** 更新时间 */
    private LocalDateTime gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public String toString() {
        return "UserDO{id=" + id
                + ", userName='" + userName + '\''
                + ", status=" + status
                + ", failCount=" + failCount
                + ", lockUntil=" + lockUntil
                + '}';
    }
}
