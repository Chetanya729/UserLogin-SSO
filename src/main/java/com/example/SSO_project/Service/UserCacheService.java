package com.example.SSO_project.Service;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.domain.CachedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;

    /**
     * Cached user lookup. Lives in its own bean (not UserDetailServiceImpl)
     * so calls from other beans go through the caching proxy — a @Cacheable
     * method called from within its own class bypasses the cache entirely.
     * Returns null when the user doesn't exist; "unless" keeps nulls out of Redis.
     */
    @Cacheable(cacheNames = "users", key = "#username", unless = "#result == null")
    public CachedUser findUser(String username) {
        return userRepository.findByUsername(username)
                .map(u -> new CachedUser(
                        u.getUsername(),
                        u.getPassword(),
                        u.getRole().name()))
                .orElse(null);
    }
}
