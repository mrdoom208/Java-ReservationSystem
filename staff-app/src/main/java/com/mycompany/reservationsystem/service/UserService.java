package com.mycompany.reservationsystem.service;

import com.mycompany.reservationsystem.api.ApiClient;
import com.mycompany.reservationsystem.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserService {

    public static List<Map<String, Object>> getAllUsers() {
        return ApiClient.getUsers();
    }

    public static Map<String, Object> getUserById(Long id) {
        return ApiClient.getUserById(id);
    }

    public static Map<String, Object> createUser(Object user) {
        return ApiClient.createUser(user);
    }

    public static void updateUser(Long id, Object user) {
        ApiClient.updateUser(id, user);
    }

    public static void deleteUser(Long id) {
        ApiClient.deleteUser(id);
    }

    public static List<User> loadAllUsers() {
        List<Map<String, Object>> response = ApiClient.getUsers();
        List<User> users = new ArrayList<>();
        for (Map<String, Object> map : response) {
            User user = new User();
            user.setId(((Number) map.getOrDefault("id", 0)).longValue());
            user.setUsername((String) map.get("username"));
            user.setPassword((String) map.get("password"));
            user.setFirstname((String) map.get("firstname"));
            user.setLastname((String) map.get("lastname"));
            String pos = (String) map.get("position");
            if (pos != null) {
                user.setPosition(User.Position.valueOf(pos));
            }
            user.setStatus((String) map.get("status"));
            users.add(user);
        }
        return users;
    }

    public static long countCustomers() {
        return ApiClient.countCustomers();
    }

    public static User save(User user) {
        if (user.getId() == null) {
            ApiClient.createUser(user);
            return user;
        } else {
            ApiClient.updateUser(user.getId(), user);
            return user;
        }
    }

    public static User findByUsername(String username) {
        List<Map<String, Object>> response = ApiClient.getUsers();
        for (Map<String, Object> map : response) {
            if (username.equals(map.get("username"))) {
                User user = new User();
                user.setId(((Number) map.getOrDefault("id", 0)).longValue());
                user.setUsername((String) map.get("username"));
                user.setPassword((String) map.get("password"));
                user.setFirstname((String) map.get("firstname"));
                user.setLastname((String) map.get("lastname"));
                String pos = (String) map.get("position");
                if (pos != null) {
                    user.setPosition(User.Position.valueOf(pos));
                }
                user.setStatus((String) map.get("status"));
                return user;
            }
        }
        return null;
    }
}
