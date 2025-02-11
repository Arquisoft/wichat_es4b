package com.uniovi.services;

import com.uniovi.dto.PlayerDto;
import com.uniovi.entities.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface PlayerService {
    /**
     * Add a new player to the database
     * @param dto PlayerDto with the information of the new player
     * @return The new player
     * @throws IllegalArgumentException if the email or nickname are already in use
     */
    Player addNewPlayer(PlayerDto dto);

    /**
     * Get all the players in the database
     * @return A list with all the players
     */
    List<Player> getUsers();


    /**
     * Get all the players in the database with same multiplayerCode
     * @return A list with  the players
     */
    List<Player> getUsersByMultiplayerCode(int multiplayerCode);

    /**
     * Get a player by its id
     * @param id The id of the player
     * @return The player with the given id
     */
    Optional<Player> getUser(Long id);

    /**
     * Get a player by its email
     * @param email The email of the player
     * @return The player with the given email
     */
    Optional<Player> getUserByEmail(String email);

    /**
     * Get a player by its username
     * @param username The nickname of the player
     * @return The player with the given nickname
     */
    Optional<Player> getUserByUsername(String username);

    /**
     * Get all the players with a given role
     * @param role The role to filter the players
     * @return A list with all the players with the given role
     */
    List<Player> getUsersByRole(String role);

    /**
     * Generate an API key for a player
     * @param player The player to generate the API key for
     */
    void generateApiKey(Player player);

    /**
     * Update the information of a player
     * @param id The id of the player to update
     * @param playerDto The new information of the player
     */
    void updatePlayer(Long id, PlayerDto playerDto);

    /**
     * Update the multiplayerCode of a player
     * @param id The id of the player to update
     * @param code The new multiplayerCode of the player
     */
    boolean changeMultiplayerCode(Long id, String code);

    String getScoreMultiplayerCode(Long id);

    void setScoreMultiplayerCode(Long id, String score);

    int createMultiplayerGame(Long id);

    void deleteMultiplayerCode(Long id);

    /**
     * Delete a player from the database
     * @param id The id of the player to delete
     */
    void deletePlayer(Long id);

    /**
     * Get a page with all the players in the database
     * @param pageable The page information
     * @return A page with all the players
     */
    Page<Player> getPlayersPage(Pageable pageable);

    /**
     * Update the password of a player
     * @param player The player to update the password
     * @param password The new password
     */
    void updatePassword(Player player, String password);

    /**
     * Save a player in the database
     * @param player The player to save
     */
    void savePlayer(Player player);
}
