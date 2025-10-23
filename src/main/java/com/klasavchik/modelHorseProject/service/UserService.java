package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.Gender;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.Role;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.mapper.UserMapper;
import com.klasavchik.modelHorseProject.repository.RoleRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;
    public  Optional<User> getUserBuId(Long id) {
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
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Email already exists";
        }
        else{
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

        // Обновляем поля
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setBio(dto.getBio());
        System.out.println("Gender from DTO: " + dto.getGender());
        if (dto.getGender() != null && dto.getGender().equals("MALE")) {
            profile.setGender(Gender.MALE);
        } else if (dto.getGender() != null && dto.getGender().equals("FEMALE")) {
            profile.setGender(Gender.FEMALE);
        } else {
            System.out.println("Invalid or null gender: " + dto.getGender());
        }
        profile.setDateOfBirth(dto.getDateOfBirth());
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
        return userMapper.toProfileDTO(user);
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
}
