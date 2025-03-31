package com.uniovi.controllers;

import com.uniovi.dto.QuestionBaseDto;
import com.uniovi.entities.Player;
import com.uniovi.entities.QuestionBase;
import com.uniovi.services.ApiKeyService;
import com.uniovi.services.impl.PlayerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
public class HomeController<T extends QuestionBase, P extends QuestionBaseDto> {
    private final PlayerServiceImpl<T,P> playerService;
    private final ApiKeyService apiKeyService;


    @Autowired
    public HomeController(PlayerServiceImpl<T,P> playerService, ApiKeyService apiKeyService) {
        this.playerService = playerService;
        this.apiKeyService = apiKeyService;

    }

    @RequestMapping("/")
    public String home(){
        return "index";
    }

    @RequestMapping("/game")
    public String game(){
        return "player/game";
    }

    @RequestMapping("/home/apikey")
    public String apiKeyHome(Authentication auth, Model model) {
        Optional<Player> playerOpt = playerService.getUserByUsername(auth.getName());
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            model.addAttribute("apiKey", player.getApiKey());
        }
        return "player/apiKeyHome";
    }

    @RequestMapping("/home/apikey/create")
    public String createApiKey(Authentication auth) {
        if (playerService.getUserByUsername(auth.getName()).isPresent()) {
            Player player = playerService.getUserByUsername(auth.getName()).get();
            if (player.getApiKey() == null) {
                apiKeyService.createApiKey(player);
            }
        }
        return "redirect:/home/apikey";
    }

    @RequestMapping("/instructions")
    public String instructions(){
        return "instructions";
    }

    @RequestMapping("/about-us")
    public String authors(){
        return "about-us";
    }

}
