package com.uniovi.controllers;



import com.uniovi.entities.User;
import com.uniovi.services.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @Autowired
    private LlmService llmService;

    @Value("${llm.api.key}")
    private String apiKey;

    @GetMapping("/llm/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "llm/login";
    }

    @PostMapping("/llm/login")
    public String loginUser(User user, Model model) {
        // Validación del usuario (esto debería ser más robusto)
        if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            model.addAttribute("error", "Username or Password is required");
            return "llm/login";
        }

        // Llamada al servicio LLM para generar el mensaje
        String question = "Please, generate a greeting message for " + user.getUsername();
        String greetingMessage = llmService.getLLMResponse(question, apiKey,"empathy");

        model.addAttribute("greetingMessage", greetingMessage);
        model.addAttribute("username", user.getUsername());

        return "llm/login"; // Regresar a la misma página con el mensaje
    }
}
