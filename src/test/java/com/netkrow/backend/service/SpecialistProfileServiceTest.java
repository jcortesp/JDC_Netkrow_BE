package com.netkrow.backend.service;

import com.netkrow.backend.model.SpecialistProfile;
import com.netkrow.backend.model.User;
import com.netkrow.backend.repository.SpecialistProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class SpecialistProfileServiceTest {

    @Mock
    private SpecialistProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private SpecialistProfileService profileService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateNewProfile() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        SpecialistProfile profileData = new SpecialistProfile();
        profileData.setHeadline("Full Stack Developer");
        profileData.setBio("Expert en Java y React");
        profileData.setLocation("Ciudad X");
        profileData.setTimezone("GMT-5");
        profileData.setLanguages("English, Spanish");
        profileData.setEducation("Universidad Ejemplo");
        profileData.setExperience("5 años");
        profileData.setSkills(Set.of("Java", "React"));
        profileData.setRatePerHour(new BigDecimal("50"));

        // Simulamos que el usuario existe y que no hay perfil asociado
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        // Simulamos que al guardar, se le asigna un ID al perfil
        SpecialistProfile savedProfile = new SpecialistProfile();
        savedProfile.setId(100L);
        savedProfile.setHeadline(profileData.getHeadline());
        // (Se pueden copiar el resto de propiedades de manera similar)
        when(profileRepository.save(any(SpecialistProfile.class))).thenReturn(savedProfile);

        SpecialistProfile result = profileService.createOrUpdateProfile(userId, profileData);
        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    public void testUpdateExistingProfile() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        // Perfil existente
        SpecialistProfile existingProfile = new SpecialistProfile();
        existingProfile.setId(200L);
        existingProfile.setHeadline("Antiguo Titular");

        SpecialistProfile profileData = new SpecialistProfile();
        profileData.setHeadline("Nuevo Titular");
        profileData.setBio("Bio actualizada");
        profileData.setLocation("Nueva Ubicación");
        profileData.setTimezone("GMT-5");
        profileData.setLanguages("English, Spanish");
        profileData.setEducation("Universidad Actualizada");
        profileData.setExperience("6 años");
        profileData.setSkills(Set.of("Java", "Spring Boot"));
        profileData.setRatePerHour(new BigDecimal("60"));

        // Simulamos que el usuario existe y que ya tiene un perfil
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findByUser(user)).thenReturn(Optional.of(existingProfile));

        // Simulamos que al guardar se retorna el perfil actualizado
        SpecialistProfile updatedProfile = new SpecialistProfile();
        updatedProfile.setId(200L);
        updatedProfile.setHeadline(profileData.getHeadline());
        updatedProfile.setBio(profileData.getBio());
        updatedProfile.setLocation(profileData.getLocation());
        updatedProfile.setTimezone(profileData.getTimezone());
        updatedProfile.setLanguages(profileData.getLanguages());
        updatedProfile.setEducation(profileData.getEducation());
        updatedProfile.setExperience(profileData.getExperience());
        updatedProfile.setSkills(profileData.getSkills());
        updatedProfile.setRatePerHour(profileData.getRatePerHour());
        when(profileRepository.save(any(SpecialistProfile.class))).thenReturn(updatedProfile);

        SpecialistProfile result = profileService.createOrUpdateProfile(userId, profileData);
        assertNotNull(result);
        assertEquals("Nuevo Titular", result.getHeadline());
        assertEquals(new BigDecimal("60"), result.getRatePerHour());
    }
}
