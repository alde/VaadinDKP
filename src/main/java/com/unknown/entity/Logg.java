
package com.unknown.entity;

import com.unknown.entity.character.SiteUser;
import com.vaadin.Application;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Logg {

        private void addLog(String message, String username, String type) {
                Date d = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String date = dateFormat.format(d);
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("INSERT INTO log (date, username, message, type) VALUES (?,?,?,?)");
                        p.setString(1, date);
                        p.setString(2, username);
                        p.setString(3, message);
                        p.setString(4, type);
                        p.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                }
        }

        public List<Log> readLog() {
                List<Log> log = new ArrayList<Log>();
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM log");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                Log l = new Log(rs.getString("date"), rs.getString("username"), rs.getString("message"), rs.getString("type"));
                                log.add(l);
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                }
                return log;
        }

        public void addLog(String message, String string) {
                String name = "";
                Application app = UnknownEntityDKP.getInstance();
                if (app == null || (SiteUser) app.getUser() == null) {
                        name = "<unknown>";
                } else {
                        name = ((SiteUser) app.getUser()).getName();
                }
                this.addLog(message, name, string);
        }
}
