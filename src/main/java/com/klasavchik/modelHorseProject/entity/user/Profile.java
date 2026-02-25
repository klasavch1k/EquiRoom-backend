package com.klasavchik.modelHorseProject.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity

public class Profile {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String avatar;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
        @Column(unique = true)
    private String nickname;
    private String bio;
    private String vkNickname;
}
