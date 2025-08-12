package com.api.matrimony.mapper;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.User;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.response.LocationResponce;
import com.api.matrimony.response.MatchResponse;

public class MatchMapper {

    // =================== Response -> Entity ===================
    public static Match toEntity(MatchResponse response, User loggedInUser, User matchedUser) {
        if (response == null || loggedInUser == null || matchedUser == null) {
            return null;
        }

        Match match = new Match();
        match.setUser(loggedInUser);
        match.setMatchedUser(matchedUser);
        match.setMatchScore(response.getMatchScore() != null
                ? BigDecimal.valueOf(response.getMatchScore())
                : BigDecimal.ZERO);
        match.setStatus(MatchStatus.PENDING);
        match.setMatchedAt(LocalDateTime.now());
        match.setUpdatedAt(LocalDateTime.now());

        return match;
    }

    public static List<Match> toEntityList(List<MatchResponse> responses, User loggedInUser, UserRepository userRepository) {
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
    public static MatchResponse toResponse(Match match) {
        if (match == null || match.getMatchedUser() == null) {
            return null;
        }

        User matchedUser = match.getMatchedUser();

        MatchResponse response = new MatchResponse();
        response.setUserId(matchedUser.getId());
        response.setName(matchedUser.getUsername());
        response.setAge(calculateAge(matchedUser.getProfile().getDateOfBirth()));
        response.setGender(matchedUser.getProfile().getGender() != null ? matchedUser.getProfile().getGender().name() : null);
        response.setLocation(toLocationResponse(matchedUser)); // map location
        response.setEducation(matchedUser.getProfile().getEducation());
        response.setOccupation(matchedUser.getProfile().getOccupation());
        response.setHasPhotos(matchedUser.getPhotos() != null && !matchedUser.getPhotos().isEmpty());
        response.setMatchScore(match.getMatchScore() != null ? match.getMatchScore().doubleValue() : 0.0);

        return response;
    }

    public static List<MatchResponse> toResponseList(List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return List.of();
        }
        return matches.stream()
                .map(MatchMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =================== Helper Methods ===================
    private static Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private static LocationResponce toLocationResponse(User user) {
        if (user.getProfile()== null) {
            return null;
        }
        LocationResponce location = new LocationResponce();
        location.setCity(user.getProfile().getCity());
        location.setState(user.getProfile().getState());
        location.setCountry(user.getProfile().getCountry());
        return location;
    }
}

