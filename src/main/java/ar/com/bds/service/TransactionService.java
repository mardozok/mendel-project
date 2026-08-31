package ar.com.bds.service;

import ar.com.bds.model.dto.TransactionInputDTO;
import ar.com.bds.model.dto.TransactionStatusDTO;
import ar.com.bds.model.dto.TransactionSumDTO;

import java.util.List;

public interface TransactionService {

    TransactionStatusDTO saveTransaction(TransactionInputDTO input);

    List<Long> getTransactionIdsByType(String type);

    TransactionSumDTO getTransactionSum(Long transactionId);
}
