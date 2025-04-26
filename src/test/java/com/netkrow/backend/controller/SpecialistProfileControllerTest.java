package com.netkrow.backend.controller;

import com.netkrow.backend.model.SpecialistProfile;
import com.netkrow.backend.security.JwtUtils;
import com.netkrow.backend.service.SpecialistProfileService;
import com.netkrow.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpecialistProfileController.class)
public class SpecialistProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpecialistProfileService profileService;

    @MockBean
    private UserService userService;

    // Se inyecta JwtUtils para satisfacer la dependencia del filtro de seguridad
    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "SPECIALIST")
    public void testCreateOrUpdateProfile() throws Exception {
        Long userId = 1L;

        // Creamos un objeto de perfil con datos de prueba
        SpecialistProfile profile = new SpecialistProfile();
        profile.setHeadline("Desarrollador Full Stack");
        profile.setBio("Experiencia en React y Spring Boot");
        profile.setLocation("Ciudad de México");
        profile.setTimezone("GMT-6");
        profile.setLanguages("Español, Inglés");
        profile.setEducation("Universidad de la Tecnología");
        profile.setExperience("3 años");
        profile.setRatePerHour(new java.math.BigDecimal("50"));
        profile.setSkills(java.util.Set.of("Java", "React", "Spring Boot"));

        // Simulamos que el servicio retorna el objeto de perfil recibido
        Mockito.when(profileService.createOrUpdateProfile(Mockito.eq(userId), Mockito.any(SpecialistProfile.class)))
                .thenReturn(profile);

        // Realizamos la petición POST al endpoint "/api/specialists/profile", incluyendo un token CSRF válido
        mockMvc.perform(post("/api/specialists/profile")
                        .with(csrf())
                        .header("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk());
    }
}
