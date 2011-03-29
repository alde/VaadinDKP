/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author alde
 */
public class Logg {

        public static void addLog(String message, String username, String type) {
                DBConnection c = new DBConnection();
                Date d = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String date = dateFormat.format(d);
                try {
                        PreparedStatement p = c.prepareStatement("INSERT INTO log (date, username, message, type) VALUES (?,?,?,?)");
                        p.setString(1, date);
                        p.setString(2, username);
                        p.setString(3, message);
                        p.setString(4, type);
                        p.executeUpdate();
                      } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
        }

        public static List<Log> readLog() {
                DBConnection c = new DBConnection();
                List<Log> log = new ArrayList<Log>();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM log");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                Log l = new Log(rs.getString("date"), rs.getString("username"), rs.getString("message"), rs.getString("type"));
                                log.add(l);
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return log;
        }
}
