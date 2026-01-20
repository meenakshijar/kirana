package com.example.kirana.service;

import com.example.kirana.dto.StoreRequest;
import com.example.kirana.dto.StoreResponse;
import com.example.kirana.model.mongo.Store;
import com.example.kirana.repository.mongo.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public StoreResponse createStore(StoreRequest request) {

        String storeId = "STORE_" + UUID.randomUUID();

        Store store = new Store();
        store.setStoreId(storeId);
        store.setStoreName(request.getStoreName());
        store.setCity(request.getCity());
        store.setCountry(request.getCountry());
        store.setAddress(request.getAddress());
        store.setPincode(request.getPincode());
        store.setContactEmail(request.getContactEmail());
        store.setContactNumber(request.getContactNumber());
        store.setBaseCurrency(request.getBaseCurrency());
        store.setActive(true);

        storeRepository.save(store);

        StoreResponse response = new StoreResponse();
        response.setStoreId(store.getStoreId());
        response.setStoreName(store.getStoreName());
        response.setCity(store.getCity());
        response.setCountry(store.getCountry());
        response.setBaseCurrency(store.getBaseCurrency());
        response.setMessage("Store registered successfully");

        return response;
    }

    public Store getStoreById(String storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeId));
    }
}
