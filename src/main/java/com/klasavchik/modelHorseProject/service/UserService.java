package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.Gender;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.Role;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.mapper.UserMapper;
import com.klasavchik.modelHorseProject.repository.FollowRepository;
import com.klasavchik.modelHorseProject.repository.RoleRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository; // Новая зависимость
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    public Optional<User> getUserBuId(Long id) {
        return userRepository.findById(id);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllWithProfile().stream().map(userMapper::toUserResponse).toList();
    }

    @Transactional
    public String create(CreateUserRequest userDto) {
        Role roleUser = roleRepository.findByRoleName("ROLE_USER");
        Profile profile = Profile.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .build();
        User user = userMapper.DtoToEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.addRole(roleRepository.findByRoleName(roleUser.getRoleName()));
        user.setCreatedAt(LocalDate.now());
        user.setProfile(profile);
        System.out.println("юзер готов");
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Email already exists";
        } else {
            userRepository.save(user);
            return "User created: " + user.getEmail();
        }
    }

    // Новый метод для получения пользователя по email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailWithProfileAndRoles(email);
    }

    @Transactional
    public void update(UpdateUserRequest dto, MultipartFile avatarFile) throws IOException {
        System.out.println("Starting update for user ID: " + dto.getId());
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> {
                    System.out.println("User not found for ID: " + dto.getId());
                    return new RuntimeException("User not found");
                });
        System.out.println("User found: " + user.getEmail());

        // Проверяем уникальность email, если он изменился
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.findByEmail(dto.getEmail()).isPresent()) {
            System.out.println("Email already exists: " + dto.getEmail());
            throw new RuntimeException("Email already exists");
        }


        Profile profile = user.getProfile();
        if (profile == null) {
            System.out.println("Profile is null, creating new profile");
            profile = new Profile();
            user.setProfile(profile);
        }
        String oldNickname = user.getProfile() != null ? user.getProfile().getNickname() : null;
        String newNickname = dto.getNickname();

// Никнейм изменился?
        boolean nicknameChanged = !Objects.equals(oldNickname, newNickname);

// Если изменился — проверяем, не занят ли новый никнейм
        if (nicknameChanged && newNickname != null && !newNickname.trim().isEmpty()) {
            boolean nicknameTaken = userRepository.findByProfile_NicknameIgnoreCase(newNickname)
                    .filter(u -> !u.getId().equals(user.getId())) // исключаем текущего пользователя
                    .isPresent();

            if (nicknameTaken) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nickname already exists");
            }
        }

        // Обновляем поля
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBio(dto.getBio());
        profile.setNickname(dto.getNickname());
        profile.setDateOfBirth(dto.getDateOfBirth());

        if (dto.getGender() != null && dto.getGender().equals("MALE")) {
            profile.setGender(Gender.MALE);
        } else if (dto.getGender() != null && dto.getGender().equals("FEMALE")) {
            profile.setGender(Gender.FEMALE);
        } else {
            System.out.println("Invalid or null gender: " + dto.getGender());
        }

        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());

        // Обработка аватарки
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = fileStorageService.saveFile(avatarFile, "avatars");
            profile.setAvatar(avatarUrl);
            System.out.println("Avatar uploaded: " + avatarUrl);
        } else if (dto.getAvatar() == null) {
            // Если явно передан null — удаляем аватарку
            profile.setAvatar(null);
            System.out.println("Avatar removed");
        }
        // Если avatarFile null и dto.avatar не null — оставляем как есть

        System.out.println("Attempting to save user: " + user.getEmail());
        try {
            User savedUser = userRepository.save(user);
            System.out.println("User saved successfully: " + savedUser.getEmail());
        } catch (DataIntegrityViolationException e) {
            System.out.println("Data integrity violation during save: " + e.getMessage());
            throw new RuntimeException("Failed to save user due to data integrity violation", e);
        } catch (Exception e) {
            System.out.println("Unexpected error during save: " + e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findByIdWithProfileRolesAndModels(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isFollowedByCurrentUser = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long currentUserId = userDetails.getUserId();
            isFollowedByCurrentUser = followRepository.existsByFollowerIdAndFollowedId(currentUserId, id);
        }

        UserProfileDTO dto = userMapper.toProfileDTO(user);
        dto.setFollowers(followRepository.countFollowers(id));
        dto.setFollowing(followRepository.countFollowing(id));
        dto.setIsFollowedByCurrentUser(isFollowedByCurrentUser);
        return dto;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public Page<UserSearchDTO> searchUsers(String query, Pageable pageable) {
        String search = "%" + query.toLowerCase() + "%";
        return userRepository.findBySearch(search, pageable);
    }

    @Transactional(readOnly = true)
    public JwtResponse login(CreateUserRequest userDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = getUserByEmail(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .toList();

        String jwt = jwtUtil.generateToken(userDetails, user.getId(), roles);
        return new JwtResponse(jwt, user.getId(), roles);
    }

    public DetailUserResponse getDetailUser(Long id) {
        User user = userRepository.findByIdWithProfileRolesAndModels(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        DetailUserResponse detailUserResponse = userMapper.toDetailUserResponse(user);
        return detailUserResponse;
    }

    // Новые методы для подписки/отписки
    @Transactional
    public void follow(Long targetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (followRepository.existsByFollowerIdAndFollowedId(currentUserId, targetId)) {
            return; // Уже подписан, идемпотентно
        }

        currentUser.follow(target);
//        userRepository.save(currentUser);
    }


    @Transactional
    public void unfollow(Long targetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (!followRepository.existsByFollowerIdAndFollowedId(currentUserId, targetId)) {
            return; // Не подписан, идемпотентно
        }

        currentUser.unfollow(target);
//        userRepository.save(currentUser);
    }

    public boolean nicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public Page<UserSearchDTO> getFollowers(Long userId, String query, Pageable pageable) {
        String search = "%" + query.toLowerCase() + "%";
        return followRepository.findFollowers(userId, search, pageable);
    }

    public Page<UserSearchDTO> getFollowing(Long userId, String query, Pageable pageable) {
        String search = "%" + query.toLowerCase() + "%";
        Page<UserSearchDTO> following = followRepository.findFollowing(userId, search, pageable);
        return following;
    }
}