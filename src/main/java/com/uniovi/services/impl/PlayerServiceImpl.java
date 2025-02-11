package com.uniovi.services.impl;

import com.uniovi.dto.PlayerDto;
import com.uniovi.dto.RoleDto;
import com.uniovi.entities.ApiKey;
import com.uniovi.entities.Associations;
import com.uniovi.entities.Player;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.MultiplayerSessionService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.uniovi.entities.Role;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final MultiplayerSessionService multiplayerSessionService;
    private final Random random = new SecureRandom();

    public PlayerServiceImpl(PlayerRepository playerRepository, RoleService roleService, MultiplayerSessionService multiplayerSessionService,PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.multiplayerSessionService = multiplayerSessionService;
    }

    @Override
    public Player addNewPlayer(PlayerDto dto) {
        if (playerRepository.findByEmail(dto.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (playerRepository.findByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("Username already in use");
        }

        Player player = new Player(
            dto.getUsername(),
            dto.getEmail(),
            passwordEncoder.encode(dto.getPassword())
        );

        if (dto.getRoles() == null)
            dto.setRoles(new String[] {"ROLE_USER"} );

        for (String roleStr : dto.getRoles()) {
            Role r = roleService.getRole(roleStr);
            if (r != null)
                Associations.PlayerRole.addRole(player, r);
            else {
                r = roleService.addRole(new RoleDto(roleStr));
                Associations.PlayerRole.addRole(player, r);
            }
        }

        playerRepository.save(player);
        return player;
    }

    @Override
    public List<Player> getUsers() {
        List<Player> l = new ArrayList<>();
        playerRepository.findAll().forEach(l::add);
        return l;
    }

    @Override
    public List<Player> getUsersByMultiplayerCode(int multiplayerCode) {
        List<Player> l = new ArrayList<>();
        playerRepository.findAllByMultiplayerCode(multiplayerCode).forEach(l::add);
        return l;
    }

    @Override
    public Optional<Player> getUser(Long id) {
        return playerRepository.findById(id);
    }

    @Override
    public Optional<Player> getUserByEmail(String email) {
        return Optional.ofNullable(playerRepository.findByEmail(email));
    }

    @Override
    public Optional<Player> getUserByUsername(String username) {
        return Optional.ofNullable(playerRepository.findByUsername(username));
    }

    @Override
    public List<Player> getUsersByRole(String role) {
        Role r = roleService.getRole(role);
        if (r == null)
            return new ArrayList<>();

        return new ArrayList<>(r.getPlayers());
    }

    @Override
    public void generateApiKey(Player player) {
        ApiKey apiKey = new ApiKey();
        Associations.PlayerApiKey.addApiKey(player, apiKey);
        playerRepository.save(player);
    }

    @Override
    public void updatePlayer(Long id, PlayerDto playerDto) {
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty())
            return;

        Player p = player.get();
        if (playerDto.getEmail() != null)
            p.setEmail(playerDto.getEmail());
        if (playerDto.getUsername() != null)
            p.setUsername(playerDto.getUsername());
        if (playerDto.getPassword() != null)
            p.setPassword(passwordEncoder.encode(playerDto.getPassword()));
        if (playerDto.getRoles() != null) {
            p.getRoles().clear();
            for (String roleStr : playerDto.getRoles()) {
                Role r = roleService.getRole(roleStr);
                if (r != null)
                    Associations.PlayerRole.addRole(p, r);
                else {
                    r = roleService.addRole(new RoleDto(roleStr));
                    Associations.PlayerRole.addRole(p, r);
                }
            }
        }

        playerRepository.save(p);
    }

    @Override
    public boolean changeMultiplayerCode(Long id, String code) {
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty())
            return false;

        Player p = player.get();
        if(existsMultiplayerCode(code)){
            p.setMultiplayerCode(Integer.parseInt(code));
            playerRepository.save(p);
            return true;
        }
        return false;
    }
    @Override
    public String getScoreMultiplayerCode(Long id) {
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty())
            return "";

        return player.get().getScoreMultiplayerCode();
    }

    @Override
    public void setScoreMultiplayerCode(Long id, String score) {
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty())
            return;

        Player p =player.get();
        p.setScoreMultiplayerCode(score);
        playerRepository.save(p);
    }
    /**
    * A multiplayerCodeExists if there are any player
     * with same multiplayerCode at the moment of the join
    * */
    private boolean existsMultiplayerCode(String code){
        return ! multiplayerSessionService.getPlayersWithScores(Integer.parseInt(code)).isEmpty();
    }

    @Override
    public int createMultiplayerGame(Long id){
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty())
            return -1;

        Player p = player.get();
        int code = random.nextInt(10000);
        p.setMultiplayerCode(code);
        playerRepository.save(p);
        return code;
    }

    @Override
    public void deleteMultiplayerCode(Long id){
        Optional<Player> player = playerRepository.findById(id);
        if (player.isEmpty())
            return;

        Player p = player.get();
        p.setMultiplayerCode(null);
        playerRepository.save(p);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public Page<Player> getPlayersPage(Pageable pageable) {
        return playerRepository.findAll(pageable);
    }

    @Override
    public void updatePassword(Player player, String password) {
        player.setPassword(passwordEncoder.encode(password));
        playerRepository.save(player);
    }

    @Override
    public void savePlayer(Player player) {
        playerRepository.save(player);
    }
}
