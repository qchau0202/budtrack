package vn.edu.tdtu.lhqc.budtrack.models;

import java.util.Date;

/**
 * Transaction model - represents a financial transaction (income or expense).
 * Simple model with essential fields.
 */
public class Transaction {
    private long id;
    private TransactionType type; // EXPENSE, INCOME, or OTHERS
    private long amount; // Amount in VND (stored as long)
    private long walletId; // Reference to Wallet
    private Long categoryId; // Reference to Category (nullable for income/others) - DEPRECATED: use categoryName and categoryIconResId
    private String categoryName; // Category name (for user-defined categories)
    private Integer categoryIconResId; // Category icon resource ID (for user-defined categories)
    private String merchantName; // Name of merchant/place (e.g., "Starbucks Coffee")
    private String note; // Optional note/description
    private Date date; // Transaction date and time
    private Double latitude; // Optional: location latitude for map display
    private Double longitude; // Optional: location longitude for map display
    private String address; // Optional: full address string

    // Default constructor for database
    public Transaction() {
    }

    // Constructor for creating new transactions
    public Transaction(TransactionType type, long amount, long walletId, Date date) {
        this.type = type;
        this.amount = amount;
        this.walletId = walletId;
        this.date = date;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getCategoryIconResId() {
        return categoryIconResId;
    }

    public void setCategoryIconResId(Integer categoryIconResId) {
        this.categoryIconResId = categoryIconResId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Helper method to check if transaction has location
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}

