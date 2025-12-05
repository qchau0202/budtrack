package vn.edu.tdtu.lhqc.budtrack.database.category;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    public int iconResId;

    public CategoryEntity() {
    }

    public CategoryEntity(@NonNull String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }
}

