package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoreRepository extends MongoRepository<Store, String> {
}
