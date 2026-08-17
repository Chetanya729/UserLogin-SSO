package com.example.SSO_project.Service;

import com.example.SSO_project.Repository.UserRepository;
import com.example.SSO_project.domain.PROVIDER;
import com.example.SSO_project.domain.ROLE;
import com.example.SSO_project.domain.UserRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email;
        String username;
        PROVIDER provider;
        String nameAttributeKey;

        switch (registrationId) {
            case "github" -> {
                Object id = attributes.get("id");
                String login = (String) attributes.get("login");
                String fullName = (String) attributes.get("name");
                email = (String) attributes.get("email");
                if (email == null) {
                    email = id + "@users.noreply.github.com";
                }
                username = fullName != null ? fullName : (login != null ? login : email);
                provider = PROVIDER.GITHUB;
                nameAttributeKey = "id";
            }
            case "facebook" -> {
                Object id = attributes.get("id");
                String fullName = (String) attributes.get("name");
                email = (String) attributes.get("email");
                if (email == null) {
                    email = id + "@facebook.local";
                }
                username = fullName != null ? fullName : email;
                provider = PROVIDER.FACEBOOK;
                nameAttributeKey = "id";
            }
            default -> throw new OAuth2AuthenticationException(
                    "Unsupported OAuth2 provider: " + registrationId);
        }

        final String finalEmail = email;
        final String finalUsername = username;
        final PROVIDER finalProvider = provider;

        UserRegister user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> userRepository.save(
                        UserRegister.builder()
                                .username(finalUsername)
                                .email(finalEmail)
                                .password(null)
                                .role(ROLE.USER)
                                .provider(finalProvider)
                                .build()
                ));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes,
                nameAttributeKey
        );
    }
}
