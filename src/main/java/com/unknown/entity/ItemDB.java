/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bobo
 */
public class ItemDB implements ItemDAO {

    @Override
    public List<Items> getItems() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CharacterDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        Connection c = null;
        List<Items> items = new ArrayList<Items>();
        try {
            c = new DBConnection().getConnection();
            PreparedStatement p = c.prepareStatement("SELECT * FROM items");
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                items.add(new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), rs.getString("type"), rs.getBoolean("isLegendary")));
            }
        } catch (SQLException e) {
        }
        return items;
    }
}
