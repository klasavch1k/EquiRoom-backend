package com.klasavchik.modelHorseProject.mapper;

import com.klasavchik.modelHorseProject.dto.*;
import com.klasavchik.modelHorseProject.entity.Gender;
import com.klasavchik.modelHorseProject.entity.Profile;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.entity.UserRole;
import com.klasavchik.modelHorseProject.repository.FollowRepository;
import com.klasavchik.modelHorseProject.repository.ModelRepository;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {
    private final ModelRepository modelRepository;

    public UserMapper(ModelRepository modelRepository, FollowRepository followRepository) {
        this.modelRepository = modelRepository;
    }

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

    public UserProfileDTO toProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .nickname(user.getProfile().getNickname())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .bio(user.getProfile().getBio())
                .figurines( modelRepository.getCountByOwnerId(user.getId())) // Кол-во фигурок
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
                .id(user.getId())
                .userRoles(user.getUserRoles().stream().map(UserRole::getRole).collect(Collectors.toSet()))
                .email(user.getEmail())
                .password(user.getPassword())
                .build();
    }

    public DetailUserResponse toDetailUserResponse(User user) {
        String gender = null;
        if(user.getProfile().getGender() != null){
            gender = user.getProfile().getGender().toString();
        }
        return DetailUserResponse.builder()
                .id(user.getId())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getProfile().getAvatar())
                .dateOfBirth(user.getProfile().getDateOfBirth())
                .gender(gender)
                .bio(user.getProfile().getBio())
                .nickname(user.getProfile().getNickname())
                .build();
    }
}
