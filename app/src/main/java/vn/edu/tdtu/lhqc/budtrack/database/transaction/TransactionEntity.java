package vn.edu.tdtu.lhqc.budtrack.database.transaction;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey
    public long id;

    @NonNull
    public String type; // EXPENSE, INCOME, or OTHERS (stored as String)

    public long amount; // Amount in VND (stored as long)

    public long walletId; // Reference to Wallet

    public Long categoryId; // Reference to Category (nullable) - DEPRECATED

    public String categoryName; // Category name (for user-defined categories)

    public Integer categoryIconResId; // Category icon resource ID

    public String merchantName; // Name of merchant/place

    public String note; // Optional note/description

    public Long date; // Transaction date and time (stored as timestamp)

    public Double latitude; // Optional: location latitude

    public Double longitude; // Optional: location longitude

    public String address; // Optional: full address string

    public TransactionEntity() {
    }
}

