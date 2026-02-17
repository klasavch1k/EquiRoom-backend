package com.klasavchik.modelHorseProject.dto.user;

import com.klasavchik.modelHorseProject.entity.user.Role;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class UserAuthDto {
    private Long id;
    private String email;
    private String password;

    @Builder.Default
    private Set<Role> userRoles = new HashSet<>();
}
