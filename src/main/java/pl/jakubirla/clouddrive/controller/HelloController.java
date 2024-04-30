package pl.jakubirla.clouddrive.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping("")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println(authentication.getAuthorities());

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")))
            name = "your majesty";

        return String.format("Hello %s from HelloController", name);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @GetMapping("user")
    public String helloUser() {
        return "Hello User";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("admin")
    public String helloAdmin() {
        return "Hello Admin";
    }
}
