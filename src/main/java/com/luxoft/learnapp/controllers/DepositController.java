package com.luxoft.learnapp.controllers;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.luxoft.learnapp.api.model.AddDepositSchema;
import com.luxoft.learnapp.api.model.CloseDepositSchema;
import com.luxoft.learnapp.api.model.CreateDepositSchema;
import com.luxoft.learnapp.api.model.WithdrawDepositSchema;
import com.luxoft.learnapp.api.web.DepositApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping(value = "/app")
public class DepositController implements DepositApi {

    private IMap<String, String> depositGuidToAuthTokenMap;
    private IMap<String, Integer> depositGuidToSumMap;


    @Autowired
    public DepositController(@Qualifier("hazelcastInstance") HazelcastInstance hazelcastInstance) {
        depositGuidToAuthTokenMap = hazelcastInstance.getMap("authTokenToDepositGuidMap");
        depositGuidToSumMap = hazelcastInstance.getMap("depositGuidToSumMap");
    }

    @Override
    public ResponseEntity<Void> depositOperationsAddPut(@Valid AddDepositSchema addDepositSchema) {
        if (isValidOperation(addDepositSchema.getAuthToken(), addDepositSchema.getDepositGuid())) {
            Integer oldSum = depositGuidToSumMap.get(addDepositSchema.getDepositGuid());
            depositGuidToSumMap.put(addDepositSchema.getDepositGuid(), oldSum + addDepositSchema.getAmount());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Override
    public ResponseEntity<Void> depositOperationsClosePut(@Valid CloseDepositSchema closeDepositSchema) {
        if (isValidOperation(closeDepositSchema.getAuthToken(), closeDepositSchema.getDepositGuid())) {
            depositGuidToSumMap.delete(closeDepositSchema.getDepositGuid());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    private boolean isValidOperation(String authToken, String depositGuid) {
        return authToken.equals(
                depositGuidToAuthTokenMap.get(depositGuid));
    }

    @Override
    public ResponseEntity<String> depositOperationsNewPost(@Valid CreateDepositSchema createDepositSchema) {
        String depositGuid = UUID.randomUUID().toString();
        depositGuidToSumMap.put(depositGuid, createDepositSchema.getAmount());
        depositGuidToAuthTokenMap.put(depositGuid, createDepositSchema.getAuthToken());
        return ResponseEntity.status(HttpStatus.CREATED).body("DepositGUID = " + createDepositSchema.getAuthToken());
    }

    @Override
    public ResponseEntity<Void> depositOperationsWithdrawPut(@Valid WithdrawDepositSchema withdrawDepositSchema) {
        if (isValidOperation(withdrawDepositSchema.getAuthToken(), withdrawDepositSchema.getDepositGuid())) {
            Integer oldSum = depositGuidToSumMap.get(withdrawDepositSchema.getDepositGuid());
            depositGuidToSumMap.put(withdrawDepositSchema.getDepositGuid(), oldSum - withdrawDepositSchema.getAmount());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
