package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.Role;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.mapper.UserMapper;
import com.klasavchik.modelHorseProject.repository.RoleRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public  Optional<User> getUserBuId(Long id) {
        return userRepository.findById(id);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllWithProfile().stream().map(userMapper::toUserResponse).toList();
    }

    @Transactional
    public String create(CreateUserRequest userDto) {
        System.out.println("мы дошли до креата");
        Role roleUser = roleRepository.findByRoleName("ROLE_USER");
        Profile profile = Profile.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .build();
        System.out.println("мы сделали роли и профайл");

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
    public User update(UpdateUserRequest dto) {
        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(dto.getPassword());

        Profile profile = user.getProfile();
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());

        return userRepository.save(user); // Hibernate поймёт, что это managed entity
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long id) {
        User user = userRepository.findByIdWithProfileAndRoles(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Инициализируем коллекцию для избежания LazyInitializationException
        Hibernate.initialize(user.getHorseModelsOwn());
        return userMapper.toProfileDTO(user);
    }
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
}
