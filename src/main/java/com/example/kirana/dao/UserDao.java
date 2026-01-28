package com.example.kirana.dao;
import com.example.kirana.model.mongo.User;


public interface UserDao  {
     User findByUserName(String userName);


}
