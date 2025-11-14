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
@ToString(exclude = {"userRoles", "following", "followers"})
@EqualsAndHashCode(exclude = {"userRoles", "following", "followers"})
@Entity
@Table(name = "users")

// таблица с основными данными для генерации
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String phoneNumber;
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();
    private LocalDate createdAt;
    private LocalDateTime updatedAt;
    private boolean isOnline;
    private LocalDateTime lastLoginAt; //последнее время онлайна

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "settings_id")
    Settings settings;
    @Builder.Default
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Follow> following = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "followed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Follow> followers = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addRole(Role role) {
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(this);
        userRoles.add(userRole);
    }
    public void removeRole(Role role) {
        userRoles.removeIf(userRole -> userRole.getRole().equals(role));
    }


    public void follow(User target) {
        if (this.equals(target)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        Follow follow = Follow.builder()
                .follower(this)
                .followed(target)
                .build();
        following.add(follow);
        target.followers.add(follow);
    }

    public void unfollow(User target) {
        following.removeIf(f -> f.getFollowed().equals(target));
        target.followers.removeIf(f -> f.getFollower().equals(this));
    }
}
