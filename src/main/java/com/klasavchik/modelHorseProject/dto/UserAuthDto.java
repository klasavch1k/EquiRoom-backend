package com.klasavchik.modelHorseProject.dto;

import com.klasavchik.modelHorseProject.entity.Role;
import com.klasavchik.modelHorseProject.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserAuthDto {
    private String email;
    private String password;

    @Builder.Default
    private Set<Role> userRoles = new HashSet<>();
}
