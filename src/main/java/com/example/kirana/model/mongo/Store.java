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
    private String storeId; // This now serves as the primary key (_id)

    private String storeName;
    private boolean isActive;
    private String city;
    private String country;
    private String address;
    private String pincode;
    private String contactEmail;
    private String contactNumber;
    private String baseCurrency;
}
