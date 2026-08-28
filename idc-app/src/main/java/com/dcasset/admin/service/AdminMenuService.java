package com.dcasset.admin.service;

import com.dcasset.admin.entity.SysMenuEntity;
import com.dcasset.admin.entity.SysRoleMenuEntity;
import com.dcasset.admin.repository.SysMenuRepository;
import com.dcasset.admin.repository.SysRoleMenuRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户中心：菜单树与角色已授权菜单
 */
@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final SysMenuRepository menuRepository;
    private final SysRoleMenuRepository roleMenuRepository;

    /** 菜单树（[{id, name, children}]，与前端 el-tree 约定一致） */
    public List<MenuNodeVO> tree() {
        List<SysMenuEntity> menus = menuRepository.findByDelFlagOrderBySortOrderAsc("0");
        Map<Long, MenuNodeVO> nodeMap = new LinkedHashMap<>();
        for (SysMenuEntity menu : menus) {
            MenuNodeVO node = new MenuNodeVO();
            node.setId(menu.getMenuId());
            node.setName(menu.getName());
            node.setChildren(new ArrayList<>());
            nodeMap.put(menu.getMenuId(), node);
        }
        List<MenuNodeVO> roots = new ArrayList<>();
        for (SysMenuEntity menu : menus) {
            MenuNodeVO node = nodeMap.get(menu.getMenuId());
            MenuNodeVO parent = menu.getParentId() == null ? null : nodeMap.get(menu.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    /** 角色已授权的菜单 ID 列表 */
    public List<Long> roleMenuIds(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId).stream()
                .map(SysRoleMenuEntity::getMenuId)
                .sorted()
                .collect(Collectors.toList());
    }

    @Data
    public static class MenuNodeVO {
        private Long id;
        private String name;
        private List<MenuNodeVO> children;
    }
}
