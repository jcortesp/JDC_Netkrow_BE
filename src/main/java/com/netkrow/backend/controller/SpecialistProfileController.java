package com.netkrow.backend.controller;

import com.netkrow.backend.model.SpecialistProfile;
import com.netkrow.backend.service.SpecialistProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/specialists")
public class SpecialistProfileController {

    @Autowired
    private SpecialistProfileService profileService;

    // Endpoint de búsqueda (sin cambios)
    @GetMapping("/search")
    public ResponseEntity<?> searchProfiles(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) java.math.BigDecimal minRate,
            @RequestParam(required = false) java.math.BigDecimal maxRate
    ) {
        return ResponseEntity.ok(profileService.search(skill, minRate, maxRate));
    }

    // Endpoint para crear o actualizar el perfil
    // Se utiliza un header "userId" para identificar dinámicamente al usuario autenticado.
    @PostMapping("/profile")
    public ResponseEntity<?> createOrUpdateProfile(@RequestBody SpecialistProfile profileData,
                                                   @RequestHeader("userId") Long userId) {
        SpecialistProfile saved = profileService.createOrUpdateProfile(userId, profileData);
        return ResponseEntity.ok(saved);
    }

    // Endpoint para obtener el perfil del usuario autenticado
    @GetMapping("/profile")
    public ResponseEntity<?> getProfileByUser(@RequestHeader("userId") Long userId) {
        return profileService.getProfileByUserId(userId)
                .map(profile -> ResponseEntity.ok(profile))
                .orElse(ResponseEntity.notFound().build());
    }

    // Listar todos los perfiles
    @GetMapping("/all")
    public ResponseEntity<?> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }
}
