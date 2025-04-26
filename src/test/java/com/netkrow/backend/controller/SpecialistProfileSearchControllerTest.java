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
import java.math.BigDecimal;
import java.util.Arrays;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpecialistProfileController.class)
public class SpecialistProfileSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpecialistProfileService profileService;

    // Bean simulado para UserService (requerido por seguridad)
    @MockBean
    private UserService userService;

    // Bean simulado para JwtUtils (requerido por el filtro de seguridad)
    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testSearchSpecialistProfiles() throws Exception {
        // Creamos dos perfiles de prueba
        SpecialistProfile profile1 = new SpecialistProfile();
        profile1.setId(1L);
        profile1.setHeadline("Desarrollador Java");
        profile1.setRatePerHour(new BigDecimal("50"));

        SpecialistProfile profile2 = new SpecialistProfile();
        profile2.setId(2L);
        profile2.setHeadline("Experto en Spring Boot");
        profile2.setRatePerHour(new BigDecimal("60"));

        // Simulamos que el servicio retorna una lista de perfiles que cumplen el criterio
        Mockito.when(profileService.search(Mockito.eq("Java"), Mockito.any(), Mockito.any()))
                .thenReturn(Arrays.asList(profile1, profile2));

        // Realizamos la petición GET al endpoint de búsqueda con parámetros
        mockMvc.perform(get("/api/specialists/search")
                        .param("skill", "Java")
                        .param("minRate", "30")
                        .param("maxRate", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
