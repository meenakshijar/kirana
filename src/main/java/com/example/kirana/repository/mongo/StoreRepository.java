package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.Products;
import com.example.kirana.model.mongo.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends MongoRepository<Store, String> {
    Optional<Store> findByStoreId(String storeId);
}
