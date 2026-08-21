package com.example.SSO_project.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache-friendly snapshot of a user for authentication lookups.
 * Kept deliberately simple: non-final class + no-args constructor so the
 * Redis JSON serializer (default typing = NON_FINAL) can round-trip it.
 * Password may be null for OAuth-only accounts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedUser {
    private String username;
    private String password;
    private String role;
}
