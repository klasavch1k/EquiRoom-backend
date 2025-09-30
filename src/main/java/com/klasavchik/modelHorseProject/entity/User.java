package com.klasavchik.modelHorseProject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"modelsOwn","modelsSetMade","userRoles"})
@EqualsAndHashCode(exclude = {"modelsOwn","modelsSetMade","userRoles"})

@Entity
@Table(name = "users")

// таблица с основными данными для генерации
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();
    private LocalDate createdAt;
    private boolean isOnline;
    private LocalDateTime lastLoginAt; //последнее время онлайна

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private Set<Model> modelsOwn = new HashSet<>();

    public void addRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(this);
        userRoles.add(userRole);
    }
    public void removeRole(Role role) {
        userRoles.removeIf(userRole -> userRole.getRole().equals(role));
    }
}
