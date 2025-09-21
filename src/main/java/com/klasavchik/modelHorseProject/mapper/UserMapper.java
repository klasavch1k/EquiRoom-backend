package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    //мапит CreateUserRequest в User
    public User DtoToEntity(CreateUserRequest userDto) {
        Profile profile = Profile.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .build();
        return User.builder()
                .profile(profile)
                .email(userDto.getEmail())
                .build();
    }
    //мапит UpdateUserRequest в User
    public User DtoToEntity(UpdateUserRequest userDto) {
        Profile profile = Profile.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .id(userDto.getId())
                .build();
        return User.builder()
                .profile(profile)
                .password(userDto.getPassword())
                .email(userDto.getEmail())
                .build();
    }

    public UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .nickName(user.getProfile().getNickName()) // Используем как отображаемое имя
                .modelsCount((int) user.getModelsOwn().size()) // Кол-во фигурок
                .collectingCount(0) // Заглушка, замени логикой
                .membersCount(0) // Заглушка, замени логикой
                .build();
    }
    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .email(user.getEmail())
                .build();
    }
    public UserAuthDto toUserAuthDto(User user) {
        return UserAuthDto.builder()
                .userRoles(user.getUserRoles().stream().map(UserRole::getRole).toList())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
    }

}
