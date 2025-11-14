package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.entity.Settings;
import com.klasavchik.modelHorseProject.entity.Theme;
import com.klasavchik.modelHorseProject.entity.User;
import com.klasavchik.modelHorseProject.entity.Theme;
import com.klasavchik.modelHorseProject.repository.SettingsRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    public Theme getTheme(Long userId) {
        Settings settings = getSettings(userId);
        return settings.getTheme();
    }

    public void changeTheme(Long userId, Theme theme) {
        Settings settings = getSettings(userId);
        settings.setTheme(theme);
        settingsRepository.save(settings);
    }

    private Settings getSettings(Long userId) {
        return userRepository.findById(userId)
                .map(User::getSettings)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
    }
}