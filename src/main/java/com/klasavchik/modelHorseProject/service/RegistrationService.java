package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationListItemResponse;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Registration;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.entity.user.Profile;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.show.RegistrationRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ShowRepository showRepository;
    private final ShowService showService; // для проверки роли

    @Transactional(readOnly = true)
    public List<RegistrationListItemResponse> getRegistrationsForShow(Long showId, Long currentUserId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        // Если текущий пользователь — организатор или судья → показываем все
        boolean isOrganizerOrJudge = showService.isOrganizerOrJudge(show, currentUserId);

        List<Registration> registrations;
        if (isOrganizerOrJudge) {
            registrations = registrationRepository.findByShowId(showId);
        } else {
            registrations = registrationRepository.findApprovedByShowId(showId);
        }

        return registrations.stream().map(reg -> {
            User user = reg.getUser();
            Profile profile = user.getProfile();

            String displayName = profile != null
                    ? (profile.getFirstName() + " " + profile.getLastName()).trim()
                    : user.getEmail();
            String nickname = profile != null ? profile.getNickname() : null;

            TicketPrice tp = reg.getTicketPrice();
            int total = reg.getEntries().size();
            int included = tp != null ? tp.getIncludedModels() : 0;
            int additional = Math.max(0, total - included);

            return RegistrationListItemResponse.builder()
                    .registrationId(reg.getId())
                    .userId(user.getId())
                    .userNickname(nickname)
                    .userDisplayName(displayName.isEmpty() ? user.getEmail() : displayName)
                    .userAvatarUrl(profile != null ? profile.getAvatar() : null)
                    .ticketPriceId(tp != null ? tp.getId() : null)
                    .ticketType(tp != null ? tp.getType() : "Бесплатно")
                    .ticketPriceValue(tp != null ? tp.getPrice() : 0)
                    .includedModels(tp != null ? tp.getIncludedModels() : 0)
                    .totalModels(total)
                    .additionalModels(additional)
                    .additionalPrice(show.getAdditionalPrice())
                    .isSponsor(reg.isSponsor())
                    .isJudge(reg.isJudge())
                    .lotteryTickets(reg.getLotteryTickets())
                    .status(reg.getStatus())
                    .createdAt(reg.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}