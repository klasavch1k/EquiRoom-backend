package com.klasavchik.modelHorseProject.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("D:/projectPhoto/horses");

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Пустой файл невозможно сохранить");
        }

        // Создание директории при первом запуске
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        // Генерируем новое имя файла (UUID + оригинальное расширение)
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path filePath = root.resolve(filename);

        // Копируем с заменой, если вдруг совпадёт UUID (практически невозможно)
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Возвращаем путь, который фронт сможет использовать
        return "/uploads/horses/" + filename;
    }

    private String getFileExtension(String originalName) {
        if (originalName == null) return "";
        int dotIndex = originalName.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < originalName.length() - 1)
                ? originalName.substring(dotIndex + 1)
                : "";
    }
}
