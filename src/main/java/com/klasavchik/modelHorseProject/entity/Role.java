package com.klasavchik.modelHorseProject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "userRole")
@EqualsAndHashCode(exclude = "userRole")

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;
    private String roleName;

//    @OneToMany(mappedBy = "role", orphanRemoval = true)
//    private List<UserRole> userRole;
}
