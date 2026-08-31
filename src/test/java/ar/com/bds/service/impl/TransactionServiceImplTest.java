package ar.com.bds.service.impl;

import ar.com.bds.exception.DataBaseOperationException;
import ar.com.bds.exception.InternalGenericException;
import ar.com.bds.exception.TransactionNotFoundException;
import ar.com.bds.model.db.TransactionData;
import ar.com.bds.model.dto.TransactionInputDTO;
import ar.com.bds.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    private TransactionServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TransactionServiceImpl(repository);
    }

    @Test
    void shouldSaveTransactionUsingIdFromDto() {
        TransactionInputDTO input = new TransactionInputDTO(10L, 5000D, "cars", null);

        assertThat(service.saveTransaction(input).status()).isEqualTo("ok");

        verify(repository).save(new TransactionData(10L, 5000D, "cars", null));
    }

    @Test
    void shouldReplaceTransactionWithSameId() {
        TransactionInputDTO input = new TransactionInputDTO(10L, 8000D, "shopping", null);

        service.saveTransaction(input);

        verify(repository).save(new TransactionData(10L, 8000D, "shopping", null));
    }

    @Test
    void shouldReturnTransactionIdsByType() {
        when(repository.findByType("cars")).thenReturn(Arrays.asList(
                new TransactionData(10L, 5000D, "cars", null),
                new TransactionData(13L, 2000D, "cars", null)
        ));

        assertThat(service.getTransactionIdsByType("cars"))
                .containsExactly(10L, 13L);
    }

    @Test
    void shouldReturnEmptyListWhenTypeDoesNotExist() {
        when(repository.findByType("unknown")).thenReturn(Collections.emptyList());

        assertThat(service.getTransactionIdsByType("unknown")).isEmpty();
    }

    @Test
    void shouldCalculateTransitiveSum() {
        when(repository.findById(10L)).thenReturn(new TransactionData(10L, 5000D, "cars", null));
        when(repository.findById(11L)).thenReturn(new TransactionData(11L, 10000D, "shopping", 10L));
        when(repository.findById(12L)).thenReturn(new TransactionData(12L, 5000D, "shopping", 11L));
        when(repository.findByParentId(10L)).thenReturn(Collections.singletonList(
                new TransactionData(11L, 10000D, "shopping", 10L)));
        when(repository.findByParentId(11L)).thenReturn(Collections.singletonList(
                new TransactionData(12L, 5000D, "shopping", 11L)));
        when(repository.findByParentId(12L)).thenReturn(Collections.emptyList());

        assertThat(service.getTransactionSum(10L).sum()).isEqualTo(20000D);
    }

    @Test
    void shouldNotIncludeAncestorsInSum() {
        when(repository.findById(11L)).thenReturn(new TransactionData(11L, 10000D, "shopping", 10L));
        when(repository.findById(12L)).thenReturn(new TransactionData(12L, 5000D, "shopping", 11L));
        when(repository.findByParentId(11L)).thenReturn(Collections.singletonList(
                new TransactionData(12L, 5000D, "shopping", 11L)));
        when(repository.findByParentId(12L)).thenReturn(Collections.emptyList());

        assertThat(service.getTransactionSum(11L).sum()).isEqualTo(15000D);
    }

    @Test
    void shouldReturnOwnAmountWhenTransactionHasNoChildren() {
        when(repository.findById(10L)).thenReturn(new TransactionData(10L, 5000D, "cars", null));
        when(repository.findByParentId(10L)).thenReturn(Collections.emptyList());

        assertThat(service.getTransactionSum(10L).sum()).isEqualTo(5000D);
    }

    @Test
    void shouldThrowWhenTransactionDoesNotExist() {
        when(repository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.getTransactionSum(999L))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: '999'");
    }

    @Test
    void shouldThrowDatabaseExceptionWhenFindByTypeFails() {
        when(repository.findByType("cars")).thenThrow(new RuntimeException("database error"));

        assertThatThrownBy(() -> service.getTransactionIdsByType("cars"))
                .isInstanceOf(DataBaseOperationException.class);
    }

    @Test
    void shouldThrowDatabaseExceptionWhenSavingFails() {
        doThrow(new RuntimeException("database error"))
                .when(repository).save(any(TransactionData.class));

        TransactionInputDTO input = new TransactionInputDTO(10L, 5000D, "cars", null);

        assertThatThrownBy(() -> service.saveTransaction(input))
                .isInstanceOf(DataBaseOperationException.class);
    }

    @Test
    void shouldThrowInternalExceptionWhenFindByIdFails() {
        when(repository.findById(10L)).thenThrow(new RuntimeException("database error"));

        assertThatThrownBy(() -> service.getTransactionSum(10L))
                .isInstanceOf(InternalGenericException.class);
    }
}
