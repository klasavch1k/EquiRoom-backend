// 1. Show
package com.klasavchik.modelHorseProject.entity.ShowEntity;

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
@ToString(exclude = {"creators", "ticketPrices", "divisions", "judges", "registrations", "showMedia"})
@EqualsAndHashCode(exclude = {"creators", "ticketPrices", "divisions", "judges", "registrations", "showMedia"})
@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private Set<ShowCreator> creators = new HashSet<>();

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
}