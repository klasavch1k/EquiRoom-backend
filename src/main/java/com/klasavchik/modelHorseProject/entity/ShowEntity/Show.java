// 1. Show
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"creators", "ticketPrices", "divisions", "judges", "registrations", "showMedia"})
@EqualsAndHashCode(exclude = {"creators", "ticketPrices", "divisions", "judges", "registrations", "showMedia"})
@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String rulesFileUrl;

    private boolean lotteryEnabled;
    private Integer additionalPrice;        // цена доп. модели
    private boolean isPaid;                 // платное / бесплатное шоу
    private Integer maxAdditionalPhotos;    // макс. доп. фото на одну запись

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ShowCreator> organizer = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TicketPrice> ticketPrices = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Division> divisions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Judge> judges = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Registration> registrations = new HashSet<>();

    private String bannerUrl;   // URL баннера шоу (может быть null)
    private boolean isCompleted = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isCompleted = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Show show = (Show) o;
        return Objects.equals(id, show.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isStarted() {
        return startDate != null && !startDate.isAfter(LocalDate.now());
    }
}