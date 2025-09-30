package com.klasavchik.modelHorseProject.entity;

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

//тут держим всю остальную инфу о юзере, для большей сепарации данных
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
    private String nickName;
    private String status; // кринж подпись в аккаунте

    //базовый набор, если надо, то будем изменять
}
