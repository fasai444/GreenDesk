package org.example.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur pour la page d'accueil
 * Redirige vers Swagger UI
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/home.html";
    }
}
