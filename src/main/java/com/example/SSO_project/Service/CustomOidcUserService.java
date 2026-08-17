package com.example.SSO_project.Service;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.domain.PROVIDER;
import com.example.SSO_project.domain.ROLE;
import com.example.SSO_project.domain.UserRegister;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(@NonNull OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(request);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        UserRegister user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        UserRegister.builder()
                                .username(name != null ? name : email)
                                .email(email)
                                .password(null)
                                .role(ROLE.USER)
                                .provider(PROVIDER.GOOGLE)
                                .build()
                ));

        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"
        );
    }
}
