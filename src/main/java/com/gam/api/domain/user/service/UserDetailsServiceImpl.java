package com.gam.api.domain.user.service;

import com.gam.api.common.message.ExceptionMessage;
import com.gam.api.domain.user.repository.UserRepository;
import com.gam.api.domain.user.entity.GamUserDetails;
import com.gam.api.domain.user.repository.AuthProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        val user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.TOKEN_USER_EXCEPTION.getMessage()));

        val userRole = user.getRole();
        val userStatus = user.getUserStatus();

        val authUser = authProviderRepository.searchAuthProviderByUser(user)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.TOKEN_USER_EXCEPTION.getMessage()));

        val authUserId = String.valueOf(authUser.getId());

        authorities.add(new SimpleGrantedAuthority("ROLE_"+userRole.toString()));
        authorities.add(new SimpleGrantedAuthority(userStatus.toString()));

        return GamUserDetails.builder()
                .id(user.getId())
                .user(user)
                .authUserId(authUserId)
                .username(user.getUserName())
                .authorities(authorities)
                .build();
    }
}