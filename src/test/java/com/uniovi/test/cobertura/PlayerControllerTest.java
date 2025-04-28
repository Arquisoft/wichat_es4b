package com.uniovi.test.cobertura;

import com.uniovi.controllers.PlayersController;
import com.uniovi.entities.Player;
import com.uniovi.services.PlayerService;
import com.uniovi.services.RoleService;
import com.uniovi.services.impl.GameSessionImageServiceImpl;
import com.uniovi.services.impl.GameSessionServiceImpl;
import com.uniovi.services.impl.QuestionServiceImpl;
import com.uniovi.validators.EditUserValidator;
import com.uniovi.validators.SignUpValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlayersControllerTest {

	private MockMvc mockMvc;
	private PlayersController playersController;

	@Mock
	private PlayerService playerService;

	@Mock
	private RoleService roleService;

	@Mock
	private GameSessionServiceImpl gameSessionService;

	@Mock
	private GameSessionImageServiceImpl gameSessionImageServiceImpl;

	@Mock
	private QuestionServiceImpl questionService;

	@Mock
	private EditUserValidator editUserValidator;

	@Mock
	private SignUpValidator signUpValidator;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		playersController = new PlayersController(playerService, signUpValidator,
												  gameSessionService, roleService,
												  questionService, editUserValidator,
												  gameSessionImageServiceImpl);
		mockMvc           = MockMvcBuilders.standaloneSetup(playersController).build();
	}

	@Test
	void testShowRegistrationForm() throws Exception {
		mockMvc.perform(get("/signup")).andExpect(status().isOk())
				.andExpect(view().name("player/signup"))
				.andExpect(model().attributeExists("user"));
	}

	@Test
	void testGetDetails_UserExists() throws Exception {
		Player player = new Player();
		player.setUsername("testUser");

		when(playerService.getUserByUsername("testUser")).thenReturn(Optional.of(player));

		mockMvc.perform(get("/player/details/testUser")).andExpect(status().isOk())
				.andExpect(view().name("player/details"))
				.andExpect(model().attributeExists("user"));
	}

	@Test
	void testGetDetails_UserNotFound() throws Exception {
		when(playerService.getUserByUsername("unknownUser")).thenReturn(Optional.empty());

		mockMvc.perform(get("/player/details/unknownUser")).andExpect(status().isOk())
				.andExpect(view().name("/player/home"));
	}

	@Test
	void testShowLoginForm() throws Exception {
		mockMvc.perform(get("/login")).andExpect(status().isOk())
				.andExpect(view().name("player/login"));
	}
}
