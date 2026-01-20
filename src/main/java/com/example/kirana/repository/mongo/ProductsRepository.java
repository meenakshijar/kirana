package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.Products;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductsRepository extends MongoRepository<Products, String> {

    List<Products> findByStoreId(String storeId);
}
