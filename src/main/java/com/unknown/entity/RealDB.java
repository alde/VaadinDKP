/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.unknown.entity;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RealDB implements CharacherDAO {

        public Connection connect() throws SQLException {
        String userName = "root", userPassword = "piccolo", databaseURL = "jdbc:mysql://unknown-entity.com:3306/dkp";
        Connection conn = null;
        conn = DriverManager.getConnection(databaseURL, userName, userPassword);
        return conn;
    }

    @Override
    public List<User> getUsers() {
        Connection c = null;
        List<User> users = new ArrayList<User>();
        try {
            c = connect();
            PreparedStatement p = c.prepareStatement("SELECT * FROM characters JOIN character_classes USING(id)");
            ResultSet rs = p.executeQuery();
            Object[] rows;
            while (rs.next()) {
                Role role = Role.valueOf(rs.getString("character_classes.name"));
                users.add(new User(rs.getString("characters.name"),role,rs.getBoolean("characters.active")));
            }
        } catch (SQLException e) {
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException ex) {
                    Logger.getLogger(RealDB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    return users;
    }

    @Override
    public Collection<User> getUsersWithRole(final Role role) {
    return Collections2.filter(getUsers(), new HasRolePredicate(role));
    }

    private static class HasRolePredicate implements Predicate<User> {

	private final Role role;

	public HasRolePredicate(Role role) {
	    this.role = role;
	}

	@Override
	public boolean apply(User user) {
	    return user.getRole().equals(role);
	}
    }

}
