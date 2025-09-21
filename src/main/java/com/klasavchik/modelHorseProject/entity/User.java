package com.klasavchik.modelHorseProject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

@Entity
@Table(name = "users")

// таблица с основными данными для генерации
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    //private Status status; // статус акаунта, забанен, подтверждён, не подтверждён

    @Builder.Default
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserRole> userRoles = new ArrayList<>();

    private LocalDate createdAt;
    private boolean isOnline;
    private LocalDateTime lastLoginAt; //последнее время онлайна

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Model> modelsOwn = new ArrayList<>();

    @OneToMany(mappedBy = "master")
    private Set<Model> modelsSetMade = new HashSet<>();

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
