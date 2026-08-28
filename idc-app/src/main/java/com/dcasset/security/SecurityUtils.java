package com.dcasset.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具，替代原 PigX SecurityUtils
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CurrentUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser user) {
            return user;
        }
        return null;
    }

    public static String getUsername() {
        CurrentUser user = getUser();
        return user == null ? null : user.getUsername();
    }
}
