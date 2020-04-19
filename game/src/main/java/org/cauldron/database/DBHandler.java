package org.cauldron.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

//This class will act like mysql client
public class DBHandler {

    static String url = "jdbc:mysql://0.tcp.ngrok.io:14243/";

    //At the moment, we use my account to access mysql
    private final String schema = "demo";
    private final String gameUser = "cxk858";
    private final String password = "Navior11!!";
    private String table = "user";

    public Connection myConn;
    public Statement stmt;


    //Create new data into database
    //insert into <table name>
    //column names (userID, email, password, login_status, match_history)
    //VALUES
    //all varchar type must have ''

    //Bring data
    //select <userID> from <table name>, which is user

    public DBHandler() {
        init();
    }

    public void init() {
        try {
            DriverManager.setLoginTimeout(10);
            myConn = DriverManager.getConnection(url + schema + "?serverTimezone=GMT", gameUser, password);

            stmt = myConn.createStatement();

            System.out.println("local server is initialised");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //print requested database
    public void print() {

        try {
            ResultSet myRs = stmt.executeQuery("select * from " + table);

            while (myRs.next()) {
                System.out.println(myRs.getString("userID") + ", " + myRs.getString("password") + ", " + myRs.getString("email"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //save data into database
    public void save(Database db) {

        try {
            String sql = "insert into " + table
                    + " (userID, email, password) "
                    + "VALUES "
                    + db.values();

            stmt.executeUpdate(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete data from database
    public void delete(Database db) {
        try {
            String sql = "delete from " + table + " where userID= '" + db.getUserID() + "'";

            if (stmt.executeUpdate(sql) == 0) {
                throw new Exception("cannot find the user");
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
    //update <table>
    //set <column> = <new data>
    //where <primary key> (,which is userID) = <userID>

    public boolean update(Database db) {
        try {
            String sql = "select * from " + table;
            stmt.executeQuery(sql);

            sql = "update " + table
                    + " set email = '" + db.getEmail() + "'"
                    + ", password = '" + db.getPassword() + "'"
                    + " where userID = '" + db.getUserID() + "'";

            if(!validEmail(db.getEmail())){
                return false;
            }

            if (stmt.executeUpdate(sql) == 0) {
                return false;
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return true;
    }

    //For login system
    public Database get(String userID) {
        Database db = new Database(userID);
        try {
            String sql = "select * from " + table
                    + " where userID = '" + db.getUserID() + "'";

            ResultSet dbRs = stmt.executeQuery(sql);

            while (dbRs.next()) {
                db.setEmail(dbRs.getString("email"));
                db.setPassword(dbRs.getString("password"));

                return db;
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return db;
    }

    public boolean verify(Database db) {
        Database storedDB = this.get(db.getUserID());
        if (storedDB == null)
            return false;

        return storedDB.getPassword().equals(db.getPassword());
    }

    public boolean isExist(String userID) {

        try {
            String sql = "select * from " + table + " where userID = '" + userID + "'";

            ResultSet myRs = stmt.executeQuery(sql);

            if (myRs.next() && !myRs.isAfterLast())
                return true;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTable() {
        return this.table;
    }

    private boolean validEmail(String s) {
        return s.matches("^([a-zA-Z0-9_\\-\\.\\+]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
    }

    public String toString(Database db) {
        String str = "UserID: " + db.getUserID()
                + " Password: " + db.getPassword()
                + " email: " + db.getEmail();
        return str;
    }
}