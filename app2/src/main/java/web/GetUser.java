package web;


import model.User;

import java.sql.*;
import java.util.Objects;

public class GetUser {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sso_test?characterEncoding=utf-8&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PSWD = "wkf123";

    public static User getUser(String name){
        User user =new User();
        user.setName("null");
        user.setPassword("null");
        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName(JDBC_DRIVER).newInstance();
            conn = DriverManager.getConnection(DB_URL, USER, PSWD);

            stmt = conn.createStatement();
            String sql = "select * from user";
            ResultSet rs = stmt.executeQuery(sql);

            int index = 0;
            while (rs.next()) {
                index++;

                Integer id = rs.getInt("id");
                String userName = rs.getString("userName");
                Integer age = rs.getInt("useAge");
                String password = rs.getString("usePwd");
                //System.out.println(id+userName+age+password);
                if(Objects.equals(name, userName)){
                    System.out.println("yes");
                    user.setId(id);
                    user.setName(userName);
                    user.setAge(age);
                    user.setPassword(password);
                    System.out.println(user.getPassword());
                }
                else{
                    System.out.println("no");

                }
            }
        } catch (Exception e) {
            System.out.print("DB/SQL ERROR:" + e.getMessage());
        } finally {
            try {
                assert stmt != null;
                stmt.close();
                conn.close();
            } catch (SQLException e) {
                System.out.print("Can't close stmt/conn because of " + e.getMessage());
            }

        }
        return user;
    }


//   public static void main(String[] args) {
//        User u=new User();
//        u=getUser("wkf");
//        System.out.println(u.getPassword());
//    }
}


