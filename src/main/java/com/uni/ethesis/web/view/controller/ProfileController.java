package com.uni.ethesis.web.view.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uni.ethesis.data.dto.ProfileUpdateDto;
import com.uni.ethesis.data.dto.UserDto;
import com.uni.ethesis.service.UserService;
import com.uni.ethesis.service.UserViewService;
import com.uni.ethesis.utils.mappers.UserMapper;
import com.uni.ethesis.web.view.model.PasswordChangeViewModel;
import com.uni.ethesis.web.view.model.ProfileUpdateViewModel;
import com.uni.ethesis.web.view.model.UserViewModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserViewService userViewService;

    @GetMapping
    public String showProfile(Model model, Authentication auth) {
        try {
            // Get current user information using service
            UserDto currentUser = userViewService.getCurrentUser(auth);
            
            // Create UserViewModel using service
            UserViewModel userViewModel = userViewService.getCurrentUserViewModel(auth);
            
            // Create profile update view model with current values
            ProfileUpdateViewModel profileUpdateViewModel = new ProfileUpdateViewModel();
            profileUpdateViewModel.setFirstName(currentUser.getFirstName());
            profileUpdateViewModel.setLastName(currentUser.getLastName());
            profileUpdateViewModel.setEmail(currentUser.getEmail());
            
            model.addAttribute("user", userViewModel);
            model.addAttribute("profileUpdateViewModel", profileUpdateViewModel);
            model.addAttribute("passwordChangeViewModel", new PasswordChangeViewModel());
            model.addAttribute("title", "User Profile");
            
            return "profile/index";
        } catch (Exception e) {
            log.error("Error loading profile", e);
            model.addAttribute("error", "Failed to load profile information");
            return "error";
        }
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute ProfileUpdateViewModel profileUpdateViewModel,
                              BindingResult bindingResult,
                              Authentication auth,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                // Create UserViewModel using service
                UserViewModel userViewModel = userViewService.getCurrentUserViewModel(auth);
                
                model.addAttribute("user", userViewModel);
                model.addAttribute("passwordChangeViewModel", new PasswordChangeViewModel());
                model.addAttribute("title", "User Profile");
                return "profile/index";
            }

            // Get current user ID using UserViewService
            UserDto currentUser = userViewService.getCurrentUser(auth);
            
            // Convert view model to DTO using mapper
            ProfileUpdateDto profileUpdateDto = userMapper.toProfileUpdateDto(profileUpdateViewModel);
            
            // Update profile
            UserDto updatedUser = userService.updateUserProfile(currentUser.getId(), profileUpdateDto);
            
            log.info("Profile updated successfully for user: {}", updatedUser.getEmail());
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            
            return "redirect:/profile";

        } catch (Exception e) {
            log.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update profile. Please try again.");
            return "redirect:/profile";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeViewModel passwordChangeViewModel,
                               BindingResult bindingResult,
                               Authentication auth,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors() || !passwordChangeViewModel.isPasswordsMatch()) {
                if (!passwordChangeViewModel.isPasswordsMatch()) {
                    bindingResult.rejectValue("confirmPassword", "error.passwordChangeViewModel", 
                                            "Passwords do not match");
                }
                
                UserDto currentUser = userViewService.getCurrentUser(auth);
                ProfileUpdateViewModel profileUpdateViewModel = new ProfileUpdateViewModel();
                profileUpdateViewModel.setFirstName(currentUser.getFirstName());
                profileUpdateViewModel.setLastName(currentUser.getLastName());
                profileUpdateViewModel.setEmail(currentUser.getEmail());
                
                // Create UserViewModel using UserViewService
                UserViewModel userViewModel = userViewService.getCurrentUserViewModel(auth);
                
                model.addAttribute("user", userViewModel);
                model.addAttribute("profileUpdateViewModel", profileUpdateViewModel);
                model.addAttribute("title", "User Profile");
                return "profile/index";
            }

            // Note: In a real Keycloak integration, you would call Keycloak Admin API
            // to change the password. For now, we'll show a message about this limitation.
            redirectAttributes.addFlashAttribute("info", 
                "Password change requests must be handled through the authentication provider. " +
                "Please contact your system administrator or use the 'Forgot Password' option at login.");
            
            return "redirect:/profile";

        } catch (Exception e) {
            log.error("Error processing password change request", e);
            redirectAttributes.addFlashAttribute("error", "Failed to process password change request.");
            return "redirect:/profile";
        }
    }
}
