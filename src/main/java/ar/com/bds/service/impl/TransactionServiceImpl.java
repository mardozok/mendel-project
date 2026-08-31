package ar.com.bds.service.impl;

import ar.com.bds.exception.DataBaseOperationException;
import ar.com.bds.exception.InternalGenericException;
import ar.com.bds.exception.TransactionNotFoundException;
import ar.com.bds.model.db.TransactionData;
import ar.com.bds.model.dto.TransactionInputDTO;
import ar.com.bds.model.dto.TransactionStatusDTO;
import ar.com.bds.model.dto.TransactionSumDTO;
import ar.com.bds.repository.TransactionRepository;
import ar.com.bds.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository repository;

    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public TransactionStatusDTO saveTransaction(TransactionInputDTO input) {
        log.info("Saving transaction with id '{}'", input.id());

        TransactionData transactionData = new TransactionData(
                input.id(),
                input.amount(),
                input.type(),
                input.parentId()
        );

        saveTransactionData(transactionData);
        return new TransactionStatusDTO("ok");
    }

    @Override
    public List<Long> getTransactionIdsByType(String type) {
        log.info("Getting transaction ids by type '{}'", type);
        return findTransactionsByType(type).stream()
                .map(TransactionData::id)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionSumDTO getTransactionSum(Long transactionId) {
        log.info("Calculating transaction sum for id '{}'", transactionId);
        return new TransactionSumDTO(calculateSum(transactionId, new HashSet<>()));
    }

    private double calculateSum(Long transactionId, Set<Long> visited) {
        if (!visited.add(transactionId)) {
            return 0D;
        }

        TransactionData transaction = findTransaction(transactionId);
        double sum = transaction.amount();

        for (TransactionData child : findChildren(transactionId)) {
            sum += calculateSum(child.id(), visited);
        }

        return sum;
    }

    private TransactionData findTransaction(Long transactionId) {
        try {
            TransactionData data = repository.findById(transactionId);
            if (data == null) {
                throw new TransactionNotFoundException(
                        "Transaction not found with id: '" + transactionId + "'"
                );
            }
            return data;
        } catch (TransactionNotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Error obtaining transaction data", exception);
            throw new InternalGenericException(
                    "Error obtaining transaction data for transaction id: '" + transactionId + "'",
                    exception
            );
        }
    }

    private List<TransactionData> findTransactionsByType(String type) {
        try {
            return repository.findByType(type);
        } catch (Exception exception) {
            log.error("Error finding transaction data by type", exception);
            throw new DataBaseOperationException(
                    "Error finding transaction data by type: '" + type + "'",
                    exception
            );
        }
    }

    private List<TransactionData> findChildren(Long parentId) {
        try {
            return repository.findByParentId(parentId);
        } catch (Exception exception) {
            log.error("Error finding child transactions", exception);
            throw new DataBaseOperationException(
                    "Error finding child transactions for parent id: '" + parentId + "'",
                    exception
            );
        }
    }

    private void saveTransactionData(TransactionData transactionData) {
        try {
            repository.save(transactionData);
        } catch (Exception exception) {
            log.error("Error saving transaction data", exception);
            throw new DataBaseOperationException(
                    "Error saving transaction data for transaction id: '" + transactionData.id() + "'",
                    exception
            );
        }
    }
}
