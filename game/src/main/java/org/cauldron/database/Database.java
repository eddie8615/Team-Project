package org.cauldron.database;

public class Database {
    private String userID;
    private String email;
    private String password;

    public Database(String userID, String email, String password) {
        this.userID = userID;
        this.email = email;
        this.password = password;
    }

    public Database(String userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public Database(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String values() {
        String str = "('" + this.userID + "', '" + this.email + "', '" + this.password + "')";
        return str;
    }

    @Override
    public String toString() {
        String str = "UserID: " + this.userID
                + " Password: " + this.password
                + " email: " + this.email;
        return str;
    }
}
