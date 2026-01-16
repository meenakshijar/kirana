package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.Transactions;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends MongoRepository<Transactions, String> {

    List<Transactions> findByStoreId(String storeId);
}
