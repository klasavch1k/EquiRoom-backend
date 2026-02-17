package com.klasavchik.modelHorseProject.controller;

import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.dto.user.*;
import com.klasavchik.modelHorseProject.security.CustomUserDetails;
import com.klasavchik.modelHorseProject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;

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

    @GetMapping("{id}/detail")
    public DetailUserResponse getDetailUser(@PathVariable Long id) {
        return userService.getDetailUser(id);
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

    @GetMapping("/search")
    public Page<UserSearchDTO> searchUsers(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.searchUsers(query, pageable);
    }
    @GetMapping("/{id}/followers")
    public Page<UserSearchDTO> getFollowers(
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getFollowers(id, query, pageable);
    }

    @GetMapping("/{id}/following")
    public Page<UserSearchDTO> getFollowing(
            @PathVariable("id") Long id,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getFollowing(id, query, pageable);
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

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean exists = userService.nicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeRequest request) {
        // Получаем текущего пользователя
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        try {
            // Вызываем сервис, который проверяет старый пароль, хэширует новый и сохраняет
            userService.updatePassword(request, currentUserId);
            return ResponseEntity.ok("Пароль успешно обновлён");
        } catch (RuntimeException e) {
            // Возможные ошибки: старый пароль неверный или новый совпадает со старым
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
// две ручки на изменения почты и номера
//    @PutMapping("/updateContactInform")
//    public ResponseEntity<?> updateContactInform(
//            @RequestPart("userData") UpdatePersonInformRequest userDto) throws IOException {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
//        Long currentUserId = userDetails.getUserId();
//
//        try {
//            userService.updateContactInform(userDto, currentUserId);
//            return ResponseEntity.ok().build();
//        } catch (RuntimeException e) {
//            if (e.getMessage().equals("Email already exists")) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
//            }
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        }
//    }

    @PutMapping("/updatePersonInform")
    public ResponseEntity<?> updatePersonInform(
            @RequestPart("userData") UpdatePersonInformRequest userDto,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getUserId();

        try {
            userService.updatePersonInform(userDto,currentUserId, avatarFile);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Email already exists")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
        }
    }


    //удаляет пользователя
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        userService.deleteById(id);
    }

    // Новые эндпоинты для подписки/отписки
    @PostMapping("/{id}/follow")
    @ResponseStatus(HttpStatus.OK)
    public void follow(@PathVariable("id") Long id) {
        userService.follow(id);
    }


    @PostMapping("/{id}/unfollow")
    @ResponseStatus(HttpStatus.OK)
    public void unfollow(@PathVariable("id") Long id) {
        userService.unfollow(id);
    }
}