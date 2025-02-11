package com.uniovi.components;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws ServletException, IOException {
        setDefaultFailureUrl("/login?error");

        super.onAuthenticationFailure(request, response, exception);

        String errorMessage = null;

        if (exception.getMessage().equalsIgnoreCase("User is disabled")) {
            errorMessage = "Tu cuenta ha sido deshabilitada.";
        } else if (exception.getMessage().equalsIgnoreCase("User account has expired")) {
            errorMessage = "Tu cuenta ha expirado";
        } else if (exception.getMessage().equalsIgnoreCase("Bad credentials")) {
            errorMessage = "Usuario o contraseña incorrectos";
        }

        request.getSession().setAttribute("loginErrorMessage", errorMessage);
    }
}