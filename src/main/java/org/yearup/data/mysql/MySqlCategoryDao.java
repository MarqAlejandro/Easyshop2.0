package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component // Marks this class as a Spring-managed component (bean) for dependency injection
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    // Constructor calls parent class with the provided DataSource
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return List of Category objects
     */
    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet row = statement.executeQuery();

            while (row.next()) {
                Category category = mapRow(row);
                categories.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return categories;
    }

    /**
     * Retrieves a single category by its ID.
     *
     * @param categoryId ID of the category to retrieve
     * @return Category object or null if not found
     */
    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            ResultSet row = statement.executeQuery();

            if (row.next()) {
                return mapRow(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Inserts a new category into the database.
     *
     * @param category the category to insert
     * @return the inserted category with its new ID
     */
    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories(name, description) VALUES (?, ?)";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();

                if (generatedKeys.next()) {
                    int categoryId = generatedKeys.getInt(1);
                    return getById(categoryId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Updates an existing category's name and description.
     *
     * @param categoryId ID of the category to update
     * @param category the updated category data
     */
    @Override
    public void update(int categoryId, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a category by its ID.
     *
     * @param categoryId the ID of the category to delete
     */
    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Maps a single row from the ResultSet to a Category object.
     *
     * @param row the current row of the ResultSet
     * @return a fully populated Category object
     * @throws SQLException if a database access error occurs
     */
    private Category mapRow(ResultSet row) throws SQLException {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        // Using double brace initialization for brevity
        return new Category() {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};
    }
}