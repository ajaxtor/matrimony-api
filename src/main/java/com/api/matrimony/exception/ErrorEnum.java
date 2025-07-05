package com.api.matrimony.exception;



public enum ErrorEnum {

	// General Error Code starts from - 1001

	// Permission / Login related Error Code starts from - 2001

	// Mismatched Error code Starts from - 3001

	// Search Type error start from - 4001

	// Media Type error start from - 5001

	PERMISSION_DENIED("Permisson Denied"), USER_ALREADY_EXIST("User already exist "),
	EMAIL_ALREADY_EXIST("Email is already registered"),NUMBER_ALREADY_EXIST("Phone number is already registered"),
	USER_OR_PASSWORD_NOT_MATCH(" User Name or password is not matched"), BAD_RESQUEST(""), UNAUTHORIZED_ERROE(""),
	INVALID_USER("Invalid user"),INACTIVE_ACCOUNT("Account is not active. Please contact support."),
	ACCOUNT_IS_NOT_VERIFIED("Account is not verified. Please verify your account first."),REFRESH_TOKEN_EXP("Invalid or expired refresh token"),
	BLANK_OR_EMPTY_LIST(" blank or empty List Returned"),INVALID_OTP("Invalid or expired OTP"), FILE_SIZE_IS_MORE_THAN_REQ("File size cannot exceed 10MB"),
	ONLY_OWN_PHOTO_CAN_DELETE("You can only delete your own photos"), PHOTO_CAN_NOT_FOUND(" photos CAn not found"),USER_IS_NOT_BLOCK("User is not blocked"),
	USER_TO_BLOCK_NOT_FOUND("User to block not found"),BLOCK_USER_NOT_FOUND("Blocker user not found"),YOU_CAN_NOT_BLOCK_URSELF("You cannot block yourself"),USER_ALREADY_BLOCK("User is already blocked"),
	FAILED_TO_UPLOAD_PHOTO("failed TO upload photoS "),IMAGE_ALLOW("Only image files are allowed"),EMPTY_FILE("empty file"),
	TOO_MANY_APTEMT_FOR_OTP("Too many OTP attempts. Please try again later."),MATCH_NOT_FOUND("Match not found"),
	ONLY_VIEW_OWN_MATCH("You can only view your own matches"),ONLY_ACT_OWN_MATCH("You can only act on your own matches"),
	PROFILE_NOT_FOUND_FOR_USER("Profile not found for user"),USER_NOT_FOUND("User not found"),PROFILE_NOT_ACCESSABLE("Profile not accessible"),
	CNOV_NOT_FOUND("Conversation not found"),YOU_ARE_PART_OF_CONV("You are not part of this conversation"),SUB_NOT_FOUND("Subscription plan not found"),
	SUB_NOT_ACTIVE("Subscription plan is not active"),USER_IN_ACTIVE_SUB("User already has an active subscription"),
	NO_ACTIVE_SUB("No active subscription found"),USER_1_NOT_FOUND("User1 not found"),USER_2_NOT_FOUND("User2 not found"),
	NO_MATCH_FUND_BTWN_USER("No mutual match found between users"),ONLY_MUTUAL_MATCH_CAN_MSG("You can only message users with mutual matches"),
	YOU_BLOCK_USER("You have blocked this user"),YOU_BLOCK_BY_USER("You are blocked by this user"),
	RECIVER_NOT_FUND("Receiver not found"),SENDER_NOT_FUND("Sender not found"),ONLY_CAN_DELETE_OWN_MSG("You can only delete your own messages"),
	MESSAGE_NOT_FUND("Message not found"),;
;

	private String exceptionError;

	ErrorEnum(String exceptionError) {
		this.exceptionError = exceptionError;
	}

	public String getExceptionError() {
		return exceptionError;
	}

}
