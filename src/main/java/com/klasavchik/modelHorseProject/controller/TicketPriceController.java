package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.show.price.CreateTicketPriceDto;
import com.klasavchik.modelHorseProject.dto.show.price.TicketPriceDto;
import com.klasavchik.modelHorseProject.dto.show.price.UpdateAdditionalPriceDto;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.repository.show.TicketPriceRepository;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.ShowService;
import com.klasavchik.modelHorseProject.service.TicketPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
@CrossOrigin(origins = "http://localhost:3000")
public class TicketPriceController {

    private final TicketPriceService ticketPriceService;
    private final TicketPriceRepository ticketPriceRepository;
    private final ShowService showService; // если нужно проверять права

    // Добавление одной цены
    @PostMapping("/{showId}/ticket-prices")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> addTicketPrice(
            @PathVariable Long showId,
            @Valid @RequestBody CreateTicketPriceDto dto) {

        Long userId = getCurrentUserId(); // твой метод из SecurityContext
        ticketPriceService.addTicketPrice(showId, dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Удаление одной цены
    @DeleteMapping("/{showId}/ticket-prices/{priceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteTicketPrice(
            @PathVariable Long showId,
            @PathVariable Long priceId) {

        Long userId = getCurrentUserId();
        ticketPriceService.deleteTicketPrice(showId, priceId, userId);
        return ResponseEntity.noContent().build();
    }

    // Изменение только additionalPrice
    @PatchMapping("/{showId}/additional-price")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> updateAdditionalPrice(
            @PathVariable Long showId,
            @RequestBody UpdateAdditionalPriceDto dto) {

        Long userId = getCurrentUserId();
        showService.updateAdditionalPrice(showId, dto.getAdditionalPrice(), userId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        // твой метод, как в других контроллерах
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getUserId();
    }
    // В ShowController или новом TicketPriceController

    @GetMapping("/shows/{showId}/ticket-prices")
    public ResponseEntity<List<TicketPriceDto>> getTicketPrices(@PathVariable Long showId) {
        List<TicketPrice> prices = ticketPriceRepository.findByShowId(showId);

        List<TicketPriceDto> dtos = prices.stream()
                .map(p -> TicketPriceDto.builder()
                        .id(p.getId())
                        .type(p.getType())
                        .price(p.getPrice())
                        .includedModels(p.getIncludedModels())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

}