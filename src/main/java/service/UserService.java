package service;

import entities.User;

public interface UserService {
    public User registerUser(User user);

    User getUserByAccountNumber(String account_no);

    public void saveUser(User user);

    User updateUser(User user);

    void temporaneo();
}