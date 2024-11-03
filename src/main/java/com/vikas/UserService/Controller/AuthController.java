package com.vikas.UserService.Controller;

import com.vikas.UserService.DTO.LoginRequestDTO;
import com.vikas.UserService.DTO.SignUpRequestDTO;
import com.vikas.UserService.DTO.UserDTO;
import com.vikas.UserService.DTO.ValidateTokenRequestDTO;
import com.vikas.UserService.Models.Session;
import com.vikas.UserService.Models.SessionStatus;
import com.vikas.UserService.Models.User;
import com.vikas.UserService.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<Void> logout(@PathVariable("id") Long userId, @RequestHeader("token") String token) {
        return authService.logout(token, userId);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@RequestBody SignUpRequestDTO request) {
        UserDTO UserDTO = authService.signUp(request.getEmail(), request.getPassword());
        return new ResponseEntity<>(UserDTO, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<SessionStatus> validateToken(@RequestBody ValidateTokenRequestDTO request) {
        SessionStatus sessionStatus = authService.validate(request.getToken(), request.getUserId());

        return new ResponseEntity<>(sessionStatus, HttpStatus.OK);
    }

    //below APIs are only for learning purposes, should not be present in actual systems
    @GetMapping("/session")
    public ResponseEntity<List<Session>> getAllSession() {
        return authService.getAllSession();
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return authService.getAllUsers();
    }
}
