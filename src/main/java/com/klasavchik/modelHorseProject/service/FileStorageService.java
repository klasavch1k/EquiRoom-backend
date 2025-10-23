package com.klasavchik.modelHorseProject.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("D:/projectPhoto");

    // Модифицированный метод с параметром folder (e.g., "horses" или "avatars")
    public String saveFile(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Пустой файл невозможно сохранить");
        }

        // Создание директории root/folder, если не существует
        Path dir = root.resolve(folder);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // Генерируем новое имя файла (UUID + оригинальное расширение)
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path filePath = dir.resolve(filename);

        // Копируем с заменой
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Возвращаем относительный URL
        return "/uploads/" + folder + "/" + filename;
    }

    private String getFileExtension(String originalName) {
        if (originalName == null) return "";
        int dotIndex = originalName.lastIndexOf('.');
        return (dotIndex > 0 && dotIndex < originalName.length() - 1)
                ? originalName.substring(dotIndex + 1)
                : "";
    }
}