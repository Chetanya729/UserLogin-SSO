package com.example.SSO_project.Service;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.domain.CachedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;

    @Cacheable(cacheNames = "users", key = "#username", unless = "#result == null")
    public CachedUser findUser(String username) {
        return userRepository.findByUsername(username)
                .map(u -> new CachedUser(
                        u.getUsername(),
                        u.getPassword(),
                        u.getRole().name()))
                .orElse(null);
    }
    @CacheEvict(cacheNames = "users", key = "#username")
    public void evictUser(String username) {
    }
}
