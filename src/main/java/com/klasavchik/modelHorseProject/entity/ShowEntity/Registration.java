// 8. Registration
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import com.klasavchik.modelHorseProject.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"entries"})
@EqualsAndHashCode(exclude = {"entries"})
@Entity
@Table(name = "registrations", indexes = {
        @Index(name = "idx_registration_application_number", columnList = "application_number")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_registration_user_show", columnNames = {"user_id", "show_id"})
})
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_price_id")
    private TicketPrice ticketPrice;

    private Integer lotteryTickets;
    private boolean isSponsor;
    private boolean isJudge;

    private Integer additionalModels;
    @Enumerated(EnumType.STRING)
    private StatusRegOfShow status;      // pending, paid, approved, cancelled...

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Entry> entries = new HashSet<>();

    @Column(name = "application_number", unique = true)
    private String applicationNumber;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}