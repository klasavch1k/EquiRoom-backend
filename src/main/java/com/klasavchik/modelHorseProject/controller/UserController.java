package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.klasavchik.modelHorseProject.security.JwtUtil;

import java.util.List;
import java.util.stream.Collectors;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    //возвращает список всех юзеров
    @GetMapping
    public List<UserResponse> getUsers() {
        return userService.getAllUsers();
    }

    //возвращает определённого юзера
    @GetMapping("/{id}")
    public UserProfileDTO getUser(@PathVariable("id") Long id) {
        return userService.getUserProfile(id);
    }

    // Создаёт юзера
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createUser(@RequestBody CreateUserRequest userDto) {
        String message = userService.create(userDto);
        Long userId = userService.getUserByEmail(userDto.getEmail())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve user ID"));
        return new RegisterResponse(userId, message);
    }

    // Логинация и безопасность
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CreateUserRequest userDto) {
        try {
            JwtResponse jwtResponse = userService.login(userDto);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User update(@RequestBody UpdateUserRequest userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = (Long) auth.getDetails();
        List<String> roles = auth.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        if (!roles.contains("ROLE_ADMIN") && !userDto.getId().equals(currentUserId)) {
            throw new RuntimeException("Access denied: You can only edit your own profile");
        }

        return userService.update(userDto);
    }

    //удаляет пользователя
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
    }


}


