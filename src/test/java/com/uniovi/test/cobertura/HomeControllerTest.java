package com.uniovi.test.cobertura;

import com.uniovi.controllers.HomeController;
import com.uniovi.entities.Player;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    private Player player;

    @BeforeEach
    void setup() {
        player = new Player();
        player.setId(1L);
        player.setUsername("testUser");
    }

    @Test
    void home_ReturnsIndexView() {
        // Act
        String viewName = homeController.home();

        // Assert
        assertEquals("index", viewName);
    }

    @Test
    void game_ReturnsGameView() {
        // Act
        String viewName = homeController.game();

        // Assert
        assertEquals("player/game", viewName);
    }



    @Test
    void apiKeyHome_WithNonExistingPlayer_DoesNotAddApiKeyToModel() {
        // Arrange
        when(authentication.getName()).thenReturn("nonExistingUser");
        when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

        // Act
        String viewName = homeController.apiKeyHome(authentication, model);

        // Assert
        assertEquals("player/apiKeyHome", viewName);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void createApiKey_WithExistingPlayerWithoutApiKey_CreatesApiKey() {
        // Arrange
        player.setApiKey(null);
        when(authentication.getName()).thenReturn("testUser");
        when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));

        // Act
        String viewName = homeController.createApiKey(authentication);

        // Assert
        assertEquals("redirect:/home/apikey", viewName);
        verify(apiKeyService).createApiKey(player);
    }



    @Test
    void createApiKey_WithNonExistingPlayer_DoesNotCreateApiKey() {
        // Arrange
        when(authentication.getName()).thenReturn("nonExistingUser");
        when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

        // Act
        String viewName = homeController.createApiKey(authentication);

        // Assert
        assertEquals("redirect:/home/apikey", viewName);
        verify(apiKeyService, never()).createApiKey(any(Player.class));
    }

    @Test
    void instructions_ReturnsInstructionsView() {
        // Act
        String viewName = homeController.instructions();

        // Assert
        assertEquals("instructions", viewName);
    }

    @Test
    void authors_ReturnsAboutUsView() {
        // Act
        String viewName = homeController.authors();

        // Assert
        assertEquals("about-us", viewName);
    }
}