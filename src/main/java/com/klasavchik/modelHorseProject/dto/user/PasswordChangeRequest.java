package com.klasavchik.modelHorseProject.dto.user;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PasswordChangeRequest {
    private String oldPassword;
    private String newPassword;
}
