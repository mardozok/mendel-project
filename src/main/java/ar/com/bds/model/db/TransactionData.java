package ar.com.bds.model.db;

public record TransactionData(
        Long id,
        Double amount,
        String type,
        Long parentId
) {
}
