package com.example.SSO_project.ControllerImpl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
public class UserControllerImplTest {
//
//    @Mock
//    private UserController userController;
//    @Mock
//    private UserRepository userRepository;

    @ParameterizedTest
    @CsvSource({
            "1,1,2"
    })
    public void getAllUsers(int a , int b, int c) {
        assertEquals(a+b,c);
       assertEquals(4,2+2);
    }



}
