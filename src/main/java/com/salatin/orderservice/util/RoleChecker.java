package com.salatin.orderservice.util;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class RoleChecker {

    private RoleChecker() {}

    public static boolean hasRoleManager(JwtAuthenticationToken authenticationToken) {
        return authenticationToken.getAuthorities().stream()
                .anyMatch(a -> "ROLE_manager".equals(a.getAuthority()));
    }
}
