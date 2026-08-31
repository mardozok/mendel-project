package ar.com.bds.controller;

import ar.com.bds.MendelJavaChallengeApplication;
import ar.com.bds.model.dto.TransactionInputDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MendelJavaChallengeApplication.class)
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSaveTransactionWithoutParent() throws Exception {
        performPut(transaction(1001L, 5000.0, "cars", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void shouldSaveTransactionWithParent() throws Exception {
        performPut(transaction(1002L, 10000.0, "shopping", 1001L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReplaceTransaction() throws Exception {
        performPut(transaction(1003L, 1000.0, "cars", null))
                .andExpect(status().isOk());

        performPut(transaction(1003L, 2000.0, "shopping", null))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transactions/types/shopping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem(1003)));
    }

    @Test
    void shouldReturnTransactionsByType() throws Exception {
        performPut(transaction(1004L, 1000.0, "cars", null))
                .andExpect(status().isOk());

        performPut(transaction(1005L, 2000.0, "cars", null))
                .andExpect(status().isOk());

        mockMvc.perform(get("/transactions/types/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem(1004)))
                .andExpect(jsonPath("$", hasItem(1005)));
    }

    @Test
    void shouldReturnEmptyListWhenTypeDoesNotExist() throws Exception {
        mockMvc.perform(get("/transactions/types/does-not-exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldCalculateTransitiveSum() throws Exception {
        performPut(transaction(1010L, 5000.0, "cars", null))
                .andExpect(status().isOk());

        performPut(transaction(1011L, 10000.0, "shopping", 1010L))
                .andExpect(status().isOk());

        performPut(transaction(1012L, 5000.0, "shopping", 1011L))
                .andExpect(status().isOk());

        performGetSum(1010L, 20000.0);
        performGetSum(1011L, 15000.0);
    }

    @Test
    void shouldReturnOwnAmountWhenTransactionHasNoChildren() throws Exception {
        performPut(transaction(1013L, 5000.0, "cars", null))
                .andExpect(status().isOk());

        performGetSum(1013L, 5000.0);
    }

    @Test
    void shouldReturnNotFoundWhenTransactionDoesNotExist() throws Exception {
        mockMvc.perform(get("/transactions/sum/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        performPut(transaction(1014L, -1.0, "", null))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsMissing() throws Exception {
        performPut("""
                {
                    "type": "cars"
                }
                """, 1015L)
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTypeIsMissing() throws Exception {
        performPut("""
                {
                    "amount": 5000
                }
                """, 1016L)
                .andExpect(status().isBadRequest());
    }

    private TransactionInputDTO transaction(
            Long id,
            Double amount,
            String type,
            Long parentId) {

        return new TransactionInputDTO(id, amount, type, parentId);
    }

    private ResultActions performPut(TransactionInputDTO transaction) throws Exception {
        return performPut(
                objectMapper.writeValueAsString(transaction),
                transaction.id()
        );
    }

    private ResultActions performPut(String body, Long transactionId) throws Exception {
        return mockMvc.perform(
                put("/transactions/{transactionId}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
    }

    private void performGetSum(Long transactionId, Double expectedSum) throws Exception {
        mockMvc.perform(get("/transactions/sum/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value(expectedSum));
    }
}