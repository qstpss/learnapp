package com.luxoft.learnapp.controllers;

import com.luxoft.learnapp.api.web.AuthApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/app")
public class AuthController implements AuthApi {
    private Map<String, String> credentialsMap;

    public AuthController() {
        credentialsMap = new HashMap<String, String>(){{
            put("login1", "password1");
            put("login2", "password2");
            put("login3", "password3");
            put("login4", "password4");
        }};
    }

    @Override
    public ResponseEntity<String> authAuthTokenGet(
            @NotNull @Valid String login,
            @NotNull @Valid String password) {
        if (credentialsMap.containsKey(login) && credentialsMap.containsValue(password)) {
            return ResponseEntity.ok().body("Auth token = " + UUID.randomUUID().toString());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Credentials you entered are wrong");
    }
}
