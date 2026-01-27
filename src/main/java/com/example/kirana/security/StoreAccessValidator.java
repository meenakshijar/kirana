package com.example.kirana.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class StoreAccessValidator {

    public void validateStoreAccess(String requestedStoreId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null) {
            throw new RuntimeException("Unauthorized");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return; // super admin can access everything
        }

        String tokenStoreId = (String) auth.getDetails(); // storeId saved in JwtFilter

        if (tokenStoreId == null || tokenStoreId.isBlank()) {
            throw new RuntimeException("Forbidden: storeId missing in token");
        }

        if (requestedStoreId == null || requestedStoreId.isBlank()) {
            throw new RuntimeException("storeId is required");
        }

        if (!tokenStoreId.equals(requestedStoreId)) {
            throw new RuntimeException("Forbidden: Cannot access another store");
        }
    }
}
