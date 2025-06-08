package com.uni.ethesis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthorityConverter.class);

    /**
     * Convert the source object of type {@code S} to target type {@code T}.
     *
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
     * @throws IllegalArgumentException if the source cannot be converted to the desired target type
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {

        Map<String, Object> realm = (Map<String, Object>) source.getClaims().get("realm_access");
        if (realm == null || realm.isEmpty()) {
            return new ArrayList();
        }
        log.debug("source.getClaims(): " + source.getClaims());
        Collection<GrantedAuthority> authorities = ((List<String>) realm.get("roles"))
                .stream()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
        log.debug("authorities: " + authorities);
        return authorities;
    }
}
