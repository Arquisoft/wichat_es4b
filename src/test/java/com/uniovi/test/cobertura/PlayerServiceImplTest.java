package com.uniovi.test.cobertura;

import com.uniovi.dto.PlayerDto;
import com.uniovi.dto.RoleDto;
import com.uniovi.entities.Player;
import com.uniovi.entities.Role;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.RoleService;
import com.uniovi.services.impl.MultiplayerSessionServiceImpl;
import com.uniovi.services.impl.PlayerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private MultiplayerSessionServiceImpl multiplayerSessionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PlayerServiceImpl playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addNewPlayer_Success() {
        // Arrange
        PlayerDto dto = new PlayerDto("username", "email@test.com", "password","password", null);
        when(playerRepository.findByEmail(dto.getEmail())).thenReturn(null);
        when(playerRepository.findByUsername(dto.getUsername())).thenReturn(null);
        Role role = new Role("ROLE_USER");
        when(roleService.getRole("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        Player player = playerService.addNewPlayer(dto);

        // Assert
        assertEquals(dto.getUsername(), player.getUsername());
        assertEquals(dto.getEmail(), player.getEmail());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void addNewPlayer_EmailAlreadyInUse_ThrowsException() {
        // Arrange
        PlayerDto dto = new PlayerDto("username", "email@test.com", "password","password", null);
        when(playerRepository.findByEmail(dto.getEmail())).thenReturn(new Player());

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> playerService.addNewPlayer(dto));
    }

    @Test
    void addNewPlayer_UsernameAlreadyInUse_ThrowsException() {
        // Arrange
        PlayerDto dto = new PlayerDto("username", "email@test.com", "password","password", null);
        when(playerRepository.findByEmail(dto.getEmail())).thenReturn(null);
        when(playerRepository.findByUsername(dto.getUsername())).thenReturn(new Player());

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> playerService.addNewPlayer(dto));
    }

    @Test
    void getUsers_ReturnsListOfPlayers() {
        // Arrange
        List<Player> players = List.of(new Player(), new Player());
        when(playerRepository.findAll()).thenReturn(players);

        // Act
        List<Player> result = playerService.getUsers();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void getUser_ReturnsOptionalPlayer() {
        // Arrange
        Player player = new Player();
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        // Act
        Optional<Player> result = playerService.getUser(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(player, result.get());
    }

    @Test
    void getUserByEmail_ReturnsPlayer() {
        // Arrange
        Player player = new Player();
        when(playerRepository.findByEmail("email@test.com")).thenReturn(player);

        // Act
        Optional<Player> result = playerService.getUserByEmail("email@test.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(player, result.get());
    }

    @Test
    void getUserByUsername_ReturnsPlayer() {
        // Arrange
        Player player = new Player();
        when(playerRepository.findByUsername("username")).thenReturn(player);

        // Act
        Optional<Player> result = playerService.getUserByUsername("username");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(player, result.get());
    }

    @Test
    void getUsersByRole_RoleExists_ReturnsPlayers() {
        // Arrange
        Role role = new Role("ROLE_USER");
        Player player = new Player();
        role.getPlayers().add(player);
        when(roleService.getRole("ROLE_USER")).thenReturn(role);

        // Act
        List<Player> result = playerService.getUsersByRole("ROLE_USER");

        // Assert
        assertEquals(1, result.size());
        assertEquals(player, result.get(0));
    }

    @Test
    void getUsersByRole_RoleNotExists_ReturnsEmptyList() {
        // Arrange
        when(roleService.getRole("ROLE_UNKNOWN")).thenReturn(null);

        // Act
        List<Player> result = playerService.getUsersByRole("ROLE_UNKNOWN");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void updatePlayer_UpdatesFields() {
        // Arrange
        PlayerDto dto = new PlayerDto("newUsername", "newemail@test.com", "newPassword","password", new String[]{"ROLE_USER"});
        Player player = new Player();
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        Role role = new Role("ROLE_USER");
        when(roleService.getRole("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        playerService.updatePlayer(1L, dto);

        // Assert
        assertEquals("newUsername", player.getUsername());
        assertEquals("newemail@test.com", player.getEmail());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void changeMultiplayerCode_WhenCodeDoesNotExist_ReturnsFalse() {
        // Arrange
        Player player = new Player();
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        // Act
        boolean result = playerService.changeMultiplayerCode(1L, "1234");

        // Assert
        assertFalse(result);
        verify(playerRepository, times(0)).save(player);
    }

    @Test
    void createMultiplayerGame_ReturnsGeneratedCode() {
        // Arrange
        Player player = new Player();
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        // Act
        int code = playerService.createMultiplayerGame(1L);

        // Assert
        assertTrue(code >= 0 && code < 10000);
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void deleteMultiplayerCode_RemovesCode() {
        // Arrange
        Player player = new Player();
        player.setMultiplayerCode(1234);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

        // Act
        playerService.deleteMultiplayerCode(1L);

        // Assert
        assertNull(player.getMultiplayerCode());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void deletePlayer_CallsRepositoryDelete() {
        // Act
        playerService.deletePlayer(1L);

        // Assert
        verify(playerRepository, times(1)).deleteById(1L);
    }

    @Test
    void updatePassword_EncodesAndSaves() {
        // Arrange
        Player player = new Player();
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // Act
        playerService.updatePassword(player, "newPass");

        // Assert
        assertEquals("encodedNewPass", player.getPassword());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void getPlayersPage_ReturnsPage() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<Player> page = new PageImpl<>(Collections.emptyList());
        when(playerRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Player> result = playerService.getPlayersPage(pageable);

        // Assert
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void savePlayer_CallsRepositorySave() {
        // Arrange
        Player player = new Player();

        // Act
        playerService.savePlayer(player);

        // Assert
        verify(playerRepository, times(1)).save(player);
    }
}