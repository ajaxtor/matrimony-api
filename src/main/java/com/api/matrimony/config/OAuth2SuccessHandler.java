//package com.api.matrimony.config;
//
//import java.io.IOException;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import com.api.matrimony.request.LoginRequest;
//import com.api.matrimony.response.LoginResponse;
//import com.api.matrimony.serviceImpl.AuthServiceImpl;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//
//
//
//@Component
//@RequiredArgsConstructor
//public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
//
//    private final AuthServiceImpl authServiceImpl;
//    private final JwtUtil jwtService;
//
//    @Override
//    public void onAuthenticationSuccess(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            Authentication authentication) throws IOException {
//
//        // Extract oauth user
//        DefaultOAuth2User oAuthUser = (DefaultOAuth2User) authentication.getPrincipal();
//
//        // Extract provider name
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//        String provider = oauthToken.getAuthorizedClientRegistrationId();
//
//        // Extract providerId
//        String providerId = null;
//        if (provider.equals("google")) {
//            providerId = oAuthUser.getAttribute("sub");
//        } else if (provider.equals("facebook")) {
//            providerId = oAuthUser.getAttribute("id");
//        }
//
//        // Extract email & name
//        String email = oAuthUser.getAttribute("email");
//        String name  = oAuthUser.getAttribute("name");
//
//        // Build LoginRequest for your existing login() method
//        
//        LoginRequest req = new LoginRequest();
//        req.setEmailOrPhone(email);
//        req.setFullName(name);
//        req.setProvider(provider); 
//        req.setProviderId(providerId);
//
//        // CALL YOUR EXISTING LOGIN METHOD
//        LoginResponse loginResponse = authServiceImpl.login(req);
//
//        // Extract JWT
//        String jwt = loginResponse.getAccessToken();
//
//        // Redirect to frontend with JWT
//        
//        response.sendRedirect("http://localhost:3000/social-login-success?token=" + jwt);
//    }
//}
