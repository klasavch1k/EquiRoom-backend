// 10. EntryMedia (выбранные фото для конкретной записи в шоу)
package com.klasavchik.modelHorseProject.entity.ShowEntity;

import com.klasavchik.modelHorseProject.entity.model.ModelMedia;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "entry_media")
public class EntryMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private Entry entry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private ModelMedia media;

    private boolean isMain;     // основное фото
    private Integer photoOrder;      // порядок отображения
}