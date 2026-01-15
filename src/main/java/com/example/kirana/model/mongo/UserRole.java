package com.example.kirana.model.mongo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserRole {
    @Id
    private String userRoleId;
    private String userId;
    private String storeId;
    private String roleId;
}
