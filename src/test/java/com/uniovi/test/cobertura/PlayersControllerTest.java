package com.uniovi.test.cobertura;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.configuration.SecurityConfig;
import com.uniovi.controllers.PlayersController;
import com.uniovi.dto.PlayerDto;
import com.uniovi.entities.GameSession;
import com.uniovi.entities.GameSessionImage;
import com.uniovi.entities.Player;
import com.uniovi.entities.Role;
import com.uniovi.services.PlayerService;
import com.uniovi.services.RoleService;
import com.uniovi.services.impl.GameSessionImageServiceImpl;
import com.uniovi.services.impl.GameSessionServiceImpl;
import com.uniovi.services.impl.QuestionServiceImpl;
import com.uniovi.validators.EditUserValidator;
import com.uniovi.validators.SignUpValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayersControllerTest {

	@Mock
	private PlayerService playerService;

	@Mock
	private RoleService roleService;

	@Mock
	private GameSessionServiceImpl gameSessionService;

	@Mock
	private GameSessionImageServiceImpl gameSessionImageService;

	@Mock
	private QuestionServiceImpl questionService;

	@Mock
	private EditUserValidator editUserValidator;

	@Mock
	private SignUpValidator signUpValidator;

	@Mock
	private Model model;

	@Mock
	private HttpSession session;

	@Mock
	private Principal principal;

	@Mock
	private BindingResult bindingResult;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private CsrfToken csrfToken;

	@Mock
	private Pageable pageable;

	@Mock
	private JsonNode jsonNode;

	@InjectMocks
	private PlayersController playersController;

	private Player player;
	private PlayerDto playerDto;
	private Role userRole, adminRole;
	private List<Object[]> rankingData;
	private Page<Object[]> rankingPage;
	private Page<GameSession> playerRankingPage;
	private Page<GameSessionImage> playerImageRankingPage;

	@BeforeEach
	void setup() {
		// Setup player
		player = new Player();
		player.setId(1L);
		player.setUsername("testUser");
		player.setEmail("test@example.com");

		// Setup playerDto
		playerDto = new PlayerDto();
		playerDto.setUsername("testUser");
		playerDto.setEmail("test@example.com");
		playerDto.setPassword("Password123");

		// Setup roles
		userRole = new Role();
		userRole.setName("ROLE_USER");

		adminRole = new Role();
		adminRole.setName("ROLE_ADMIN");

		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		player.setRoles(roles);

		// Setup ranking data
		rankingData = new ArrayList<>();
		rankingData.add(new Object[]{"testUser", 100});
		rankingData.add(new Object[]{"anotherUser", 50});
		rankingPage = new PageImpl<>(rankingData);

		// Setup player ranking
		List<GameSession> playerRanking = new ArrayList<>();
		GameSession gameSession = new GameSession();
		gameSession.setId(1L);
		gameSession.setScore(100);
		gameSession.setPlayer(player);
		playerRanking.add(gameSession);
		playerRankingPage = new PageImpl<>(playerRanking);

		// Setup player image ranking
		List<GameSessionImage> playerImageRanking = new ArrayList<>();
		GameSessionImage gameSessionImage = new GameSessionImage();
		gameSessionImage.setId(1L);
		gameSessionImage.setScore(100);
		gameSessionImage.setPlayer(player);
		playerImageRanking.add(gameSessionImage);
		playerImageRankingPage = new PageImpl<>(playerImageRanking);
	}

	@Test
	void showRegistrationForm_WhenAuthenticated_RedirectsToHome() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(true);

			// Act
			String viewName = playersController.showRegistrationForm(model, session);

			// Assert
			assertEquals("redirect:/home", viewName);
		}
	}

	@Test
	void showRegistrationForm_WhenNotAuthenticated_NoUserInModel_ReturnsSignupView() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);
			when(model.containsAttribute("user")).thenReturn(false);
			when(session.getAttribute("_csrf")).thenReturn(csrfToken);

			// Act
			String viewName = playersController.showRegistrationForm(model, session);

			// Assert
			assertEquals("player/signup", viewName);
			verify(model).addAttribute(eq("user"), any(PlayerDto.class));
			verify(model).addAttribute(eq("_csrf"), eq(csrfToken));
		}
	}

	@Test
	void showRegistrationForm_WhenNotAuthenticated_UserInModel_ReturnsSignupView() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);
			when(model.containsAttribute("user")).thenReturn(true);
			when(model.getAttribute("user")).thenReturn(playerDto);

			// Act
			String viewName = playersController.showRegistrationForm(model, session);

			// Assert
			assertEquals("player/signup", viewName);
			verify(model).addAttribute(eq("user"), eq(playerDto));
		}
	}

	@Test
	void getDetails_WhenPlayerExists_ReturnsDetailsView() {
		// Arrange
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));

		// Act
		String viewName = playersController.getDetails(model, "testUser");

		// Assert
		assertEquals("player/details", viewName);
		verify(model).addAttribute(eq("user"), eq(player));
	}

	@Test
	void getDetails_WhenPlayerDoesNotExist_ReturnsHomeView() {
		// Arrange
		when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

		// Act
		String viewName = playersController.getDetails(model, "nonExistingUser");

		// Assert
		assertEquals("/player/home", viewName);
	}

	@Test
	void getEdit_WhenPlayerExists_ReturnsEditView() {
		// Arrange
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));

		// Act
		String viewName = playersController.getEdit(model, "testUser");

		// Assert
		assertEquals("player/edit", viewName);
		verify(model).addAttribute(eq("user"), eq(player));
	}

	@Test
	void getEdit_WhenPlayerDoesNotExist_ReturnsHomeView() {
		// Arrange
		when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

		// Act
		String viewName = playersController.getEdit(model, "nonExistingUser");

		// Assert
		assertEquals("/player/home", viewName);
	}

	@Test
	void setEdit_WhenValidationFails_ReturnsEditView() {
		// Arrange
		when(bindingResult.hasErrors()).thenReturn(true);

		// Act
		String viewName = playersController.setEdit("testUser", playerDto, bindingResult, model);

		// Assert
		assertEquals("player/edit", viewName);
		verify(editUserValidator).setOriginalUsername("testUser");
		verify(editUserValidator).validate(eq(playerDto), eq(bindingResult));
		verify(model).addAttribute(eq("user"), eq(playerDto));
	}

	@Test
	void setEdit_WhenValidationPasses_RedirectsToLogout() {
		// Arrange
		when(bindingResult.hasErrors()).thenReturn(false);
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));

		// Act
		String viewName = playersController.setEdit("testUser", playerDto, bindingResult, model);

		// Assert
		assertEquals("redirect:/logout", viewName);
		verify(editUserValidator).setOriginalUsername("testUser");
		verify(editUserValidator).validate(eq(playerDto), eq(bindingResult));
		verify(playerService).savePlayer(player);
	}

	@Test
	void registerUserAccount_WhenAuthenticated_RedirectsToHome() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(true);

			// Act
			String viewName = playersController.registerUserAccount(request, playerDto, bindingResult, model);

			// Assert
			assertEquals("redirect:/home", viewName);
		}
	}

	@Test
	void registerUserAccount_WhenValidationFails_ReturnsSignupView() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);
			when(bindingResult.hasErrors()).thenReturn(true);

			// Act
			String viewName = playersController.registerUserAccount(request, playerDto, bindingResult, model);

			// Assert
			assertEquals("player/signup", viewName);
			verify(signUpValidator).validate(eq(playerDto), eq(bindingResult));
			verify(model).addAttribute(eq("user"), eq(playerDto));
		}
	}

	@Test
	void registerUserAccount_WhenValidationPasses_LoginSucceeds_RedirectsToHome() throws ServletException {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);
			when(bindingResult.hasErrors()).thenReturn(false);

			// Act
			String viewName = playersController.registerUserAccount(request, playerDto, bindingResult, model);

			// Assert
			assertEquals("redirect:/home", viewName);
			verify(signUpValidator).validate(eq(playerDto), eq(bindingResult));
			verify(playerService).addNewPlayer(playerDto);
			verify(request).login(playerDto.getUsername(), playerDto.getPassword());
		}
	}

	@Test
	void registerUserAccount_WhenValidationPasses_LoginFails_RedirectsToSignup() throws ServletException {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);
			when(bindingResult.hasErrors()).thenReturn(false);
			doThrow(new ServletException("Login failed")).when(request).login(anyString(), anyString());

			// Act
			String viewName = playersController.registerUserAccount(request, playerDto, bindingResult, model);

			// Assert
			assertEquals("redirect:/signup", viewName);
			verify(signUpValidator).validate(eq(playerDto), eq(bindingResult));
			verify(playerService).addNewPlayer(playerDto);
		}
	}

	@Test
	void showLoginForm_WhenAuthenticated_RedirectsToHome() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(true);

			// Act
			String viewName = playersController.showLoginForm(model, null, session);

			// Assert
			assertEquals("redirect:/home", viewName);
		}
	}

	@Test
	void showLoginForm_WhenNotAuthenticated_NoError_ReturnsLoginView() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);

			// Act
			String viewName = playersController.showLoginForm(model, null, session);

			// Assert
			assertEquals("player/login", viewName);
			verify(model, never()).addAttribute(eq("error"), any());
		}
	}

	@Test
	void showLoginForm_WhenNotAuthenticated_WithError_ReturnsLoginViewWithError() {
		try (MockedStatic<SecurityConfig> securityConfigMock = mockStatic(SecurityConfig.class)) {
			// Arrange
			securityConfigMock.when(SecurityConfig::isAuthenticated).thenReturn(false);
			when(session.getAttribute("loginErrorMessage")).thenReturn("Invalid credentials");

			// Act
			String viewName = playersController.showLoginForm(model, "true", session);

			// Assert
			assertEquals("player/login", viewName);
			verify(model).addAttribute(eq("error"), eq("Invalid credentials"));
		}
	}

	@Test
	void home_ShowsGlobalRanking() {
		// Arrange
		when(gameSessionService.getGlobalRanking(pageable)).thenReturn(rankingPage);

		// Act
		String viewName = playersController.home(pageable, model);

		// Assert
		assertEquals("player/home", viewName);
		verify(model).addAttribute(eq("ranking"), eq(rankingData));
		verify(model).addAttribute(eq("page"), eq(rankingPage));
		verify(model).addAttribute(eq("num"), any());
	}

	@Test
	void showGlobalRanking_ShowsGlobalRanking() {
		// Arrange
		when(gameSessionService.getGlobalRanking(pageable)).thenReturn(rankingPage);

		// Act
		String viewName = playersController.showGlobalRanking(pageable, model);

		// Assert
		assertEquals("ranking/globalRanking", viewName);
		verify(model).addAttribute(eq("ranking"), eq(rankingData));
		verify(model).addAttribute(eq("page"), eq(rankingPage));
		verify(model).addAttribute(eq("num"), any());
	}

	@Test
	void showGlobalImageRanking_ShowsGlobalImageRanking() {
		// Arrange
		when(gameSessionImageService.getGlobalRanking(pageable)).thenReturn(rankingPage);

		// Act
		String viewName = playersController.showGlobalImageRanking(pageable, model);

		// Assert
		assertEquals("ranking/globalRanking", viewName);
		verify(model).addAttribute(eq("ranking"), eq(rankingData));
		verify(model).addAttribute(eq("page"), eq(rankingPage));
		verify(model).addAttribute(eq("num"), any());
	}

	@Test
	void showPlayerRanking_WhenPlayerNotFound_RedirectsToLogin() {
		// Arrange
		when(principal.getName()).thenReturn("testUser");
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.empty());

		// Act
		String viewName = playersController.showPlayerRanking(pageable, model, principal);

		// Assert
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void showPlayerRanking_WhenPlayerFound_ShowsPlayerRanking() {
		// Arrange
		when(principal.getName()).thenReturn("testUser");
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));
		when(gameSessionService.getPlayerRanking(pageable, player)).thenReturn(playerRankingPage);

		// Act
		String viewName = playersController.showPlayerRanking(pageable, model, principal);

		// Assert
		assertEquals("ranking/playerRanking", viewName);
		verify(model).addAttribute(eq("ranking"), eq(playerRankingPage.getContent()));
		verify(model).addAttribute(eq("page"), eq(playerRankingPage));
		verify(model).addAttribute(eq("num"), any());
	}

	@Test
	void showPlayerImageRanking_WhenPlayerNotFound_RedirectsToLogin() {
		// Arrange
		when(principal.getName()).thenReturn("testUser");
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.empty());

		// Act
		String viewName = playersController.showPlayerImageRanking(pageable, model, principal);

		// Assert
		assertEquals("redirect:/login", viewName);
	}

	@Test
	void showPlayerImageRanking_WhenPlayerFound_ShowsPlayerImageRanking() {
		// Arrange
		when(principal.getName()).thenReturn("testUser");
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));
		when(gameSessionImageService.getPlayerRanking(pageable, player)).thenReturn(playerImageRankingPage);

		// Act
		String viewName = playersController.showPlayerImageRanking(pageable, model, principal);

		// Assert
		assertEquals("ranking/playerRanking", viewName);
		verify(model).addAttribute(eq("ranking"), eq(playerImageRankingPage.getContent()));
		verify(model).addAttribute(eq("page"), eq(playerImageRankingPage));
		verify(model).addAttribute(eq("num"), any());
	}

	@Test
	void showAdminPanel_ReturnsAdminView() {
		// Act
		String viewName = playersController.showAdminPanel();

		// Assert
		assertEquals("player/admin/admin", viewName);
	}

	@Test
	void showUserManagementFragment_ShowsUserManagement() {
		// Arrange
		List<Player> players = Collections.singletonList(player);
		Page<Player> playerPage = new PageImpl<>(players);
		when(playerService.getPlayersPage(pageable)).thenReturn(playerPage);

		// Act
		String viewName = playersController.showUserManagementFragment(model, pageable);

		// Assert
		assertEquals("player/admin/userManagement", viewName);
		verify(model).addAttribute(eq("endpoint"), eq("/player/admin/userManagement"));
		verify(model).addAttribute(eq("page"), eq(playerPage));
		verify(model).addAttribute(eq("users"), eq(players));
	}

	@Test
	void deleteUser_WhenUserNotFound_ReturnsUserNotFoundMessage() {
		// Arrange
		when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

		// Act
		String result = playersController.deleteUser(response, "nonExistingUser", principal);

		// Assert
		assertEquals("User not found", result);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	void deleteUser_WhenAttemptingToDeleteSelf_ReturnsForbiddenMessage() {
		// Arrange
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));
		when(principal.getName()).thenReturn("testUser");

		// Act
		String result = playersController.deleteUser(response, "testUser", principal);

		// Assert
		assertEquals("You can't delete yourself", result);
		verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
	}

	@Test
	void deleteUser_WhenValidUser_DeletesUser() {
		// Arrange
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));
		when(principal.getName()).thenReturn("adminUser");

		// Act
		String result = playersController.deleteUser(response, "testUser", principal);

		// Assert
		assertEquals("User deleted", result);
		verify(playerService).deletePlayer(player.getId());
	}

	@Test
	void changePassword_WhenUserNotFound_ReturnsUserNotFoundMessage() {
		// Arrange
		when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

		// Act
		String result = playersController.changePassword(response, "nonExistingUser", "newPassword");

		// Assert
		assertEquals("User not found", result);
		verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	@Test
	void changePassword_WhenUserExists_ChangesPassword() {
		// Arrange
		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));

		// Act
		String result = playersController.changePassword(response, "testUser", "newPassword");

		// Assert
		assertEquals("User password changed", result);
		verify(playerService).updatePassword(player, "newPassword");
	}

	@Test
	void getRoles_WhenUserNotFound_ReturnsEmptyJson() {
		// Arrange
		when(playerService.getUserByUsername("nonExistingUser")).thenReturn(Optional.empty());

		// Act
		String result = playersController.getRoles("nonExistingUser");

		// Assert
		assertEquals("{}", result);
	}
}