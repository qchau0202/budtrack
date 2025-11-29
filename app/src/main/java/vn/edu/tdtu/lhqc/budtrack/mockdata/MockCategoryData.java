package vn.edu.tdtu.lhqc.budtrack.mockdata;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.models.Category;

/**
 * Mock data generator for Category objects.
 * Provides sample category data for testing and development.
 */
public class MockCategoryData {

    /**
     * Generates a list of sample categories.
     * 
     * @return List of sample Category objects
     */
    public static List<Category> getSampleCategories() {
        List<Category> categories = new ArrayList<>();

        // Food category
        Category food = new Category("Food", R.drawable.ic_food_24dp, "#7ed957");
        food.setId(1);
        categories.add(food);

        // Shopping category
        Category shopping = new Category("Shopping", R.drawable.ic_shopping_24dp, "#F3BB1B");
        shopping.setId(2);
        categories.add(shopping);

        // Transport category
        Category transport = new Category("Transport", R.drawable.ic_transport_24dp, "#B5282D");
        transport.setId(3);
        categories.add(transport);

        // Home category
        Category home = new Category("Home", R.drawable.ic_home_24dp, "#808080");
        home.setId(4);
        categories.add(home);

        return categories;
    }

    /**
     * Gets a category by name.
     * 
     * @param name Category name
     * @return Category object or null if not found
     */
    public static Category getCategoryByName(String name) {
        for (Category category : getSampleCategories()) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        return null;
    }
}

