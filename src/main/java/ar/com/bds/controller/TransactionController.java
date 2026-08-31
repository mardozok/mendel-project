package ar.com.bds.controller;

import ar.com.bds.model.dto.TransactionInputDTO;
import ar.com.bds.model.dto.TransactionStatusDTO;
import ar.com.bds.model.dto.TransactionSumDTO;
import ar.com.bds.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @Operation(summary = "Save or replace a transaction in memory")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PutMapping(value = "/{transactionId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionStatusDTO saveTransaction(
            @PathVariable Long transactionId,
            @RequestBody @Valid TransactionInputDTO input) {
        TransactionInputDTO transaction = new TransactionInputDTO(
                transactionId,
                input.amount(),
                input.type(),
                input.parentId()
        );
        return service.saveTransaction(transaction);
    }

    @Operation(summary = "Get transaction ids by type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/types/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Long> getTransactionsByType(@PathVariable String type) {
        return service.getTransactionIdsByType(type);
    }

    @Operation(summary = "Get transitive transaction amount sum")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping(value = "/sum/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionSumDTO getTransactionSum(@PathVariable Long transactionId) {
        return service.getTransactionSum(transactionId);
    }
}
