package com.uni.ethesis.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.Collection;
import java.util.HashSet;

public class KeycloakGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        var mappedAuthorities = new HashSet<GrantedAuthority>();

        authorities.forEach(authority -> {

//            if (authority instanceof SimpleGrantedAuthority) {
//                mappedAuthorities.add(authority);
//            }

            if (authority instanceof OidcUserAuthority) {
                addRolesFromUserInfo(mappedAuthorities, (OidcUserAuthority) authority);
            }

        });

        return mappedAuthorities;
    }

    private void addRolesFromUserInfo(HashSet<GrantedAuthority> mappedAuthorities, OidcUserAuthority oidcUserAuthority) {

        var userInfo = oidcUserAuthority.getUserInfo();

        var roleInfo = userInfo.getClaimAsMap("realm_access");
        if (roleInfo == null || roleInfo.isEmpty()) {
            return;
        }

        var roles = (Collection<String>) roleInfo.get("roles");
        roles.forEach(roleName -> {
            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
        });
    }
}
