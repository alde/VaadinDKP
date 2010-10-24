/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.database;

import com.unknown.entity.DBConnection;
import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.items.ItemLooter;
import com.unknown.entity.items.ItemPrices;
import com.unknown.entity.items.Items;
import com.unknown.entity.raids.RaidItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
                Connection c = null;
                List<Items> items = new ArrayList<Items>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM items");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                String temp = rs.getString("type");
                                Type tempType = typeFromString(temp);
                                Items tempitem = new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), tempType);
                                tempitem.addLooterList(getLootersFormItems(rs.getInt("id")));
                                items.add(tempitem);
                        }
                } catch (SQLException e) {
                } finally {
                        closeConnection(c);
                }
                return items;
        }

        private Collection<ItemLooter> getLootersFormItems(int itemId) {
                List<ItemLooter> looters = new ArrayList<ItemLooter>();
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM loots JOIN characters JOIN items JOIN raids WHERE loots.character_id=characters.id AND loots.item_id=? AND items.id=loots.item_id AND raids.id=loots.raid_id");
                        p.setInt(1, itemId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                ItemLooter templooter = new ItemLooter();
                                templooter.setName(rs.getString("characters.name"));
                                templooter.setPrice(rs.getDouble("loots.price"));
                                templooter.setRaid(rs.getString("raids.comment"));
                                templooter.setDate(rs.getString("raids.date"));
                                templooter.setId(rs.getInt("loots.id"));
                                looters.add(templooter);
                        }
                } catch (SQLException e) {
                } finally {
                        closeConnection(c);
                }

                return looters;
        }

        @Override
        public List<ItemPrices> getDefaultPrices() throws SQLException {
                Connection c = null;
                List<ItemPrices> prices = new ArrayList<ItemPrices>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM default_prices");

                        ResultSet rs = p.executeQuery();

                        while (rs.next()) {
                                Slots slot = Slots.valueOf(rs.getString("slot"));
                                prices.add(new ItemPrices(slot, rs.getDouble("price_normal"), rs.getDouble("price_heroic")));
                        }

                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return prices;
        }

        @Override
        public int updateItem(Items item, String newname, Slots newslot, Type newtype, int newwowid, int newwowidhc, double newprice, double newpricehc) {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("UPDATE items SET name=? , wowid_normal=? , wowid_heroic=? , price_normal=? , price_heroic=? , slot=? , type=? WHERE id=?");
                        p.setString(1, newname);
                        p.setInt(2, newwowid);
                        p.setInt(3, newwowidhc);
                        p.setDouble(4, newprice);
                        p.setDouble(5, newpricehc);
                        p.setString(6, newslot.toString());
                        p.setString(7, newtype.toString());
                        p.setInt(8, item.getId());

                        success = p.executeUpdate();

                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return success;
        }

        @Override
        public int addItem(String name, int wowid, int wowid_hc, double price, double price_hc, String slot, String type) throws SQLException {
                Connection c = null;
                int result = 0;

                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement ps = c.prepareStatement("INSERT INTO items (name, wowid_normal, wowid_heroic, price_normal, price_heroic, slot, type) VALUES(?,?,?,?,?,?,?)");
                        ps.setString(1, name);
                        ps.setInt(2, wowid);
                        ps.setInt(3, wowid_hc);
                        ps.setDouble(4, price);
                        ps.setDouble(5, price_hc);
                        ps.setString(6, slot);
                        ps.setString(7, type);

                        result = ps.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return result;
        }

        @Override
        public Object getItemPrice(String itemname, boolean heroic) throws SQLException {
                Double price = 0.0;
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM items WHERE name=?");
                        p.setString(1, itemname);
                        ResultSet rs = p.executeQuery();
                        if (rs.next()) {
                                if (heroic) {
                                        price = rs.getDouble("price_heroic");
                                } else {
                                        price = rs.getDouble("price_normal");
                                }
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return price;
        }

        @Override
        public int getItemId(String loot) {
                DBConnection c = new DBConnection();
                int itemid = 0;
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM items WHERE name=?");
                        p.setString(1, loot);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                itemid = rs.getInt("id");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return itemid;
        }

        @Override
        public Items getSingleItem(String name) {
                Connection c = null;
                Items item = null;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM items WHERE name=?");
                        p.setString(1, name);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                String temp = rs.getString("type");
                                Type tempType = typeFromString(temp);
                                item = new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), tempType);
                                item.addLooterList(getLootersFormItems(rs.getInt("id")));
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return item;
        }

        @Override
        public void updateDefaultPrice(String slot, double normalprice, double heroicprice) {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("UPDATE default_prices SET price_normal=? , price_heroic=? WHERE slot=? ");
                        p.setDouble(1, normalprice);
                        p.setDouble(2, heroicprice);
                        p.setString(3, slot);
                        success = p.executeUpdate();
                        // System.out.println("Default prices changed for " + success + " slots");
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
        }

        @Override
        public int deleteItem(int id) throws SQLException {
                DBConnection c = new DBConnection();
                int success = 0;
                PreparedStatement p = c.prepareStatement("DELETE FROM items WHERE id=?");
                p.setInt(1, id);
                success += p.executeUpdate();
                c.closeStatement();
                p = c.prepareStatement("DELETE FROM loots WHERE item_id=?");
                p.setInt(1, id);
                success += p.executeUpdate();
                c.closeStatement();
                c.close();
                return success;
        }

        @Override
        public ArrayList<RaidItem> getItemsForRaid(int id) throws SQLException {
                DBConnection c = new DBConnection();
                ArrayList<RaidItem> items = new ArrayList<RaidItem>();
                PreparedStatement p = c.prepareStatement("SELECT * FROM loots JOIN items ON loots.item_id = items.id JOIN characters ON loots.character_id = characters.id WHERE loots.raid_id = ?");
                p.setInt(1, id);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                        RaidItem tmp = new RaidItem(rs.getString("items.name"), rs.getString("characters.name"), rs.getInt("loots.item_id"), rs.getDouble("loots.price"), rs.getBoolean("loots.heroic"));
                        items.add(tmp);
                        // System.out.println(rs.toString());
                }
                c.close();
                return items;
        }

        private void closeConnection(Connection c) {
                try {
                        c.close();
                } catch (SQLException ex) {
                        Logger.getLogger(ItemDB.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public Items getItemById(int id) {
                DBConnection c = new DBConnection();
                Items tmp = null;
                try {
                        PreparedStatement ps = c.prepareStatement("SELECT DISTINCT * FROM items WHERE id=?");
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                String temp = rs.getString("type");
                                Type tempType = typeFromString(temp);
                                tmp = new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), tempType);
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return tmp;
        }

        private Type typeFromString(String temp) {
                Type tempType;
                if (temp.toString().equals("Hunter, Shaman, Warrior")) {
                        tempType = Type.protector;
                } else if (temp.toString().equals("Death Knight, Druid, Mage, Rogue")) {
                        tempType = Type.vanquisher;
                } else if (temp.toString().equals("Paladin, Priest, Warlock")) {
                        tempType = Type.conqueror;
                } else {
                        tempType = Type.valueOf(temp);
                }
                return tempType;
        }
}
