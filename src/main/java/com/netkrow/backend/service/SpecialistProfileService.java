package com.netkrow.backend.service;

import com.netkrow.backend.model.SpecialistProfile;
import com.netkrow.backend.model.User;
import com.netkrow.backend.repository.SpecialistProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class SpecialistProfileService {

    @Autowired
    private SpecialistProfileRepository profileRepository;

    @Autowired
    private UserService userService;

    // Búsqueda flexible
    public List<SpecialistProfile> search(String skill, java.math.BigDecimal minRate, java.math.BigDecimal maxRate) {
        return profileRepository.search(skill, minRate, maxRate);
    }

    // Crear o actualizar perfil para un usuario especialista
    public SpecialistProfile createOrUpdateProfile(Long userId, SpecialistProfile profileData) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        SpecialistProfile profile;
        // Buscar el perfil asociado al usuario
        Optional<SpecialistProfile> existing = profileRepository.findByUser(user);
        if (existing.isPresent()) {
            profile = existing.get();
        } else {
            profile = new SpecialistProfile();
            profile.setUser(user);
        }
        // Actualizar todos los campos del perfil
        profile.setHeadline(profileData.getHeadline());
        profile.setBio(profileData.getBio());
        profile.setLocation(profileData.getLocation());
        profile.setTimezone(profileData.getTimezone());
        profile.setLanguages(profileData.getLanguages());
        profile.setEducation(profileData.getEducation());
        profile.setExperience(profileData.getExperience());
        profile.setSkills(profileData.getSkills());
        profile.setRatePerHour(profileData.getRatePerHour());

        return profileRepository.save(profile);
    }

    // Obtener el perfil por usuario
    public Optional<SpecialistProfile> getProfileByUserId(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return profileRepository.findByUser(user);
    }

    public Iterable<SpecialistProfile> getAllProfiles() {
        return profileRepository.findAll();
    }
}
