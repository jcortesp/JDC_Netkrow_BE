package com.netkrow.backend.security;

import com.netkrow.backend.controller.AuthController;
import com.netkrow.backend.controller.RCARecordController;
import com.netkrow.backend.controller.UserController;
import com.netkrow.backend.controller.BookingController;
import com.netkrow.backend.config.SecurityConfig;
import com.netkrow.backend.service.BookingService;
import com.netkrow.backend.service.OracleQueryService;
import com.netkrow.backend.service.RCARecordService;
import com.netkrow.backend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que las reglas de seguridad definidas en SecurityConfig se apliquen
 * correctamente. Usa WebMvcTest para cargar solo la capa web + Spring Security,
 * sin necesitar base de datos ni conexion Oracle.
 */
@WebMvcTest(controllers = {UserController.class, RCARecordController.class, AuthController.class, BookingController.class})
@Import(SecurityConfig.class)
@DisplayName("Reglas de seguridad - SecurityConfig")
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private RCARecordService rcaRecordService;

    @MockBean
    private OracleQueryService oracleQueryService;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    private BookingService bookingService;

    // GET /api/users

    @Test
    @DisplayName("GET /api/users sin autenticacion -> 401")
    void getUsers_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/users con ROLE_CLIENT -> 403 (solo ADMIN puede listar usuarios)")
    void getUsers_conRolClient_retorna403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SPECIALIST")
    @DisplayName("GET /api/users con ROLE_SPECIALIST -> 403")
    void getUsers_conRolSpecialist_retorna403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users con ROLE_ADMIN -> 200")
    void getUsers_conRolAdmin_retorna200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    // POST /api/rca/query

    @Test
    @DisplayName("POST /api/rca/query sin autenticacion -> 401")
    void rcaQuery_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/rca/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sql\":\"SELECT 1 FROM DUAL\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/rca/query con ROLE_CLIENT -> 403 (endpoint exclusivo ADMIN)")
    void rcaQuery_conRolClient_retorna403() throws Exception {
        mockMvc.perform(post("/api/rca/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sql\":\"SELECT 1 FROM DUAL\"}"))
                .andExpect(status().isForbidden());
    }

    // POST /api/rca/backtrace

    @Test
    @DisplayName("POST /api/rca/backtrace sin autenticacion -> 401")
    void rcaBacktrace_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/rca/backtrace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"search\":\"adidasLAM\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/rca/backtrace con ROLE_CLIENT -> 403")
    void rcaBacktrace_conRolClient_retorna403() throws Exception {
        mockMvc.perform(post("/api/rca/backtrace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"search\":\"adidasLAM\"}"))
                .andExpect(status().isForbidden());
    }

    // POST /api/auth/register (debe ser publico)

    @Test
    @DisplayName("POST /api/auth/register sin autenticacion -> no debe dar 401")
    void register_sinAutenticacion_esPublico() throws Exception {
        int status = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"t@t.com\",\"password\":\"pass\"}"))
                .andReturn()
                .getResponse()
                .getStatus();
        org.junit.jupiter.api.Assertions.assertNotEquals(401, status,
                "El endpoint de registro no debe requerir autenticacion");
    }

    // POST /api/rca/query con ROLE_ADMIN -> happy path

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/rca/query con ROLE_ADMIN -> no debe dar 401 ni 403")
    void rcaQuery_conRolAdmin_esAccesible() throws Exception {
        when(oracleQueryService.executeQuery(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());
        mockMvc.perform(post("/api/rca/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sql\":\"SELECT 1 FROM DUAL\"}"))
                .andExpect(status().isOk());
    }

    // POST /api/bookings/{id}/confirm

    @Test
    @DisplayName("POST /api/bookings/1/confirm sin autenticacion -> 401")
    void confirmBooking_sinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(post("/api/bookings/1/confirm"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/bookings/1/confirm con ROLE_CLIENT -> 403 (solo SPECIALIST)")
    void confirmBooking_conRolClient_retorna403() throws Exception {
        mockMvc.perform(post("/api/bookings/1/confirm"))
                .andExpect(status().isForbidden());
    }
}