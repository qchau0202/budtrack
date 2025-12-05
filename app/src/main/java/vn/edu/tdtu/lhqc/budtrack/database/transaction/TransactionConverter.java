package vn.edu.tdtu.lhqc.budtrack.database.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.edu.tdtu.lhqc.budtrack.models.Transaction;
import vn.edu.tdtu.lhqc.budtrack.models.TransactionType;

public class TransactionConverter {
    public static TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();
        entity.id = transaction.getId();
        entity.type = transaction.getType() != null ? transaction.getType().name() : TransactionType.EXPENSE.name();
        entity.amount = transaction.getAmount();
        entity.walletId = transaction.getWalletId();
        entity.categoryId = transaction.getCategoryId();
        entity.categoryName = transaction.getCategoryName();
        entity.categoryIconResId = transaction.getCategoryIconResId();
        entity.merchantName = transaction.getMerchantName();
        entity.note = transaction.getNote();
        entity.date = transaction.getDate() != null ? transaction.getDate().getTime() : null;
        entity.latitude = transaction.getLatitude();
        entity.longitude = transaction.getLongitude();
        entity.address = transaction.getAddress();
        return entity;
    }

    public static Transaction toModel(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        Transaction transaction = new Transaction();
        transaction.setId(entity.id);
        
        // Convert type string to enum
        try {
            transaction.setType(TransactionType.valueOf(entity.type));
        } catch (IllegalArgumentException e) {
            transaction.setType(TransactionType.EXPENSE);
        }
        
        transaction.setAmount(entity.amount);
        transaction.setWalletId(entity.walletId);
        transaction.setCategoryId(entity.categoryId);
        transaction.setCategoryName(entity.categoryName);
        transaction.setCategoryIconResId(entity.categoryIconResId);
        transaction.setMerchantName(entity.merchantName);
        transaction.setNote(entity.note);
        transaction.setDate(entity.date != null ? new Date(entity.date) : null);
        transaction.setLatitude(entity.latitude);
        transaction.setLongitude(entity.longitude);
        transaction.setAddress(entity.address);
        return transaction;
    }

    public static List<Transaction> toModelList(List<TransactionEntity> entities) {
        List<Transaction> transactions = new ArrayList<>();
        for (TransactionEntity entity : entities) {
            transactions.add(toModel(entity));
        }
        return transactions;
    }
}

