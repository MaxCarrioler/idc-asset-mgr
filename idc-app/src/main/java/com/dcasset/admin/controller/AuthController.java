package com.dcasset.admin.controller;

import com.dcasset.admin.service.UserPermissionService;
import com.dcasset.common.result.Result;
import com.dcasset.security.CurrentUser;
import com.dcasset.security.JwtService;
import com.dcasset.security.PasswordDecoderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证端点：保持 OAuth 表单契约（前端零改动）
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String CLIENT_ID = "test";
    private static final String CLIENT_SECRET = "test";

    private final JwtService jwtService;
    private final UserPermissionService permissionService;

    @Value("${app.security.encode-key}")
    private String encodeKey;

    /**
     * POST /auth/oauth2/token
     * grant_type=password：username + AES 加密 password
     * grant_type=refresh_token：refresh_token
     */
    @PostMapping(value = "/oauth2/token", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Map<String, Object>> token(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "refresh_token", required = false) String refreshToken) {
        if (!verifyClient(authorization)) {
            return error("invalid_client", "客户端认证失败");
        }
        switch (grantType) {
            case "password" -> {
                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                    return error("invalid_request", "用户名或密码缺失");
                }
                String plainPassword;
                try {
                    plainPassword = PasswordDecoderUtil.decrypt(password, encodeKey);
                } catch (Exception e) {
                    return error("invalid_grant", "密码解密失败");
                }
                CurrentUser user = permissionService.loadActiveUser(username.trim());
                if (user == null) {
                    return error("invalid_grant", "用户不存在、密码错误或账号待审批");
                }
                if (!permissionService.verifyPassword(user.getUsername(), plainPassword)) {
                    return error("invalid_grant", "用户不存在、密码错误或账号待审批");
                }
                return ResponseEntity.ok(issueTokens(user.getUsername()));
            }
            case "refresh_token" -> {
                if (refreshToken == null || refreshToken.isBlank()) {
                    return error("invalid_request", "缺少 refresh_token");
                }
                String subject;
                try {
                    subject = jwtService.parse(refreshToken, JwtService.TYPE_REFRESH);
                } catch (Exception e) {
                    return error("invalid_grant", "刷新令牌无效或已过期");
                }
                if (permissionService.loadActiveUser(subject) == null) {
                    return error("invalid_grant", "用户不存在或账号不可用");
                }
                return ResponseEntity.ok(issueTokens(subject));
            }
            default -> {
                return error("unsupported_grant_type", "不支持的 grant_type: " + grantType);
            }
        }
    }

    /** 无状态登出：端点保持存在，客户端清除令牌即登出 */
    @DeleteMapping("/token/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }

    private Map<String, Object> issueTokens(String username) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("access_token", jwtService.createAccessToken(username));
        body.put("token_type", "bearer");
        body.put("refresh_token", jwtService.createRefreshToken(username));
        body.put("expires_in", jwtService.getAccessTokenValidity());
        body.put("scope", "server");
        return body;
    }

    private boolean verifyClient(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(authorization.substring(6)),
                    StandardCharsets.UTF_8);
            return (CLIENT_ID + ":" + CLIENT_SECRET).equals(decoded);
        } catch (Exception e) {
            return false;
        }
    }

    private ResponseEntity<Map<String, Object>> error(String error, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("error_description", description);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}
