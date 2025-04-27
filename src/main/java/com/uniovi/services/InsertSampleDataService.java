package com.uniovi.services;

import com.uniovi.dto.PlayerDto;
import jakarta.transaction.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class InsertSampleDataService {
	private final PlayerService playerService;
	private final Environment environment;

	public InsertSampleDataService(PlayerService playerService, Environment environment) {
		this.playerService = playerService;
		this.environment   = environment;
	}

	@Transactional
	@EventListener(ApplicationReadyEvent.class)
	// Uncomment this line to insert sample data on startup
	public void insertSampleQuestions() {
		if (playerService.getUserByEmail("test@test.com").isEmpty()) {
			PlayerDto player = new PlayerDto();
			player.setEmail("test@test.com");
			player.setUsername("test");
			player.setPassword("test");
			if (Arrays.asList(environment.getActiveProfiles())
					.contains("test")) player.setRoles(
					new String[]{"ROLE_USER", "ROLE_ADMIN"});
			else player.setRoles(new String[]{"ROLE_USER"});
			playerService.generateApiKey(playerService.addNewPlayer(player));
		}

		if (playerService.getUserByEmail("admin@wichat.es").isEmpty()) {
			PlayerDto player = new PlayerDto();
			player.setEmail("admin@wichat.es");
			player.setUsername("admin");
			player.setPassword("adminWichat2025ASW");
			player.setRoles(new String[]{"ROLE_ADMIN"});
			playerService.generateApiKey(playerService.addNewPlayer(player));
		}
	}

}