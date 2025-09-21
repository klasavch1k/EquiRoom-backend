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
import org.springframework.web.bind.annotation.*;
import com.klasavchik.modelHorseProject.security.JwtUtil;

import java.util.List;

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
    //    создаёт юзера
//    @PostMapping("/register")
//    @ResponseStatus(HttpStatus.CREATED)
//    public String createUser(@RequestBody CreateUserRequest userDto) {
//        return userService.create(userDto);
//    }

    // Создаёт юзера
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createUser(@RequestBody CreateUserRequest userDto) {
        String message = userService.create(userDto);
        // Предполагаем, что create возвращает userId в случае успеха
        Long userId = userService.getUserByEmail(userDto.getEmail())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve user ID"));
        return new RegisterResponse(userId, message);
    }
    //логинация и безопасность
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody CreateUserRequest userDto) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
//        );
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//        String jwt = jwtUtil.generateToken(userDetails);
//        return ResponseEntity.ok(new JwtResponse(jwt)); // Новый класс JwtResponse
//    }

    // Логинация и безопасность
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody CreateUserRequest userDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);
        // Получаем userId из базы по email
        Long userId = userService.getUserByEmail(userDto.getEmail())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new JwtResponse(jwt, userId));
    }

    //обновляет юзера
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User update(@RequestBody UpdateUserRequest userDto){
        return userService.update(userDto);
    }

    //удаляет пользователя
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
    }


}
record JwtResponse(String token, Long userId) {
}

