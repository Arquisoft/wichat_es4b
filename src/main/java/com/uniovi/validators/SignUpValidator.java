package com.uniovi.validators;

import com.uniovi.dto.PlayerDto;
import com.uniovi.services.PlayerService;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class SignUpValidator implements Validator {
    private PlayerService playerService;

    public SignUpValidator(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(PlayerDto.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PlayerDto user = (PlayerDto) target;

        if (!EmailValidator.getInstance().isValid(user.getEmail())) {
            errors.rejectValue("email", "signup.error.email.valid",
                    "El email no es válido");
        }

        if(playerService.getUserByEmail(user.getEmail()).isPresent()){
            errors.rejectValue("email", "signup.error.email.already",
                    "Ya hay una cuenta registrada con este email");
        }

        if (playerService.getUserByUsername(user.getUsername()).isPresent()) {
            errors.rejectValue("username", "signup.error.username.already",
                    "Ya existe una cuenta con este nombre de usuario");
        }

        if (user.getPassword() == null
            || !user.getPassword().equals(user.getPasswordConfirm())) {
            errors.rejectValue("passwordConfirm", "signup.error.password.match",
                    "Las contraseñas no coinciden");
        }
    }
}
