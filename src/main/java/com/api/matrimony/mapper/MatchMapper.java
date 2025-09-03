package com.api.matrimony.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.User;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.response.LocationResponce;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.utils.GeneralMethods;

@Component
public class MatchMapper {

    private final GeneralMethods generalMethods;

    @Autowired
    public MatchMapper(GeneralMethods generalMethods) {
        this.generalMethods = generalMethods;
    }

    // =================== Response -> Entity ===================
    public Match toEntity(MatchResponse response, User loggedInUser, User matchedUser) {
        if (response == null || loggedInUser == null || matchedUser == null) {
            return null;
        }

        Match match = new Match();
        match.setUser(loggedInUser);
        match.setMatchId(response.getMatchId());
        match.setMatchedUser(matchedUser);
        match.setMatchScore(response.getMatchScore() != null
                ? BigDecimal.valueOf(response.getMatchScore())
                : BigDecimal.ZERO);
        match.setStatus(MatchStatus.MATCH);
        match.setMatchedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        return match;
    }

    public List<Match> toEntityList(List<MatchResponse> responses, User loggedInUser, UserRepository userRepository) {
        if (responses == null || responses.isEmpty()) {
            return List.of();
        }
        return responses.stream()
                .map(response -> {
                    User matchedUser = userRepository.findById(response.getUserId())
                            .orElseThrow(() -> new RuntimeException("Matched user not found: " + response.getUserId()));
                    return toEntity(response, loggedInUser, matchedUser);
                })
                .collect(Collectors.toList());
    }

    // =================== Entity -> Response ===================
    public MatchResponse toResponse(Match match) {
        if (match == null || match.getMatchedUser() == null) {
            return null;
        }

        User matchedUser = match.getMatchedUser();

        MatchResponse response = new MatchResponse();
        response.setUserId(match.getMatchedUser().getId());
        response.setMatchId(match.getMatchId());
        response.setMatchStatus(match.getStatus());
        ProfileResponse data = generalMethods.mapToProfileResponse(match.getMatchedUser().getProfile());
        response.setProfileResponse(data);
        response.setMatchScore(match.getMatchScore() != null ? match.getMatchScore().doubleValue() : 0.0);

        return response;
    }

    public List<MatchResponse> toResponseList(List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return List.of();
        }
        return matches.stream()
                .map(this::toResponse)   // ✅ FIXED: use instance method, not static reference
                .collect(Collectors.toList());
    }

    // =================== Helper Methods ===================
    private static Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private static LocationResponce toLocationResponse(User user) {
        if (user.getProfile() == null) {
            return null;
        }
        LocationResponce location = new LocationResponce();
        location.setCity(user.getProfile().getCity());
        location.setState(user.getProfile().getState());
        location.setCountry(user.getProfile().getCountry());
        return location;
    }
}
