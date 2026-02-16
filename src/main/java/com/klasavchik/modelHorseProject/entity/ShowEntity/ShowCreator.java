// 2. ShowCreator (many-to-many между Show и User)
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import com.klasavchik.modelHorseProject.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "show_creators")
public class ShowCreator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String role; // "creator", "co-organizer" и т.д.
}