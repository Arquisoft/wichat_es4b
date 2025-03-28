package com.uniovi.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uniovi.configuration.SecurityConfig;
import com.uniovi.dto.PlayerImageDto;
import com.uniovi.dto.RoleDto;
import com.uniovi.entities.Associations;
import com.uniovi.entities.GameSession;
import com.uniovi.entities.Player;
import com.uniovi.entities.Role;
import com.uniovi.services.*;
import com.uniovi.services.impl.PlayerServiceImageImpl;
import com.uniovi.validators.EditUserValidator;
import com.uniovi.validators.SignUpValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.uniovi.dto.PlayerDto;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.List;

@Controller
public class PlayersController {
    private final PlayerService playerService;
    private final RoleService roleService;
    private final PlayerServiceImageImpl playerServiceImageImpl;
    private final QuestionService questionService;
    private final SignUpValidator signUpValidator;
    private final EditUserValidator editUserValidator;
    private final GameSessionService gameSessionService;

    @Autowired
    public PlayersController(PlayerService playerService, SignUpValidator signUpValidator, GameSessionService gameSessionService,
                             RoleService roleService, QuestionService questionService, EditUserValidator editUserValidator, PlayerServiceImageImpl playerServiceImageImpl) {
        this.playerService = playerService;
        this.signUpValidator =  signUpValidator;
        this.gameSessionService = gameSessionService;
        this.roleService = roleService;
        this.questionService = questionService;
        this.editUserValidator = editUserValidator;
        this.playerServiceImageImpl = playerServiceImageImpl;
    }

    @RequestMapping("/signup")
    public String showRegistrationForm(Model model){
        if (SecurityConfig.isAuthenticated())
            return "redirect:/home";

        if (model.containsAttribute("user")) {
            model.addAttribute("user", model.getAttribute("user"));
            return "player/signup";
        }

        model.addAttribute("user", new PlayerDto());
        return "player/signup";
    }


    @RequestMapping(value = "player/details/{username}")
    public String getDetails(Model model, @PathVariable String username) {
        Optional<Player> player = playerService.getUserByUsername(username);
        if (player.isPresent()) {
            model.addAttribute("user", player.get());
            return "player/details";
        }
        return "/player/home";
    }

    @RequestMapping(value = "/player/edit/{username}")
    public String getEdit(Model model, @PathVariable String username) {
        Optional<Player> player = playerService.getUserByUsername(username);
        if (player.isPresent()) {
            model.addAttribute("user", player.get());
            return "player/edit";
        }
        return "/player/home";
    }

    @RequestMapping(value = "/player/edit/{username}", method = RequestMethod.POST)
    public String setEdit(@PathVariable String username, @Validated @ModelAttribute("user") PlayerDto user,
                          BindingResult result, Model model) {

        editUserValidator.setOriginalUsername(username);
        editUserValidator.validate(user, result);

        if(result.hasErrors()) {
            // Añade explícitamente el objeto user con el nombre "user"
            model.addAttribute("user", user);
            return "player/edit";
        }

        Optional<Player> originalUser = playerService.getUserByUsername(username);
        if (originalUser.isPresent()) {
            originalUser.get().setUsername(user.getUsername());
            originalUser.get().setEmail(user.getEmail());
            playerService.savePlayer(originalUser.get());
        }

        return "redirect:/logout";
    }

    @RequestMapping(value="/signup", method = RequestMethod.POST)
    public String registerUserAccount(HttpServletRequest request, @Validated @ModelAttribute("user") PlayerDto user, BindingResult result, Model model){
        if (SecurityConfig.isAuthenticated())
            return "redirect:/home";

        signUpValidator.validate(user, result);

        if(result.hasErrors()) {
            model.addAttribute("user", user);
            return "player/signup";
        }

        playerService.addNewPlayer(user);
        playerServiceImageImpl.addNewPlayer(user);

        try {
            request.login(user.getUsername(), user.getPassword());
        } catch (ServletException e) {
            return "redirect:/signup";
        }
        return "redirect:/home";
    }

    @RequestMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error,
                                HttpSession session) {
        if (error != null) {
            model.addAttribute("error", session.getAttribute("loginErrorMessage"));
        }

        if (SecurityConfig.isAuthenticated())
            return "redirect:/home";

        return "player/login";
    }

    @RequestMapping("/home")
    public String home(Pageable pageable,Model model) {
        Page<Object[]> ranking = gameSessionService.getGlobalRanking(pageable);

        model.addAttribute("ranking", ranking.getContent());
        model.addAttribute("page", ranking);
        model.addAttribute("num", pageable.getPageSize());

        return "player/home";
    }

    @RequestMapping("/ranking/global")
    public String showGlobalRanking(Pageable pageable, Model model) {
        Page<Object[]> ranking = gameSessionService.getGlobalRanking(pageable);

        model.addAttribute("ranking", ranking.getContent());
        model.addAttribute("page", ranking);
        model.addAttribute("num", pageable.getPageSize());

        return "ranking/globalRanking";
    }

    @RequestMapping("/ranking/local")
    public String showPlayerRanking(Pageable pageable, Model model, Principal principal) {
        Optional<Player> player = playerService.getUserByUsername(principal.getName());
        Player p = player.orElse(null);

        if (p == null) {
            return "redirect:/login";
        }

        Page<GameSession> ranking = gameSessionService.getPlayerRanking(pageable, p);

        model.addAttribute("ranking", ranking.getContent());
        model.addAttribute("page", ranking);
        model.addAttribute("num", pageable.getPageSize());

        return "ranking/playerRanking";
    }

    // ----- Admin endpoints -----

    @RequestMapping("/player/admin")
    public String showAdminPanel(Model model) {
        return "player/admin/admin";
    }

    @RequestMapping("/player/admin/userManagement")
    public String showUserManagementFragment(Model model, Pageable pageable) {
        model.addAttribute("endpoint", "/player/admin/userManagement");
        Page<Player> users = playerService.getPlayersPage(pageable);
        model.addAttribute("page", users);
        model.addAttribute("users", users.getContent());

        return "player/admin/userManagement";
    }

    @RequestMapping("/player/admin/deleteUser")
    @ResponseBody
    public String deleteUser(HttpServletResponse response, @RequestParam String username, Principal principal) {
        Player player = playerService.getUserByUsername(username).orElse(null);
        if (player == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "User not found";
        }

        if (principal.getName().equals(player.getUsername())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "You can't delete yourself";
        }

        playerService.deletePlayer(player.getId());
        return "User deleted";
    }

    @RequestMapping("/player/admin/changePassword")
    @ResponseBody
    public String changePassword(HttpServletResponse response, @RequestParam String username, @RequestParam String password) {
        Player player = playerService.getUserByUsername(username).orElse(null);
        if (player == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "User not found";
        }

        playerService.updatePassword(player, password);
        return "User password changed";
    }

    @RequestMapping("/player/admin/getRoles")
    @ResponseBody
    public String getRoles(@RequestParam String username) {
        List<Role> roles = roleService.getAllRoles();
        Player player = playerService.getUserByUsername(username).orElse(null);

        roles.remove(roleService.getRole("ROLE_USER"));

        if (player == null) {
            return "{}";
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rolesJson = mapper.createObjectNode();
        for (Role role : roles) {
            boolean hasRole = player.getRoles().contains(role);
            rolesJson.put(role.getName(), hasRole);
        }

        return rolesJson.toString();
    }

    @RequestMapping("/player/admin/changeRoles")
    @ResponseBody
    public String changeRoles(HttpServletResponse response, @RequestParam String username, @RequestParam String roles) {
        Player player = playerService.getUserByUsername(username).orElse(null);
        if (player == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "User not found";
        }

        JsonNode rolesJson;
        try {
            rolesJson = new ObjectMapper().readTree(roles);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Invalid roles";
        }

        rolesJson.fieldNames().forEachRemaining(roleName -> {
            boolean hasRole = rolesJson.get(roleName).asBoolean();

            Role role = roleService.getRole(roleName);
            if (role == null && !hasRole) {
                return;
            } else if (role == null) {
                role = roleService.addRole(new RoleDto(roleName));
            }

            if (hasRole) {
                Associations.PlayerRole.addRole(player, role);
            } else {
                Associations.PlayerRole.removeRole(player, role);
            }
        });

        playerService.savePlayer(player);
        return "User roles changed";
    }

    @RequestMapping("/player/admin/questionManagement")
    public String showQuestionManagementFragment(Model model) throws IOException {
        Resource jsonFile = new ClassPathResource(QuestionGeneratorService.JSON_FILE_PATH);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(jsonFile.getInputStream());
        model.addAttribute("jsonContent", json.toString());

        return "player/admin/questionManagement";
    }

    @RequestMapping("/player/admin/deleteAllQuestions")
    @ResponseBody
    public String deleteAllQuestions() throws IOException {
        questionService.deleteAllQuestions();
        return "Questions deleted";
    }

    @PutMapping("/player/admin/saveQuestions")
    @ResponseBody
    public String saveQuestions(HttpServletResponse response, @RequestBody String json) throws IOException {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            questionService.setJsonGenerator(node);
            return "Questions saved";
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return e.getMessage();
        }
    }

    @RequestMapping("/questions/getJson")
    @ResponseBody
    public String getJson() {
        return questionService.getJsonGenerator().toString();
    }

    @RequestMapping("/player/admin/monitoring")
    public String showMonitoring(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        return "player/admin/monitoring";
    }
}
