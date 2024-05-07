package pl.jakubirla.clouddrive.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.jakubirla.clouddrive.service.TokenService;


@RestController
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public String token(Authentication authentication) throws NullPointerException {
        String token = tokenService.generateToken(authentication);

        return String.format("\"%s\"", token);
    }
}
