package com.uniovi.services.impl;

import com.uniovi.dto.PlayerDto;
import com.uniovi.dto.PlayerImageDto;
import com.uniovi.dto.RoleDto;
import com.uniovi.dto.RoleImageDto;
import com.uniovi.entities.*;
import com.uniovi.repositories.PlayerImageRepository;
import com.uniovi.repositories.PlayerRepository;
import com.uniovi.services.MultiplayerSessionService;
import com.uniovi.services.PlayerService;
import com.uniovi.services.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PlayerServiceImageImpl  {
    private final PlayerImageRepository playerRepository;
    private final RoleServiceImageImpl roleService;
    private final PasswordEncoder passwordEncoder;
    private final MultiplayerSessionImageImpl multiplayerSessionService;
    private final Random random = new SecureRandom();

    public PlayerServiceImageImpl(PlayerImageRepository playerRepository, RoleServiceImageImpl roleService, MultiplayerSessionImageImpl multiplayerSessionService, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.multiplayerSessionService = multiplayerSessionService;
    }


    public PlayerImage addNewPlayer(PlayerDto dto) {
        if (playerRepository.findByEmail(dto.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (playerRepository.findByUsername(dto.getUsername()) != null) {
            throw new IllegalArgumentException("Username already in use");
        }

        PlayerImage player = new PlayerImage(
            dto.getUsername(),
            dto.getEmail(),
            passwordEncoder.encode(dto.getPassword())
        );

        if (dto.getRoles() == null)
            dto.setRoles(new String[] {"ROLE_USER"} );

        for (String roleStr : dto.getRoles()) {
            RoleImage r = roleService.getRole(roleStr);
            if (r != null)
                Associations.PlayerImageRole.addRole(player, r);
            else {
                r = roleService.addRole(new RoleImageDto(roleStr));
                Associations.PlayerImageRole.addRole(player, r);
            }
        }

        playerRepository.save(player);
        return player;
    }


    public List<PlayerImage> getUsers() {
        List<PlayerImage> l = new ArrayList<>();
        playerRepository.findAll().forEach(l::add);
        return l;
    }


    public List<PlayerImage> getUsersByMultiplayerCode(int multiplayerCode) {
        List<PlayerImage> l = new ArrayList<>();
        playerRepository.findAllByMultiplayerCode(multiplayerCode).forEach(l::add);
        return l;
    }


    public Optional<PlayerImage> getUser(Long id) {
        return playerRepository.findById(id);
    }


    public Optional<PlayerImage> getUserByEmail(String email) {
        return Optional.ofNullable(playerRepository.findByEmail(email));
    }


    public Optional<PlayerImage> getUserByUsername(String username) {
        return Optional.ofNullable(playerRepository.findByUsername(username));
    }


    public List<PlayerImage> getUsersByRole(String role) {
        RoleImage r = roleService.getRole(role);
        if (r == null)
            return new ArrayList<>();

        return new ArrayList<>(r.getPlayers());
    }


    public void generateApiKey(PlayerImage player) {
        ApiKeyImage apiKey = new ApiKeyImage();
        Associations.PlayerImageApiKey.addApiKey(player, apiKey);
        playerRepository.save(player);
    }

    public void updatePlayer(Long id, PlayerImageDto playerImageDto) {
        Optional<PlayerImage> player = playerRepository.findById(id);
        if (player.isEmpty())
            return;

        PlayerImage p = player.get();
        if (playerImageDto.getEmail() != null)
            p.setEmail(playerImageDto.getEmail());
        if (playerImageDto.getUsername() != null)
            p.setUsername(playerImageDto.getUsername());
        if (playerImageDto.getPassword() != null)
            p.setPassword(passwordEncoder.encode(playerImageDto.getPassword()));
        if (playerImageDto.getRoles() != null) {
            p.getRoles().clear();
            for (String roleStr : playerImageDto.getRoles()) {
                RoleImage r = roleService.getRole(roleStr);
                if (r != null)
                    Associations.PlayerImageRole.addRole(p, r);
                else {
                    r = roleService.addRole(new RoleImageDto(roleStr));
                    Associations.PlayerImageRole.addRole(p, r);
                }
            }
        }

        playerRepository.save(p);
    }


    public boolean changeMultiplayerCode(Long id, String code) {
        Optional<PlayerImage> player = playerRepository.findById(id);
        if (player.isEmpty())
            return false;

        PlayerImage p = player.get();
        if(existsMultiplayerCode(code)){
            p.setMultiplayerCode(Integer.parseInt(code));
            playerRepository.save(p);
            return true;
        }
        return false;
    }

    public String getScoreMultiplayerCode(Long id) {
        Optional<PlayerImage> player = playerRepository.findById(id);
        if (player.isEmpty())
            return "";

        return player.get().getScoreMultiplayerCode();
    }


    public void setScoreMultiplayerCode(Long id, String score) {
        Optional<PlayerImage> player = playerRepository.findById(id);
        if (player.isEmpty())
            return;

        PlayerImage p =player.get();
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


    public int createMultiplayerGame(Long id){
        Optional<PlayerImage> player = playerRepository.findById(id);
        if (player.isEmpty())
            return -1;

        PlayerImage p = player.get();
        int code = random.nextInt(10000);
        p.setMultiplayerCode(code);
        playerRepository.save(p);
        return code;
    }


    public void deleteMultiplayerCode(Long id){
        Optional<PlayerImage> player = playerRepository.findById(id);
        if (player.isEmpty())
            return;

        PlayerImage p = player.get();
        p.setMultiplayerCode(null);
        playerRepository.save(p);
    }


    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }


    public Page<PlayerImage> getPlayersPage(Pageable pageable) {
        return playerRepository.findAll(pageable);
    }


    public void updatePassword(PlayerImage player, String password) {
        player.setPassword(passwordEncoder.encode(password));
        playerRepository.save(player);
    }


    public void savePlayer(PlayerImage player) {
        playerRepository.save(player);
    }
}
