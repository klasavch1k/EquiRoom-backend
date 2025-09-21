package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.dto.CreateUserRequest;
import com.klasavchik.modelHorseProject.dto.UpdateUserRequest;
import com.klasavchik.modelHorseProject.dto.UserProfileDTO;
import com.klasavchik.modelHorseProject.dto.UserResponse;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.Role;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.mapper.UserMapper;
import com.klasavchik.modelHorseProject.repository.RoleRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private static int flag = 0;

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
        return userRepository.findByEmail(email);
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Инициализируем коллекцию для избежания LazyInitializationException
        Hibernate.initialize(user.getHorseModelsOwn());
        return userMapper.toProfileDTO(user);
    }
}
