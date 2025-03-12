package com.uniovi.validators;

import com.uniovi.dto.PlayerDto;
import com.uniovi.entities.Player;
import com.uniovi.services.PlayerService;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class EditUserValidator implements Validator {
    private PlayerService playerService;
    private String originalUsername;

    public EditUserValidator(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setOriginalUsername(String username) {
        this.originalUsername = username;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(PlayerDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PlayerDto user = (PlayerDto) target;
        Optional<Player> originalUser = playerService.getUserByUsername(originalUsername);

        if (!originalUser.isPresent()) {
            return;
        }

        if (!EmailValidator.getInstance().isValid(user.getEmail())) {
            errors.rejectValue("email", "edit.error.email.valid",
                    "El email no es válido");
        }

        // Comprobar email solo si ha cambiado
        if(!originalUser.get().getEmail().equals(user.getEmail()) &&
                playerService.getUserByEmail(user.getEmail()).isPresent()) {
            errors.rejectValue("email", "edit.error.email.already",
                    "Ya hay una cuenta registrada con este email");
        }

        // Comprobar username solo si ha cambiado
        if (!originalUser.get().getUsername().equals(user.getUsername()) &&
                playerService.getUserByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "edit.error.username.already",
                    "Ya existe una cuenta con este nombre de usuario");
        }
    }
}