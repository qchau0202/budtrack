package vn.edu.tdtu.lhqc.budtrack.database.category.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.database.category.CategoryEntity;

@Dao
public interface CategoryDao {
    @Insert
    long insert(CategoryEntity category);

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    long insertOrReplace(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryEntity> getAllCategories();

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    CategoryEntity getCategoryById(long id);

    @Query("SELECT * FROM categories WHERE name = :name AND iconResId = :iconResId LIMIT 1")
    CategoryEntity getCategoryByNameAndIcon(String name, int iconResId);

    @Query("SELECT * FROM categories WHERE name = :name AND iconResId = :iconResId AND (name != :excludeName OR iconResId != :excludeIconResId) LIMIT 1")
    CategoryEntity getCategoryByNameAndIconExcluding(String name, int iconResId, String excludeName, int excludeIconResId);

    @Query("DELETE FROM categories WHERE name = :name AND iconResId = :iconResId")
    void deleteByNameAndIcon(String name, int iconResId);

    @Query("DELETE FROM categories WHERE id = :id")
    void deleteById(long id);
}

