/**
 * 
 */
package com.utopia.smoothstack.userauthservice.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.smoothstack.utopia.userauthservice.registration.controller.PasswordResetController;
import com.smoothstack.utopia.userauthservice.registration.dto.PasswordDto;
import com.smoothstack.utopia.userauthservice.registration.dto.UserFields;

/**
 * @author Craig Saunders
 *
 */
class PasswordResetControllerTest {    
    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception
    {
        //controller = new PasswordResetController();
    }

    @Test
    void test()
    {
        //assertEquals(controller.resetPasswordTokenLink("userA"), "password-reset");
        //public UserFields cofirmPasswordToken(@RequestParam("token") String token)
        //public String registerUserAccount(@Valid @RequestBody final PasswordDto passwordDto)
    }

}
