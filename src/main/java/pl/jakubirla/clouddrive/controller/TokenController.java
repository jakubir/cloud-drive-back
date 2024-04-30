package pl.jakubirla.clouddrive.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
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
        System.out.printf("User: %s - ", authentication.toString());
        System.out.printf("generated token: %s%n", token);

        return token;
    }
}
