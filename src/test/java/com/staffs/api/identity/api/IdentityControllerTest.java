package com.staffs.api.identity.api;

import com.staffs.api.identity.application.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private IdentityController controller;

    @Test
    void loginDelegatesToServiceAndWrapsToken() {
        var request = new LoginRequest("user@example.com", "s3cret");
        when(userService.login("user@example.com", "s3cret")).thenReturn("jwt-token");

        var response = controller.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }
}
