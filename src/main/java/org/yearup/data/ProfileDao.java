package org.yearup.data;

import org.yearup.models.Profile;

/**
 * Interface for performing CRUD operations on user profiles.
 * Implementations of this interface should manage the user's personal
 * and contact information stored in the `profiles` table.
 */
public interface ProfileDao // change
{
    /**
     * Creates a new profile for the given user.
     *
     * @param profile the Profile object containing user details
     * @return the created Profile object (may include generated fields)
     */
    Profile create(Profile profile);

    /**
     * Retrieves a profile based on the user's ID.
     *
     * @param userId the ID of the user whose profile is requested
     * @return the Profile object if found; otherwise, null
     */
    Profile getByUserId(int userId);

    /**
     * Updates an existing profile for the given user ID.
     *
     * @param profile the updated Profile data
     * @param userId the ID of the user whose profile is being updated
     */
    void update(Profile profile, int userId);
}