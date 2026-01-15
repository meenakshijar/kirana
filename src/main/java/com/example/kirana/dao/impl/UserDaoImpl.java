package com.example.kirana.dao.impl;

import com.example.kirana.dao.UserDao;
import com.example.kirana.exception.UserNotFoundException;
import com.example.kirana.model.mongo.User;
import com.example.kirana.repository.mongo.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    public UserDaoImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User findByUserName(String userName) {
        System.out.println("Looking for userName = " + userName);
        System.out.println("All users count = " + userRepository.count());

        return userRepository.findByUserName(userName)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found: " + userName));
    }


    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
