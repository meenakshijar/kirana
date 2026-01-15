package com.example.kirana.model.mongo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    private String storeId;
    private String storeRegion;
    private String region;
    private String baseCurrency;
}
