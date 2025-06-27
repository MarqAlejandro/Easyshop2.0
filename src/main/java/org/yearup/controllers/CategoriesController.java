package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

@RestController // Indicates this class handles REST API requests and returns JSON responses
@RequestMapping("categories") // Base URL for all endpoints in this controller
@CrossOrigin // Enables cross-origin requests (e.g., frontend running on a different port)
public class CategoriesController
{
    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    // Constructor-based dependency injection
    @Autowired
    public CategoriesController(ProductDao productDao, CategoryDao categoryDao)
    {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
    }

    /**
     * GET /categories
     * Retrieves all categories.
     *
     * @return List of all categories
     */
    @GetMapping("")
    @PreAuthorize("permitAll()") // Open to all users, authenticated or not
    public List<Category> getAll()
    {
        try {
            return categoryDao.getAllCategories();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * GET /categories/{id}
     * Retrieves a single category by ID.
     *
     * @param id the category ID
     * @return Category object
     */
    @GetMapping("{id}")
    @PreAuthorize("permitAll()") // Open to all users
    public Category getById(@PathVariable int id)
    {
        try {
            var category = categoryDao.getById(id);

            if (category == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            return category;
        }
        catch (ResponseStatusException ex) {
            throw ex; // Re-throw if it's already a status exception
        }
        catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * GET /categories/{categoryId}/products
     * Retrieves all products under a given category.
     *
     * @param categoryId the ID of the category
     * @return List of products
     */
    @GetMapping("{categoryId}/products")
    @PreAuthorize("permitAll()") // Open to all users
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        try {
            return productDao.listByCategoryId(categoryId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * POST /categories
     * Creates a new category.
     *
     * @param category the category data from request body
     * @return the created Category object
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Restricted to admin users
    @ResponseStatus(HttpStatus.CREATED) // Sets 201 Created response code
    public Category addCategory(@RequestBody Category category)
    {
        try {
            return categoryDao.create(category);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * PUT /categories/{id}
     * Updates an existing category by ID.
     *
     * @param id the ID of the category to update
     * @param category updated category data from request body
     */
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Admin only
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        try {
            categoryDao.update(id, category);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    /**
     * DELETE /categories/{id}
     * Deletes a category by ID.
     *
     * @param id the ID of the category to delete
     */
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Admin only
    @ResponseStatus(HttpStatus.NO_CONTENT) // Respond with 204 No Content if successful
    public void deleteCategory(@PathVariable int id)
    {
        try {
            var category = categoryDao.getById(id);

            if (category == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            categoryDao.delete(id);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}