package ar.com.bds.repository;

import ar.com.bds.model.db.TransactionData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, TransactionData> transactions = new ConcurrentHashMap<>();

    @Override
    public void save(TransactionData transactionData) {
        transactions.put(transactionData.id(), transactionData);
    }

    @Override
    public TransactionData findById(Long transactionId) {
        return transactions.get(transactionId);
    }

    @Override
    public List<TransactionData> findByType(String type) {
        List<TransactionData> result = new ArrayList<>();
        for (TransactionData transactionData : transactions.values()) {
            if (transactionData.type().equals(type)) {
                result.add(transactionData);
            }
        }
        result.sort(Comparator.comparing(TransactionData::id));
        return result;
    }

    @Override
    public List<TransactionData> findByParentId(Long parentId) {
        List<TransactionData> result = new ArrayList<>();
        for (TransactionData transactionData : transactions.values()) {
            if (parentId.equals(transactionData.parentId())) {
                result.add(transactionData);
            }
        }
        result.sort(Comparator.comparing(TransactionData::id));
        return result;
    }
}
