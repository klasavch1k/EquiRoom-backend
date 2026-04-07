package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.price.CreateTicketPriceDto;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.repository.show.TicketPriceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketPriceService {

    private final TicketPriceRepository ticketPriceRepository;
    private final ShowRepository showRepository;

    public void addTicketPrice(Long showId, CreateTicketPriceDto dto, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEdit(show, userId);

        // Проверка уникальности type
        if (ticketPriceRepository.existsByShowIdAndType(showId, dto.getType())) {
            throw new IllegalArgumentException("Цена с типом " + dto.getType() + " уже существует");
        }

        TicketPrice price = TicketPrice.builder()
                .show(show)
                .type(dto.getType())
                .price(dto.getPrice())
                .includedModels(dto.getIncludedModels())
                .description(dto.getDescription())
                .build();

        ticketPriceRepository.save(price);
    }

    public void deleteTicketPrice(Long showId, Long priceId, Long userId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        checkCanEdit(show, userId);

        TicketPrice price = ticketPriceRepository.findById(priceId)
                .orElseThrow(() -> new EntityNotFoundException("Цена не найдена"));

        if (!price.getShow().getId().equals(showId)) {
            throw new IllegalArgumentException("Цена не принадлежит этому шоу");
        }

        ticketPriceRepository.delete(price);
    }

    private void checkCanEdit(Show show, Long userId) {
        boolean isCreator = show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(userId) && "creator".equals(sc.getRole()));
        if (!isCreator) {
            throw new AccessDeniedException("Только создатель может менять цены");
        }
        if (show.isCompleted() || (show.getEndDate() != null && !java.time.LocalDate.now().isBefore(show.getEndDate()))) {
            throw new com.klasavchik.modelHorseProject.exception.ShowReadOnlyException();
        }
        if (show.isStarted()) {
            throw new IllegalStateException("Нельзя менять билеты после старта шоу");
        }
    }

}