package com.fastcampus.boardserver.controller;

import com.fastcampus.boardserver.dto.UserDTO;
import com.fastcampus.boardserver.service.impl.UserServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Log4j2
public class UserController {
    private final UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody UserDTO userDTO) {
        if (userDTO.hasNullDataBeforeRegister(userDTO)) {
            throw new RuntimeException("회원가입 정보를 확인해주세요");
        }
        userService.register(userDTO);
    }
}
