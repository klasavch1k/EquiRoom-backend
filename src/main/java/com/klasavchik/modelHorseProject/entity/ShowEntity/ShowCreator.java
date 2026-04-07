// 2. ShowCreator (many-to-many между Show и User)
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import com.klasavchik.modelHorseProject.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "show_creators", uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "user_id"}))
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShowCreator that = (ShowCreator) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}