package com.netkrow.backend.controller;

import com.netkrow.backend.model.User;
import com.netkrow.backend.security.JwtUtils;
import com.netkrow.backend.service.UserService;
import com.netkrow.backend.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class) // Importamos la configuración de seguridad personalizada
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterUser() throws Exception {
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole("ROLE_CLIENT");

        Mockito.when(userService.findByEmail(Mockito.anyString()))
                .thenReturn(java.util.Optional.empty());
        User user = new User();
        user.setId(1L);
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(user);

        String payload = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginUser() throws Exception {
        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser.getEmail(), loginUser.getPassword());
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationToken);

        Mockito.when(userService.findByEmail(Mockito.anyString()))
                .thenReturn(java.util.Optional.of(loginUser));
        String jwtToken = "dummy-jwt-token";
        Mockito.when(jwtUtils.generateToken(Mockito.any(User.class))).thenReturn(jwtToken);

        String payload = objectMapper.writeValueAsString(loginUser);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGetAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
