package ru.sanctio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sanctio.service.UserActivationService;

@RequestMapping("/user")
@RestController
public class ActivationController {

    private final UserActivationService userActivationService;

    @Autowired
    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    @GetMapping("/activation")
    public ResponseEntity<?> activation(@RequestParam String id) {
        boolean result = userActivationService.activation(id);
        if(result) {
            return ResponseEntity.ok().body("Регистрация успешно прошла!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
