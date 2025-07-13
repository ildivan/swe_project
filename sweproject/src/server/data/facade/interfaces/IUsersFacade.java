package server.data.facade.interfaces;

import java.util.List;
import java.util.Optional;

import server.authservice.User;

public interface IUsersFacade {
    public void addUsers(List<User> users);
    public List<User> getUsers();
    public User getUser(String username);
    public boolean addUser(User user);
    public boolean modifyUser(
        String username, 
        Optional<String> newName,
        Optional<String> newPassword,
        Optional<String> newRole,
        Optional<Boolean> newActive,
        Optional<Boolean> newDeleted);
    public boolean deleteUser(String username);
    public boolean doesUserExist(String username);
}
