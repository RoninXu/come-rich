package com.finance.planner.career.service.impl;

import com.finance.planner.career.dto.SaveProfileRequest;
import com.finance.planner.career.dto.UserProfileDto;
import com.finance.planner.career.entity.UserProfile;
import com.finance.planner.career.repository.UserProfileRepository;
import com.finance.planner.career.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfileDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional
    public UserProfileDto saveProfile(Long userId, SaveProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUserId(userId);
                    return p;
                });

        if (request.getOccupation() != null) {
            profile.setOccupation(request.getOccupation());
        }
        if (request.getSkills() != null) {
            profile.setSkills(request.getSkills());
        }
        if (request.getAvailableHoursPerWeek() != null) {
            profile.setAvailableHoursPerWeek(request.getAvailableHoursPerWeek());
        }
        if (request.getIncomeExpectation() != null) {
            profile.setIncomeExpectation(request.getIncomeExpectation());
        }
        if (request.getInterests() != null) {
            profile.setInterests(request.getInterests());
        }
        if (request.getExperienceLevel() != null) {
            profile.setExperienceLevel(request.getExperienceLevel());
        }

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Saved profile for user {}", userId);
        return UserProfileDto.fromEntity(saved);
    }
}
