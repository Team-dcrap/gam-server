package com.gam.api.domain.user.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Builder
@Getter
public class GamUserDetails implements UserDetails {

    // TODO: User 객체 이전 이후 삭제 필요
    private final Long id;
    private final User user;
    private final String username;
    private final String authUserId;
    private List<GrantedAuthority> authorities;

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public User getUser() {
        return user;
    }

    // TODO: User 객체 이전 이후 삭제 필요
    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return authUserId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}