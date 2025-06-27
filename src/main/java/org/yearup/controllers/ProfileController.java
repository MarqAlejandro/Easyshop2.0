package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.security.SecurityUtils;

@RestController // Marks this class as a REST controller returning JSON responses
@RequestMapping("profile") // Base route for all profile-related endpoints
@CrossOrigin // Enables Cross-Origin Resource Sharing (for frontend/backend interaction)
public class ProfileController
{
    // Dependencies for accessing profile and user data
    private final ProfileDao profileDao;
    private final UserDao userDao;

    // Constructor-based dependency injection
    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    /**
     * GET /profile
     * Retrieves the profile information of the currently authenticated user.
     *
     * @return Profile object for the logged-in user
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')") // Restricts access to users or admins
    public Profile getProfile() {
        // Get the current authenticated username
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        // Find the user ID using the username
        int userId = userDao.getIdByUsername(username);

        // Retrieve and return the profile associated with the user ID
        return profileDao.getByUserId(userId);
    }

    /**
     * PUT /profile
     * Updates the profile of the currently authenticated user.
     *
     * @param profile the new profile data sent in the request body
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')") // Restricts access to users or admins
    public void updateProfile(@RequestBody Profile profile)
    {
        // Get the current authenticated username
        String username = SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated"));

        // Find the user ID for the current user
        int userId = userDao.getIdByUsername(username);

        // Update the profile data in the database
        profileDao.update(profile, userId);
    }
}