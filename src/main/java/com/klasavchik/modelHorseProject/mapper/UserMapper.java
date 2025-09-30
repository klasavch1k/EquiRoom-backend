package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.entity.UserRole;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
                .password(userDto.getPassword())
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
                .userName(user.getProfile().getNickName()) // Используем как отображаемое имя
                .figurines((int) user.getModelsOwn().size()) // Кол-во фигурок
                .followers(10)
                .following(10)
                .avatar(user.getProfile().getAvatar())// Заглушка, замени логикой
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
                .userRoles(user.getUserRoles().stream().map(UserRole::getRole).collect(Collectors.toSet()))
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
    }

}
