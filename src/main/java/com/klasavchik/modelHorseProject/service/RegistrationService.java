package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.show.registration.CreateRegistrationRequest;
import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationCreateResponse;
import com.klasavchik.modelHorseProject.dto.show.registration.RegistrationListItemResponse;
import com.klasavchik.modelHorseProject.dto.show.registration.UpdateRegistrationRequest;
import com.klasavchik.modelHorseProject.dto.show.registration.UpdateRegistrationStatusRequest;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Registration;
import com.klasavchik.modelHorseProject.entity.ShowEntity.Show;
import com.klasavchik.modelHorseProject.entity.ShowEntity.StatusRegOfShow;
import com.klasavchik.modelHorseProject.entity.ShowEntity.TicketPrice;
import com.klasavchik.modelHorseProject.entity.user.Profile;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.repository.show.RegistrationRepository;
import com.klasavchik.modelHorseProject.repository.show.ShowRepository;
import com.klasavchik.modelHorseProject.repository.show.TicketPriceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final TicketPriceRepository ticketPriceRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public Page<RegistrationListItemResponse> getRegistrationsForShowPaged(
            Long showId,
            Pageable pageable) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        Page<Registration> registrationsPage = registrationRepository.findByShowId(showId, pageable);

        return registrationsPage.map(reg -> mapToResponse(reg, show));
    }
    @Transactional
    public RegistrationCreateResponse createRegistration(
            Long showId,
            CreateRegistrationRequest request,
            Long currentUserId) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (registrationRepository.existsByUserIdAndShowId(currentUserId, showId)) {
            throw new IllegalStateException("Вы уже зарегистрированы на это шоу");
        }

        Registration registration = Registration.builder()
                .user(user)
                .show(show)
                .lotteryTickets(request.getLotteryTickets() != null ? request.getLotteryTickets() : 0)
                .isSponsor(request.isSponsor())
                .additionalModels(request.getExtraModels() != null ? request.getExtraModels() : 0)
                .status(StatusRegOfShow.PENDING)  // ВСЕГДА PENDING при создании
                .build();

        if (!request.isSponsor()) {
            // Обычный участник — тариф обязателен
            if (request.getTicketPriceId() == null) {
                throw new IllegalArgumentException("Для обычных участников обязателен выбор тарифа");
            }

            TicketPrice ticketPrice = ticketPriceRepository.findById(request.getTicketPriceId())
                    .orElseThrow(() -> new EntityNotFoundException("Тариф не найден"));

            if (!ticketPrice.getShow().getId().equals(showId)) {
                throw new IllegalArgumentException("Тариф не принадлежит этому шоу");
            }

            registration.setTicketPrice(ticketPrice);
        } else {
            // Спонсор — тариф пока не выбираем, оставляем null
            registration.setTicketPrice(null);
        }

        registration = registrationRepository.save(registration);

        // Генерация applicationNumber через sequence (миграция sequence оставлена отдельно)
        Object nextObj = em.createNativeQuery("SELECT nextval('application_number_seq')").getSingleResult();
        long nextVal;
        if (nextObj instanceof Number) {
            nextVal = ((Number) nextObj).longValue();
        } else {
            // на случай неожиданного типа, попробуем распарсить в long
            nextVal = Long.parseLong(nextObj.toString());
        }
        String formatted = String.format("REQ-%06d", nextVal);
        registration.setApplicationNumber(formatted);
        registration = registrationRepository.save(registration);

        String message = request.isSponsor()
                ? "Заявка спонсора отправлена. Организатор свяжется с вами для обсуждения вклада."
                : "Регистрация создана. Ожидайте обратной связи для оплаты и подтверждения заявки.";

        return RegistrationCreateResponse.builder()
                .registrationId(registration.getId())
                .applicationNumber(registration.getApplicationNumber())
                .status(registration.getStatus())
                .isSponsor(registration.isSponsor())
                .message(message)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<RegistrationListItemResponse> searchRegistrations(
            Long showId,
            String searchQuery,
            Pageable pageable) {

        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Шоу не найдено"));

        // Поиск по displayName / nickname / email (через JOIN с User и Profile)
        Page<Registration> page = registrationRepository.searchByNameOrEmail(
                showId,
                "%" + searchQuery.toLowerCase() + "%",  // case-insensitive
                pageable
        );

        return page.map(reg -> mapToResponse(reg, show));
    }

    private RegistrationListItemResponse mapToResponse(Registration reg, Show show) {
        User user = reg.getUser();
        Profile profile = user.getProfile();

        String displayName = profile != null
                ? (profile.getFirstName() + " " + profile.getLastName()).trim()
                : user.getEmail();

        String nickname = profile != null ? profile.getNickname() : null;

        TicketPrice tp = reg.getTicketPrice();
        int total = reg.getEntries().size();

        int ticketCost = (tp != null) ? tp.getPrice() : 0;
        int additionalCost = reg.getAdditionalModels() * (show.getAdditionalPrice() != null ? show.getAdditionalPrice() : 0);
        int totalCost = ticketCost + additionalCost;

        return RegistrationListItemResponse.builder()
                .registrationId(reg.getId())
                .applicationNumber(reg.getApplicationNumber())
                .userId(user.getId())
                .userNickname(nickname)
                .userDisplayName(displayName.isEmpty() ? user.getEmail() : displayName)
                .userAvatarUrl(profile != null ? profile.getAvatar() : null)
                .ticketPriceId(tp != null ? tp.getId() : null)
                .ticketType(tp != null ? tp.getType() : "Бесплатно")
                .ticketPriceValue(ticketCost)
                .includedModels(tp != null ? tp.getIncludedModels() : 0)
                .totalModels(total)
                .additionalModels(reg.getAdditionalModels())
                .additionalPrice(show.getAdditionalPrice())
                .isSponsor(reg.isSponsor())
                .isJudge(reg.isJudge())
                .lotteryTickets(reg.getLotteryTickets())
                .totalCost(totalCost)
                .status(reg.getStatus().name())
                .createdAt(reg.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public RegistrationListItemResponse getRegistrationById(Long registrationId, Long currentUserId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Регистрация не найдена"));

        Show show = registration.getShow();
        boolean isOwner = registration.getUser().getId().equals(currentUserId);

        // Организаторов надо проверить
        // Здесь предполагаем, что организаторы проверяются в контроллере или сервисе более глобально,
        // но для безопасности проверим, имеет ли право пользователь видеть эту регистрацию.
        // Пока разрешаем владельцу и (потенциально) организаторам.
        // Если это организатор, он должен иметь доступ.
        // Для простоты пока проверим: если не владелец, то должен быть организатором шоу.

        if (!isOwner) {
            // Простейшая проверка: список организаторов из шоу.
            // Но лучше делегировать эту проверку выше или использовать ShowService вспомогательный метод (если есть доступ).
            // В данном классе нет прямого доступа к ShowService (циклическая зависимость), поэтому проверим через репозиторий или коллекцию.
            boolean isOrganizer = show.getOrganizer().stream()
                    .anyMatch(sc -> sc.getUser().getId().equals(currentUserId));

            if (!isOrganizer) {
               // throw new org.springframework.security.access.AccessDeniedException("Нет доступа к просмотру этой регистрации");
               // Пока не будем кидать исключение, если фронт просто запрашивает по ID, предполагаем, что контроллер уже проверил права на уровне шоу
               // Но ID регистрации уникален.
               // Давайте всё же обезопасим: Если не владелец и не орг - ошибка.

                boolean isJudge = show.getJudges().stream()
                       .anyMatch(j -> j.getUser().getId().equals(currentUserId));

                if (!isJudge) {
                     throw new SecurityException("Вы не имеете права просматривать эту регистрацию");
                }
            }
        }

        return mapToResponse(registration, show);
    }

    @Transactional
    public RegistrationListItemResponse updateRegistration(Long registrationId, UpdateRegistrationRequest request, Long currentUserId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Регистрация не найдена"));

        Show show = registration.getShow();
        boolean isOwner = registration.getUser().getId().equals(currentUserId);
        boolean isOrganizer = show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(currentUserId));

        if (!isOwner && !isOrganizer) {
            throw new SecurityException("У вас нет прав на редактирование этой регистрации");
        }

        // Логика: если владелец, то можно только если PENDING.
        // Если организатор - можно всегда.
        if (isOwner && !isOrganizer) {
            if (registration.getStatus() == StatusRegOfShow.APPROVED) {
                throw new IllegalStateException("Заявка уже одобрена, редактирование запрещено. Обратитесь к организатору.");
            }
            // Если статус PAID, CANCELLED? Обычно PENDING - это "на рассмотрении".
            // Если PAID - то тоже нельзя наверное, деньги уплачены.
            // Пользователь сказал: "пока заявка на рассмотрении, можно менять её как угодно".
            // Обычно "на рассмотрении" это PENDING.
            if (registration.getStatus() != StatusRegOfShow.PENDING) {
                 throw new IllegalStateException("Редактирование заявки возможно только в статусе 'На рассмотрении'");
            }
        }

        // Применяем изменения

        // 1. Спонсорство
        registration.setSponsor(request.isSponsor());

        // 2. Лотерея
        registration.setLotteryTickets(request.getLotteryTickets() != null ? request.getLotteryTickets() : 0);

        // 3. Доп модели
        registration.setAdditionalModels(request.getExtraModels() != null ? request.getExtraModels() : 0);

        // 4. Тариф
        if (!request.isSponsor()) {
            if (request.getTicketPriceId() == null) {
                throw new IllegalArgumentException("Для обычных участников обязателен выбор тарифа");
            }
            // Проверяем, изменился ли тариф или его не было
            if (registration.getTicketPrice() == null || !registration.getTicketPrice().getId().equals(request.getTicketPriceId())) {
                TicketPrice ticketPrice = ticketPriceRepository.findById(request.getTicketPriceId())
                        .orElseThrow(() -> new EntityNotFoundException("Тариф не найден"));

                if (!ticketPrice.getShow().getId().equals(show.getId())) {
                    throw new IllegalArgumentException("Тариф не принадлежит этому шоу");
                }
                registration.setTicketPrice(ticketPrice);
            }
        } else {
            // Если стал спонсором - тариф сбрасываем
            registration.setTicketPrice(null);
        }

        // Сохраняем
        registration = registrationRepository.save(registration);

        return mapToResponse(registration, show);
    }

    @Transactional
    public RegistrationListItemResponse updateRegistrationStatus(Long registrationId, UpdateRegistrationStatusRequest request, Long currentUserId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Регистрация не найдена"));

        Show show = registration.getShow();

        // Статус может менять ТОЛЬКО организатор
        boolean isOrganizer = show.getOrganizer().stream()
                .anyMatch(sc -> sc.getUser().getId().equals(currentUserId));

        if (!isOrganizer) {
            throw new SecurityException("Только организатор может менять статус заявки");
        }

        if (request.getStatus() != null) {
            registration.setStatus(request.getStatus());
        }

        registration = registrationRepository.save(registration);
        return mapToResponse(registration, show);
    }
}