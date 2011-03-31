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
import com.unknown.entity.Logg;
import com.unknown.entity.Role;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.character.SiteUser;
import com.unknown.entity.character.User;
import com.vaadin.Application;
import java.math.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde 
 */
public class CharDB {
        private static List<User> cachedUsers = new ArrayList<User>();
        private static Application app;

        
        public static List<User> getUsers() {
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

        
        public static void clearCache() {
                cachedUsers.clear();
        }

        private static User calculateDKP(Multimap<Integer, Double> prices, Multimap<Integer, Integer> shareslist, Map<Integer, String> charnames, Map<Integer, String> charroles, int userid, Boolean active, int totalshares, double loot_value) {
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
                user.setAttendance(getAttendanceRaids(user));
                return user;
        }

        
        public static int getCharacterClassId(String charclass) {
                DBConnection c = new DBConnection();
                int classid = 0;
                try {
                        PreparedStatement pclass = c.prepareStatement("SELECT id FROM character_classes WHERE name=?");
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

        
        public static int getCharacterId(String charname) {
                DBConnection c = new DBConnection();
                int charid = 0;
                try {
                        PreparedStatement pclass = c.prepareStatement("SELECT id FROM characters WHERE name=?");
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

        private static List<CharacterItem> getItemsForCharacter(int charId) {
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

        
        public static String getRoleForCharacter(String name) {
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

        
        public static Collection<User> getUsersWithRole(final Role role) {
                return Collections2.filter(getUsers(), new HasRolePredicate(role));
        }

        
        public static int addNewSiteUser(String username, String password, int rank) {
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
                addLog("Added Site User [" + username + "] with Rank: " + rank);
                return success;
        }

        
        public static ImmutableList<String> getUserNames() {
                Builder<String> userNameBuilder = ImmutableList.builder();
                for (User user : getUsers()) {
                        userNameBuilder.add(user.getUsername());
                }
                return userNameBuilder.build();

        }

        
        public static Double getAttendanceRaids(User user) {
                Double foo = 0.0;
                int amountofRaids = RaidDB.getTotalRaidsLastThirtyDays();
                int attendedRaids = RaidDB.getAttendedRaidsLastThirtyDays(user);
                double temp = 0;
                if (amountofRaids == 0) {
                        temp = 0;
                } else {
                        temp = attendedRaids * 100 / amountofRaids;
                }
                foo = temp;
                return foo;
        }

        
        public static void removeLootFromCharacter(String itemname, User user) {
                Connection c = null;

                try {
                        c = new DBConnection().getConnection();
                        int charid = getCharacterId(user.getUsername());
                        int itemid = ItemDB.getItemId(itemname);
                        PreparedStatement p = c.prepareStatement("DELETE FROM loots WHERE item_id=? AND character_id=?");
                        p.setInt(1, itemid);
                        p.setInt(2, charid);
                        p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                addLog("Removed Loot [" + itemname + " from " + user.getUsername() + "]");
        }

        
        public static void updateLootForCharacter(String itemname, double price, boolean heroic, User user,
                int lootid) {
                Connection c = null;

                try {
                        c = new DBConnection().getConnection();
                        int charid = getCharacterId(user.getUsername());
                        int itemid = ItemDB.getItemId(itemname);

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
                addLog("Updated Loot for " + user.getUsername() + " [" + itemname + " | " + price + "dkp]");
        }

        private static void closeConnection(Connection c) {
                try {
                        c.close();
                } catch (SQLException ex) {
                        Logger.getLogger(CharDB.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

        
        public static void deleteCharacter(User user) {
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("DELETE FROM characters WHERE id=?");
                        p.setInt(1, user.getId());
                        p.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                addLog("Deleted Character [" + user.getUsername() + " | " + user.getRole().toString() + "]");
        }

        
        public static List<String> getSiteUsers() {
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

        public static int getSiteUserId(String username) {
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

        
        public static void updateSiteUser(String username, String password, int level) {
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
                addLog("Updated Site User [" + username + " | Rank: " + level + "]");
        }

        
        public static int getSiteUserLevel(String name) {
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

        
        public static int countActiveUsers() {
                int i = 0;
                for (User u : getUsers()) {
                        if (u.isActive()) {
                                i++;
                        }
                }
                return i;
        }

        
        public static User getUser(String username) {
                for (User u : getUsers()) {
                        if (u.getUsername().equalsIgnoreCase(username)) {
                                return u;
                        }
                }
                return null;
        }

        private static int getTotalAdjustmentsForCharacter(int userid) {
                DBConnection c = new DBConnection();
                int total = 0;
                try {
                        PreparedStatement p = c.prepareStatement("SELECT SUM(shares) AS pun FROM adjustments WHERE character_id=?");
                        p.setInt(1, userid);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                total = rs.getInt("pun");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return total;
        }

        
        public static Integer getShares(int id) {
                int adjustments = getTotalAdjustmentsForCharacter(id);
                int shares = 0;
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT SUM(number_of_shares) AS shares FROM rewards JOIN character_rewards WHERE rewards.id=character_rewards.reward_id AND character_rewards.character_id=?");
                        p.setInt(1, id);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                shares = rs.getInt("shares");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return shares - adjustments;
        }

        
        public static void setApplication(Application app) {
                CharDB.app = app;
        }

        public static List<User> getUsersSortedByDKP() {
                List<User> users = getUsers();
                Collections.sort(users, new Comparator<User>() {

                        @Override
                        public int compare(User t, User t1) {
                                return t.getDKP() < t1.getDKP() ? 1 : 0;
                        }
                });
                return users;
        }

        private static class HasRolePredicate implements Predicate<User> {

                private final Role role;

                public HasRolePredicate(Role role) {
                        this.role = role;
                }

                
                public boolean apply(User user) {
                        return user.getRole().equals(role);
                }
        }

        
        public static int addNewCharacter(String name, String role, Boolean isActive) {
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
                addLog("Added character [" + name + " | " + role.toString() + "]");
                return update;
        }

        private static String fixRole(String role) {
                if (role.equals("DeathKnight")) {
                        return "Death Knight";
                } else {
                        return role;
                }
        }

        
        public static int updateCharacter(User user, String name, String charclass, boolean active) {
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
                String foo = "";
                if (user.getUsername().equalsIgnoreCase(name)) {
                        foo = name + " | ";
                } else {
                        foo = user.getUsername() + " -> " + name + " | ";
                }
                if (!user.getRole().toString().equalsIgnoreCase(charclass)) {
                        foo = foo + user.getRole().toString() + " -> " + charclass + " | ";
                }

                addLog("Updated Character [" + foo + "Active: " + active + "]");
                return success;
        }

        private static void addLog(String message) {
                String name = "";
                if (app == null || (SiteUser) app.getUser() == null) {
                        name = "<unknown>";
                } else {
                        name = ((SiteUser) app.getUser()).getName();
                }
                Logg.addLog(message, name, "char");
        }

        public static List<User> getUsersSortedByAttendance() {
                List<User> users = getUsers();
                Collections.sort(users, new Comparator<User>() {

                        @Override
                        public int compare(User o1, User o2) {
                                 return o1.getAttendance() < o2.getAttendance() ? 1 : 0;
                        }

                });
                return users;
        }
}
