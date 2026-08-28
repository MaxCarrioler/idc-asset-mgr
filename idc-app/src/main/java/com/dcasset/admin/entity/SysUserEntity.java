package com.dcasset.admin.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_user")
public class SysUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_user_seq")
    @SequenceGenerator(name = "sys_user_seq", sequenceName = "sys_user_user_id_seq", allocationSize = 1)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 64)
    private String nickname;

    @Column(length = 64)
    private String name;

    @Column(length = 32)
    private String phone;

    @Column(length = 128)
    private String email;

    /** '0' 正常；'9' 待审批 */
    @Column(name = "lock_flag", length = 1)
    private String lockFlag;

    /** '0' 正常；'1' 已删除（软删除） */
    @Column(name = "del_flag", length = 1)
    private String delFlag;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (lockFlag == null) {
            lockFlag = "0";
        }
        if (delFlag == null) {
            delFlag = "0";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}
