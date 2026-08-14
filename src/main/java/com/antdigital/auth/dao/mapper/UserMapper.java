package com.antdigital.auth.dao.mapper;

import com.antdigital.auth.model.entity.UserDO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 用户账号数据访问接口。
 */
public interface UserMapper {

    /**
     * 根据用户名查询用户。
     *
     * @param userName 用户名
     * @return 用户数据对象，不存在时返回 null
     */
    UserDO getByUserName(@Param("userName") String userName);

    /**
     * 更新账号失败次数与锁定截止时间。
     *
     * @param id        用户ID
     * @param failCount 失败次数
     * @param lockUntil 锁定截止时间，NULL 表示解锁
     * @return 影响行数
     */
    int updateFailStatus(@Param("id") Long id,
                         @Param("failCount") Integer failCount,
                         @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * 重置账号失败状态（登录成功后调用）。
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int resetFailStatus(@Param("id") Long id);
}
