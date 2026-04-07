package com.klasavchik.modelHorseProject;

import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationListItemResponse;
import com.klasavchik.modelHorseProject.dto.show.registration.UpdateRegistrationRequest;
import com.klasavchik.modelHorseProject.dto.show.registration.UpdateRegistrationStatusRequest;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Registration;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.ShowCreator;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.repository.show.RegistrationRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowCreatorRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.repository.show.TicketPriceRepository;
import com.klasavchik.modelHorseProject.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class RegistrationSponsorIntegrationTest {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private ShowCreatorRepository showCreatorRepository;
    @Autowired
    private TicketPriceRepository ticketPriceRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void sponsorWithTicketPriceCanBeApprovedAndReadBack() {
        User organizer = userRepository.save(User.builder()
                .email("organizer@test.local")
                .password("pass")
                .build());

        User participant = userRepository.save(User.builder()
                .email("participant@test.local")
                .password("pass")
                .build());

        Show show = showRepository.save(Show.builder()
                .name("Show")
                .description("Desc")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .lotteryEnabled(false)
                .additionalPrice(100)
                .isPaid(true)
                .maxAdditionalPhotos(3)
                .bannerUrl(null)
                .build());

        ShowCreator creatorLink = ShowCreator.builder()
                .show(show)
                .user(organizer)
                .role("creator")
                .build();
        show.getOrganizer().add(creatorLink);
        showCreatorRepository.save(creatorLink);

        TicketPrice ticketPrice = ticketPriceRepository.save(TicketPrice.builder()
                .show(show)
                .type("VIP")
                .price(500)
                .includedModels(3)
                .description("VIP")
                .build());

        Registration registration = registrationRepository.save(Registration.builder()
                .user(participant)
                .show(show)
                .isSponsor(true)
                .additionalModels(0)
                .lotteryTickets(0)
                .status(StatusRegOfShow.PENDING)
                .build());

        UpdateRegistrationRequest updateRequest = UpdateRegistrationRequest.builder()
                .isSponsor(true)
                .ticketPriceId(ticketPrice.getId())
                .extraModels(6)
                .lotteryTickets(0)
                .build();

        RegistrationListItemResponse updated = registrationService.updateRegistration(
                registration.getId(), updateRequest, organizer.getId());

        assertEquals(ticketPrice.getId(), updated.getTicketPriceId());

        UpdateRegistrationStatusRequest statusRequest = UpdateRegistrationStatusRequest.builder()
                .status(StatusRegOfShow.APPROVED)
                .build();

        RegistrationListItemResponse approved = registrationService.updateRegistrationStatus(
                registration.getId(), statusRequest, organizer.getId());

        assertEquals(StatusRegOfShow.APPROVED.name(), approved.getStatus());
        assertEquals(ticketPrice.getId(), approved.getTicketPriceId());

        RegistrationListItemResponse fetched = registrationService.getRegistrationById(
                registration.getId(), organizer.getId());

        assertNotNull(fetched.getTicketPriceId());
        assertEquals(ticketPrice.getId(), fetched.getTicketPriceId());
    }
}
