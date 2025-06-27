package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component // Marks this class as a Spring-managed component for dependency injection
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    // Constructor to inject the DataSource and pass it to the base class
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    /**
     * Searches products by optional filters: categoryId, price range, and color.
     * If a filter is not provided, it will be ignored using default values.
     *
     * @param categoryId filter by category ID
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param color product color
     * @return list of products that match the filters
     */
    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                "WHERE (category_id = ? OR ? = -1) " +
                "  AND (price >= ? OR ? = -1) " +
                "  AND (price <= ? OR ? = -1) " +
                "  AND (color = ? OR ? = '')";

        // Replace nulls with placeholder values to simplify SQL logic
        categoryId = categoryId == null ? -1 : categoryId;
        minPrice = minPrice == null ? new BigDecimal("-1") : minPrice;
        maxPrice = maxPrice == null ? new BigDecimal("-1") : maxPrice;
        color = color == null ? "" : color;

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);
            statement.setBigDecimal(3, minPrice);
            statement.setBigDecimal(4, minPrice);
            statement.setBigDecimal(5, maxPrice);
            statement.setBigDecimal(6, maxPrice);
            statement.setString(7, color);
            statement.setString(8, color);

            ResultSet row = statement.executeQuery();
            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }

    /**
     * Lists all products that belong to a specific category.
     *
     * @param categoryId category ID
     * @return list of products
     */
    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();
            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId product ID
     * @return the product or null if not found
     */
    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();
            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Inserts a new product into the database.
     *
     * @param product the product to insert
     * @return the inserted product with generated ID, or null if insert failed
     */
    @Override
    public Product create(Product product)
    {
        String sql = "INSERT INTO products(name, price, category_id, description, color, image_url, stock, featured) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int productId = generatedKeys.getInt(1);
                    return getById(productId); // Fetch full product info
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Updates an existing product by ID.
     *
     * @param productId the product ID to update
     * @param product updated product data
     */
    @Override
    public void update(int productId, Product product)
    {
        String sql = "UPDATE products SET " +
                "name = ?, price = ?, category_id = ?, description = ?, color = ?, " +
                "image_url = ?, stock = ?, featured = ? " +
                "WHERE product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a product by its ID.
     *
     * @param productId the ID of the product to delete
     */
    @Override
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Maps a row from the ResultSet to a Product object.
     *
     * @param row the current row from the ResultSet
     * @return a fully populated Product object
     */
    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, color, stock, isFeatured, imageUrl);
    }
}