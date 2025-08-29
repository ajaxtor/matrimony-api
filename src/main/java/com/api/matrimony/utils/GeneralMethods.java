package com.api.matrimony.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.response.ProfileResponse;

@Component
public class GeneralMethods {
	
	@Autowired
	  private UserPhotoRepository photoRepository;

	public static <T> Page<T> paginateList(List<T> fullList, int page, int size) {
	    int total = fullList.size();
	    int start = Math.min(page * size, total);
	    int end = Math.min(start + size, total);

	    List<T> sublist = fullList.subList(start, end);
	    Pageable pageable = PageRequest.of(page, size);

	    return new PageImpl<>(sublist, pageable, total);
	}
	
	public  ProfileResponse mapToProfileResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setUserId(profile.getUser().getId());
        response.setFullName(profile.getFullName() );
        response.setDateOfBirth(profile.getDateOfBirth());
        if (profile.getDateOfBirth() != null) {
            response.setAge(Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears());
        }
        
        response.setGender(profile.getGender() != null ? profile.getGender().name() : null);
        response.setHeight(profile.getHeight());
        response.setWeight(profile.getWeight());
        response.setMaritalStatus(profile.getMaritalStatus() != null ? 
                                 profile.getMaritalStatus().name() : null);
        response.setReligion(profile.getReligion());
        response.setCaste(profile.getCaste());
        response.setSubCaste(profile.getSubCaste());
        response.setMotherTongue(profile.getMotherTongue());
        response.setEducation(profile.getEducation());
        response.setOccupation(profile.getOccupation());
        response.setAnnualIncome(profile.getAnnualIncome());
        response.setAboutMe(profile.getAboutMe());
        response.setFamilyType(profile.getFamilyType());
        response.setFamilyValue(profile.getFamilyValue());
        response.setCity(profile.getCity());
        response.setState(profile.getState());
        response.setCountry(profile.getCountry());
        response.setPincode(profile.getPincode());
        response.setProfileCreatedBy(profile.getProfileCreatedBy());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        response.setDiet(profile.getDiet());

        // Get photos
        List<UserPhoto> photos = photoRepository.findByUserIdOrderByDisplayOrderAsc(profile.getUser().getId());
        response.setPhotoUrls(photos.stream().map(UserPhoto::getPhotoUrl).collect(Collectors.toList()));
        
        // Get primary photo
        photos.stream()
                .filter(UserPhoto::getIsPrimary)
                .findFirst()
                .ifPresent(photo -> response.setPrimaryPhotoUrl(photo.getPhotoUrl()));

        return response;
        
    }
	

    public  String generate10CharId() {
        return UUID.randomUUID()
                   .toString()
                   .replace("-", "")
                   .substring(0, 10)
                   .toUpperCase();
    }
	
}
