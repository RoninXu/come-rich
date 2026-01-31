package com.finance.planner.career.service;

import com.finance.planner.career.dto.SaveProfileRequest;
import com.finance.planner.career.dto.UserProfileDto;

public interface UserProfileService {

    UserProfileDto getProfile(Long userId);

    UserProfileDto saveProfile(Long userId, SaveProfileRequest request);
}
