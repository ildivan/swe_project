package backend.server.json.objects;



public class User {

    private String name;
    private String password;
    private String role;

    public User(String name, String password, String role) {
        this.name = name;
        this.password = password;
        this.role = role;
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

    


/* 
    public void modify(String nome, String newPassword, String newRuolo) {
        List<JsonObject> usersList = fileManager.readUsersFromFile(PATH, "users");

        for (JsonObject user : usersList) {
            if (user.get("nome").getAsString().equals(nome)) {
                if (newPassword != null && !newPassword.isEmpty()) {
                    user.addProperty("password", newPassword);  //potremmo cryptarla con la libreiria messa
                }
                if (newRuolo != null && !newRuolo.isEmpty()) {
                    user.addProperty("ruolo", newRuolo);
                }
                fileManager.writeUsersToFile(PATH, usersList, "users");
                return;
            }
        }
        System.out.println("Utente non trovato.");
    }
        */
}
