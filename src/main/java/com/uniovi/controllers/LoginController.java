package com.uniovi.controllers;



import com.uniovi.entities.Userllm;
import com.uniovi.services.LlmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private LlmService llmService;

    @Value("${llm.api.key}")
    private String apiKey;

    @RequestMapping("/llm/login")
    public String showLoginForm() {
        return "llm/login";
    }

    @RequestMapping(value="/llm/login", method= RequestMethod.POST)
    public String loginUser(@ModelAttribute Userllm userllm, Model model) {
        // Validación del usuario (esto debería ser más robusto)
        if (userllm.getUsername().isEmpty() || userllm.getPassword().isEmpty()) {
            model.addAttribute("userllm", new Userllm());
            return "llm/login";
        }

        // Llamada al servicio LLM para generar el mensaje
        String question = "Please, generate a greeting message for " + userllm.getUsername();
        String greetingMessage = llmService.getLLMResponse(question, apiKey,"empathy");

        model.addAttribute("greetingMessage", greetingMessage);
        model.addAttribute("username", userllm.getUsername());

        return "llm/login"; // Regresar a la misma página con el mensaje
    }
}
