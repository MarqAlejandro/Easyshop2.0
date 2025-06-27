package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;

@Component // Marks this class as a Spring-managed component for dependency injection
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    // Constructor for injecting the DataSource and passing it to the base class
    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    /**
     * Inserts a new Profile into the database.
     *
     * @param profile the Profile object to insert
     * @return the same Profile object after insertion
     */
    @Override
    public Profile create(Profile profile)
    {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection())
        {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating profile", e);
        }
    }

    /**
     * Retrieves a Profile by user ID.
     *
     * @param userId the user ID to search for
     * @return the Profile if found, otherwise null
     */
    @Override
    public Profile getByUserId(int userId)
    {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                // Map result set to a Profile object
                return new Profile(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("zip")
                );
            }

            return null;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving profile by user ID", e);
        }
    }

    /**
     * Updates a Profile based on the provided user ID.
     *
     * @param profile the Profile object containing updated info
     * @param userId the user ID to update
     */
    @Override
    public void update(Profile profile, int userId)
    {
        String sql = "UPDATE profiles SET first_name = ?, last_name = ?, phone = ?, email = ?, " +
                "address = ?, city = ?, state = ?, zip = ? WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getEmail());
            ps.setString(5, profile.getAddress());
            ps.setString(6, profile.getCity());
            ps.setString(7, profile.getState());
            ps.setString(8, profile.getZip());
            ps.setInt(9, userId);

            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating profile", e);
        }
    }
}