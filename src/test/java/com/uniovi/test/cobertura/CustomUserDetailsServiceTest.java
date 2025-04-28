package com.uniovi.test.cobertura;

import com.uniovi.entities.Player;
import com.uniovi.entities.Role;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

	private PlayerRepository playerRepository;
	private CustomUserDetailsService customUserDetailsService;

	@BeforeEach
	void setUp() {
		playerRepository         = mock(PlayerRepository.class);
		customUserDetailsService = new CustomUserDetailsService(playerRepository);
	}

	@Test
	void loadUserByUsername_UserExists_ReturnsUserDetails() {
		// Arrange
		Player player = new Player();
		player.setUsername("testuser");
		player.setPassword("password123");

		Role role = new Role();
		role.setName("ROLE_USER");

		Set<Role> roles = new HashSet<>();
		roles.add(role);
		player.setRoles(roles);

		when(playerRepository.findByUsername("testuser")).thenReturn(player);

		// Act
		UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

		// Assert
		assertNotNull(userDetails);
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password123", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().stream()
						   .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

		verify(playerRepository, times(1)).findByUsername("testuser");
	}

	@Test
	void loadUserByUsername_UserDoesNotExist_ThrowsException() {
		// Arrange
		when(playerRepository.findByUsername("nonexistent")).thenReturn(null);

		// Act & Assert
		UsernameNotFoundException exception = assertThrows(
				UsernameNotFoundException.class, () -> {
					customUserDetailsService.loadUserByUsername("nonexistent");
				});

		assertEquals("Invalid username or password.", exception.getMessage());
		verify(playerRepository, times(1)).findByUsername("nonexistent");
	}
}
