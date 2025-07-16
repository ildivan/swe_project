package server.authservice;



public class User {

    private String name;
    private String password;
    private String role;
    private boolean active;

    public User(String name, String password, String role) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    public User(String name, String password, String role, boolean isActive) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.active = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


}
