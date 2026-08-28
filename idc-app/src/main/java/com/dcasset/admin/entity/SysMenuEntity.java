package com.dcasset.admin.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_menu")
public class SysMenuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_menu_seq")
    @SequenceGenerator(name = "sys_menu_seq", sequenceName = "sys_menu_menu_id_seq", allocationSize = 1)
    @Column(name = "menu_id")
    private Long menuId;

    @Column(nullable = false, length = 64)
    private String name;

    /** 权限点，如 accessory:transfer:view */
    @Column(length = 128)
    private String permission;

    @Column(length = 255)
    private String path;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(length = 64)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder;

    /** '0' 菜单；'1' 按钮/权限点 */
    @Column(name = "menu_type", length = 1)
    private String menuType;

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
        if (delFlag == null) {
            delFlag = "0";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}
