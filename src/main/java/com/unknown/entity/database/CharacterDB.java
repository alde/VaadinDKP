/*
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.database;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.unknown.entity.DBConnection;
import com.unknown.entity.Role;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.dao.CharacterDAO;
import com.unknown.entity.character.User;
import com.unknown.entity.dao.ItemDAO;
import com.unknown.entity.dao.RaidDAO;
import java.math.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde 
 */
public class CharacterDB implements CharacterDAO {

        RaidDAO raidDao = new RaidDB();
        ItemDAO itemDao = new ItemDB();
        private static List<User> cachedUsers = new ArrayList<User>();

        @Override
        public List<User> getUsers() {
                if (cachedUsers != null) {
                        if (!cachedUsers.isEmpty()) {
                                return new ArrayList(cachedUsers);
                        }
                }
                Connection c = null;
                List<User> users = new ArrayList<User>();
                try {
                        c = new DBConnection().getConnection();
                        int totalshares = 0;
                        int totaladjustments = 0;
                        double loot_value = 0.0;
                        PreparedStatement p = c.prepareStatement("SELECT * FROM characters JOIN character_classes ON characters.character_class_id=character_classes.id");
                        ResultSet rs = p.executeQuery();
                        PreparedStatement ploot = c.prepareStatement("SELECT * FROM loots JOIN characters where loots.character_id=characters.id");
                        ResultSet rsloot = ploot.executeQuery();
                        PreparedStatement ps = c.prepareStatement("SELECT * FROM rewards JOIN character_rewards JOIN characters ON character_rewards.reward_id=rewards.id AND characters.id=character_rewards.character_id");
                        ResultSet rss = ps.executeQuery();
                        PreparedStatement totalShares = c.prepareStatement("SELECT * FROM rewards JOIN character_rewards ON character_rewards.reward_id=rewards.id JOIN characters ON character_rewards.character_id=characters.id");
                        ResultSet rsTotalShares = totalShares.executeQuery();
                        PreparedStatement pAdjust = c.prepareStatement("SELECT SUM(shares) AS pun FROM adjustments");
                        ResultSet rsAdjust = pAdjust.executeQuery();
                        while (rsAdjust.next()) {
                                totaladjustments = rsAdjust.getInt("pun");
                        }
                        while (rsTotalShares.next()) {
                                if (rsTotalShares.getBoolean("characters.active")) {
                                        totalshares += rsTotalShares.getInt("rewards.number_of_shares");
                                }
                        }
                        totalshares += totaladjustments;
                        
                        Multimap<Integer, Double> prices = LinkedListMultimap.create();
                        Map<Integer, String> charnames = new HashMap<Integer, String>();
                        Map<Integer, String> charroles = new HashMap<Integer, String>();
                        Multimap<Integer, Integer> shareslist = LinkedListMultimap.create();
                        Map<Integer, Boolean> charactive = new HashMap<Integer, Boolean>();
                        List<Integer> charids = new ArrayList<Integer>();
                        while (rsloot.next()) {
                                double pricetemp = rsloot.getDouble("loots.price");
                                prices.put(rsloot.getInt("loots.character_id"), pricetemp);
                                if (rsloot.getBoolean("characters.active")) {
                                        loot_value += pricetemp;
                                }
                        }
                        while (rss.next()) {
                                shareslist.put(rss.getInt("characters.id"), rss.getInt("rewards.number_of_shares"));
                        }
                        while (rs.next()) {
                                charnames.put(rs.getInt("characters.id"), rs.getString("characters.name"));
                                charroles.put(rs.getInt("characters.id"), rs.getString("character_classes.name").replace(" ", ""));
                                charactive.put(rs.getInt("characters.id"), rs.getBoolean("characters.active"));
                                charids.add(rs.getInt("characters.id"));

                        }
                        for (int userid : charids) {
                                Boolean active = charactive.get(userid);
                                users.add(calculateDKP(prices, shareslist, charnames, charroles, userid, active, totalshares, loot_value));
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                cachedUsers = users;
                return users;
        }

        @Override
        public void clearCache() {
                cachedUsers.clear();
        }

        private User calculateDKP(Multimap<Integer, Double> prices, Multimap<Integer, Integer> shareslist, Map<Integer, String> charnames, Map<Integer, String> charroles, int userid, Boolean active, int totalshares, double loot_value) {
                int shares = 0;
                int adjustments = getTotalAdjustmentsForCharacter(userid);
                double dkp_earned = 0.0, dkp_spent = 0.0, dkp = 0.0, share_value = 0.0;
                Collection<Double> priceCollection = prices.get(userid);
                for (Double dkpvalue : priceCollection) {
                        dkp_spent = dkp_spent + dkpvalue;
                }

                Collection<Integer> shareCollection = shareslist.get(userid);
                for (Integer sharetemp : shareCollection) {
                        shares = shares + sharetemp;
                }
                shares += adjustments;

                if (totalshares > 0) {
                        share_value = loot_value / totalshares;
                } else {
                        share_value = 0;
                }
                dkp_earned = shares * share_value;
                dkp = dkp_earned - dkp_spent;
                BigDecimal formatted_dkp_spent = new BigDecimal(dkp_spent).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                BigDecimal formatted_dkp_earned = new BigDecimal(dkp_earned).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                BigDecimal formatted_dkp = new BigDecimal(dkp).setScale(2, BigDecimal.ROUND_HALF_DOWN);

                String username = charnames.get(userid);
                Role userrole = Role.valueOf(charroles.get(userid));
                User user = new User(userid, username, userrole, active, shares, formatted_dkp_earned.doubleValue(), formatted_dkp_spent.doubleValue(), formatted_dkp.doubleValue());
                user.addCharItems(getItemsForCharacter(userid));
                return user;
        }

        @Override
        public int getCharacterClassId(String charclass) {
                DBConnection c = new DBConnection();
                int classid = 0;
                try {
                        PreparedStatement pclass = c.prepareStatement("SELECT * FROM character_classes WHERE name=?");
                        pclass.setString(1, fixRole(charclass));
                        ResultSet rclass = pclass.executeQuery();
                        while (rclass.next()) {
                                classid = rclass.getInt("id");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return classid;
        }

        @Override
        public int getCharacterId(String charname) {
                DBConnection c = new DBConnection();
                int charid = 0;
                try {
                        PreparedStatement pclass = c.prepareStatement("SELECT * FROM characters WHERE name=?");
                        pclass.setString(1, charname);
                        ResultSet rclass = pclass.executeQuery();
                        while (rclass.next()) {
                                charid = rclass.getInt("id");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return charid;
        }

        private List<CharacterItem> getItemsForCharacter(int charId) {
                Connection c = null;
                List<CharacterItem> itemlist = new ArrayList<CharacterItem>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM loots JOIN items WHERE loots.character_id=? AND loots.item_id=items.id");
                        p.setInt(1, charId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                CharacterItem charitem = new CharacterItem();
                                charitem.setId(rs.getInt("loots.id"));
                                charitem.setName(rs.getString("items.name"));
                                charitem.setPrice(rs.getDouble("loots.price"));
                                charitem.setHeroic(rs.getBoolean("loots.heroic"));
                                charitem.setQuality(rs.getString("items.quality"));
                                itemlist.add(charitem);
                        }
                } catch (SQLException e) {
                } finally {
                        closeConnection(c);
                }
                return itemlist;
        }

        @Override
        public String getRoleForCharacter(String name) {
                DBConnection c = new DBConnection();
                String foo = "";
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM characters JOIN character_classes ON characters.character_class_id=character_classes.id WHERE characters.name=?");
                        p.setString(1, name);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                foo = rs.getString("character_classes.name");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return foo;
        }

        @Override
        public Collection<User> getUsersWithRole(
                final Role role) {
                return Collections2.filter(getUsers(), new HasRolePredicate(role));
        }

        @Override
        public int addNewSiteUser(String username, String password, int rank) {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("INSERT INTO users (name, password, rank) VALUES(?,?,?)");
                        p.setString(1, username);
                        p.setString(2, password);
                        p.setInt(3, rank);
                        success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return success;
        }

        @Override
        public ImmutableList<String> getUserNames() {
                Builder<String> userNameBuilder = ImmutableList.builder();
                for (User user : getUsers()) {
                        userNameBuilder.add(user.getUsername());
                }
                return userNameBuilder.build();

        }

        @Override
        public String getAttendanceRaids(User user) {
                String foo = "";
                int amountofRaids = raidDao.getTotalRaidsLastThirtyDays();
                int attendedRaids = raidDao.getAttendedRaidsLastThirtyDays(user);
                double temp = 0;
                if (amountofRaids == 0) {
                        temp = 0;
                } else {
                        temp = attendedRaids * 100 / amountofRaids;
                }
                foo = "" + temp;
                return foo;
        }

        @Override
        public void removeLootFromCharacter(String itemname, User user) {
                Connection c = null;

                try {
                        c = new DBConnection().getConnection();
                        int charid = getCharacterId(user.getUsername());
                        int itemid = itemDao.getItemId(itemname);
                        PreparedStatement p = c.prepareStatement("DELETE FROM loots WHERE item_id=? AND character_id=?");
                        p.setInt(1, itemid);
                        p.setInt(2, charid);
                        int success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
        }

        @Override
        public void updateLootForCharacter(String itemname, double price, boolean heroic, User user,
                int lootid) {
                Connection c = null;

                try {
                        c = new DBConnection().getConnection();
                        int charid = getCharacterId(user.getUsername());
                        int itemid = itemDao.getItemId(itemname);

                        PreparedStatement p = c.prepareStatement("UPDATE loots SET item_id=? , character_id=?, price=?, heroic=? WHERE id=?");
                        p.setInt(1, itemid);
                        p.setInt(2, charid);
                        p.setDouble(3, price);
                        p.setBoolean(4, heroic);
                        p.setInt(5, lootid);
                        int success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
        }

        private void closeConnection(Connection c) {
                try {
                        c.close();
                } catch (SQLException ex) {
                        Logger.getLogger(CharacterDB.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        @Override
        public void deleteCharacter(User user) {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("DELETE FROM characters WHERE id=?");
                        p.setInt(1, user.getId());
                        success = p.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        closeConnection(c);
                }

        }

        @Override
        public List<String> getSiteUsers() {
                DBConnection c = new DBConnection();
                List<String> users = new ArrayList<String>();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM users");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                users.add(rs.getString("name"));
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return users;
        }

        public int getSiteUserId(String username) {
                DBConnection c = new DBConnection();
                int id = 0;
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM users WHERE name=?");
                        p.setString(1, username);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                id = rs.getInt("id");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return id;
        }

        @Override
        public void updateSiteUser(String username, String password, int level) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("UPDATE users SET name=? , password = ? , rank = ? WHERE id = ?");
                        p.setString(1, username);
                        p.setString(2, password);
                        p.setInt(3, level);
                        p.setInt(4, getSiteUserId(username));
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        @Override
        public int getSiteUserLevel(String name) {
                DBConnection c = new DBConnection();
                int level = 1;
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM users WHERE name=?");
                        p.setString(1, name);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                level = rs.getInt("rank");
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return level;
        }

        @Override
        public int countActiveUsers() {
                int i = 0;
                for (User u : getUsers()) {
                        if (u.isActive()) {
                                i++;
                        }
                }
                return i;
        }

        @Override
        public User getUser(String username) {
                for (User u : getUsers()) {
                        if (u.getUsername().equalsIgnoreCase(username)) {
                                return u;
                        }
                }
                return null;
        }

        private int getTotalAdjustmentsForCharacter(int userid) {
                DBConnection c = new DBConnection();
                int total = 0;
                try {
                        PreparedStatement p = c.prepareStatement("SELECT SUM(shares) AS pun FROM adjustments WHERE character_id=?");
                        p.setInt(1, userid);
                        ResultSet rs = p.executeQuery();
                        while (rs.next())
                                total = rs.getInt("pun");
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return total;
        }

        @Override
        public Integer getShares(int id) {
                int adjustments = getTotalAdjustmentsForCharacter(id);
                int shares = 0;
                DBConnection c = new DBConnection();
                try {
                         PreparedStatement p = c.prepareStatement("SELECT SUM(number_of_shares) AS shares FROM rewards JOIN character_rewards WHERE rewards.id=character_rewards.reward_id AND character_rewards.character_id=?");
                         p.setInt(1, id);
                         ResultSet rs = p.executeQuery();
                         while (rs.next())
                                 shares = rs.getInt("shares");
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return shares - adjustments;
        }

        private class HasRolePredicate implements Predicate<User> {

                private final Role role;

                public HasRolePredicate(Role role) {
                        this.role = role;
                }

                @Override
                public boolean apply(User user) {
                        return user.getRole().equals(role);
                }
        }

        @Override
        public int addNewCharacter(String name, String role, Boolean isActive) {
                Connection c = null;
                int class_id = 0, update = 0;

                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement ps = c.prepareStatement("INSERT INTO characters (name, character_class_id, active, user_id) VALUES(?,?,?,NULL)");
                        PreparedStatement pclass = c.prepareStatement("SELECT * FROM character_classes WHERE name=?");
                        pclass.setString(1, fixRole(role));
                        ResultSet rsclass = pclass.executeQuery();

                        while (rsclass.next()) {
                                class_id = rsclass.getInt("id");
                        }
                        ps.setString(1, name);
                        ps.setInt(2, class_id);
                        ps.setBoolean(3, isActive);
                        update = ps.executeUpdate();

                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return update;
        }

        private String fixRole(String role) {
                if (role.equals("DeathKnight")) {
                        return "Death Knight";
                } else {
                        return role;
                }
        }

        @Override
        public int updateCharacter(User user, String name, String charclass, boolean active) {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        int classid = getCharacterClassId(charclass);
                        PreparedStatement p = c.prepareStatement("UPDATE characters SET name=? , character_class_id=? , active=? , user_id=NULL WHERE id=?");
                        p.setString(1, name);
                        p.setInt(2, classid);
                        p.setBoolean(3, active);
                        p.setInt(4, user.getId());
                        success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return success;
        }
}
