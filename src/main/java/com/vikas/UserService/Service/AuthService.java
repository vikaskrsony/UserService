package com.vikas.UserService.Service;

import com.vikas.UserService.DTO.UserDTO;
import com.vikas.UserService.Exception.InvalidCredentialException;
import com.vikas.UserService.Exception.InvalidTokenException;
import com.vikas.UserService.Exception.UserNotFoundException;
import com.vikas.UserService.Mapper.UserEntityDTOMapper;
import com.vikas.UserService.Models.Session;
import com.vikas.UserService.Models.SessionStatus;
import com.vikas.UserService.Models.User;
import com.vikas.UserService.Repository.SessionRepository;
import com.vikas.UserService.Repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

import static com.vikas.UserService.Models.SessionStatus.ACTIVE;
import static com.vikas.UserService.Models.SessionStatus.ENDED;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<List<Session>> getAllSession() {
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }

    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    public ResponseEntity<UserDTO> login(String email, String password) {
        //1. Get user details from DB
        Optional<User> userOptional = userRepository.findByEmail(email);

        //2. Check whether the user exists in db or not
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User for the given email id does not exist");
        }
        User user = userOptional.get();

        //3. Verify the password given at the time of the login
        /** If not using any Password algo
         * if(!user.getPassword().equals(password)){
         throw new InvalidCredentialException("Invalid credentials");
         }
         **/

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialException("Invalid Credentials");
        }


        // 4. Generate the "Token" once all the above checks is completed
        //String token = RandomStringUtils.randomAlphanumeric(30);

        MacAlgorithm algorithm = Jwts.SIG.HS256; // HS256 is a hashing algorithm for JWT token
        SecretKey key = algorithm.key().build(); // Generating the secret key

        // Start adding the claims means Attributes for the JWT tokens
        Map<String, Object> jsonForJWT = new HashMap<>();
        jsonForJWT.put("email", user.getEmail());
        jsonForJWT.put("roles", user.getRoles());
        jsonForJWT.put("createdAt", new Date());
        jsonForJWT.put("expiryAt", new Date(LocalDate.now().plusDays(3).toEpochDay()));

        String token = Jwts.builder()
                .claims(jsonForJWT) // added the claims/attributes
                .signWith(key, algorithm) // algorithm in the key
                .compact(); // building the token


        //5. Create the Session (If this user has active session then we will close and then generate the new session)
        check(userOptional.get().getId());
        // creating new session
        Session session = new Session();
        session.setSessionStatus(ACTIVE);
        session.setToken(token);
        session.setLoginAt(new Date());
        session.setUser(user);
        sessionRepository.save(session);

        //6. Generate the response
        UserDTO userDTO = UserEntityDTOMapper.getUserDTOFromUserEntity(user);

        //7. Setting up the Headers
        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token: " + token);

        ResponseEntity<UserDTO> response = new ResponseEntity<>(userDTO, headers, HttpStatus.OK);
        return response;
    }

    public ResponseEntity<String> logout(String token, Long userId) {
        // validations -> token exists, token is not expired, user exists else throw an exception
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_id(token, userId);
        if (sessionOptional.isEmpty()) {
            throw new InvalidCredentialException("Invalid Credentials");
        }
        Session session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);
        return new ResponseEntity<>("Logged-out successfully !!", HttpStatus.OK);
    }

    public UserDTO signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        //user.setPassword(password); //--> to directly save the password

        //saving the password using BCrypt Algorithm
        user.setPassword(bCryptPasswordEncoder.encode(password));

        User savedUser = userRepository.save(user);
        return UserEntityDTOMapper.getUserDTOFromUserEntity(user);
    }

    public SessionStatus validate(String token, Long userId) {
        Optional<Session> optionalSession = sessionRepository.findByTokenAndUser_id(token, userId);
        if (optionalSession.isEmpty() || optionalSession.get().getSessionStatus().equals(ENDED)) {
            throw new InvalidTokenException("Invalid Token");
        }
        return ACTIVE;
    }

    public void check(Long userID) {
        Optional<Session> sessionOptional = sessionRepository.findSessionWhereSessionStatusIsActive(userID);
        if (sessionOptional.isPresent()) {
            Session session = sessionOptional.get();
            session.setSessionStatus(SessionStatus.ENDED);
            sessionRepository.save(session);
        }
    }

}
/*
    MultiValueMapAdapter is map with single key and multiple values
    Headers
    Key     Value
    Token   """
    Accept  application/json, text, images
 */