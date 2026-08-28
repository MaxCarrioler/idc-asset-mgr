package com.dcasset.security;

import com.dcasset.admin.service.UserPermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Bearer JWT 过滤器：解析令牌、装配当前用户与权限
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserPermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtService.parse(token, JwtService.TYPE_ACCESS);
                CurrentUser user = permissionService.loadActiveUser(username);
                if (user != null) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user, token, buildAuthorities(user)));
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /** 权限点 + 角色码 + ROLE_{roleId}（兼容原业务代码的 ROLE_1 判断） */
    private List<GrantedAuthority> buildAuthorities(CurrentUser user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : user.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        for (CurrentUser.RoleInfo role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleId()));
            if (role.getRoleCode() != null) {
                authorities.add(new SimpleGrantedAuthority(role.getRoleCode()));
            }
        }
        return authorities;
    }
}
