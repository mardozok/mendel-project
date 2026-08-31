package ar.com.bds.repository;

import ar.com.bds.model.db.TransactionData;

import java.util.List;

public interface TransactionRepository {

    void save(TransactionData transactionData);

    TransactionData findById(Long transactionId);

    List<TransactionData> findByType(String type);

    List<TransactionData> findByParentId(Long parentId);
}
