package com.klasavchik.modelHorseProject.service;

import com.klasavchik.modelHorseProject.entity.settings.Settings;
import com.klasavchik.modelHorseProject.entity.settings.Theme;
import com.klasavchik.modelHorseProject.entity.user.User;
import com.klasavchik.modelHorseProject.repository.SettingsRepository;
import com.klasavchik.modelHorseProject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Theme getTheme(Long userId) {
        Settings settings = getSettings(userId);
        return settings.getTheme();
    }

    @Transactional
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