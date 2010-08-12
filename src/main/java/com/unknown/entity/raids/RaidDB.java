/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.raids;

import com.unknown.entity.DBConnection;
import com.unknown.entity.character.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alde
 */
public class RaidDB implements RaidDAO {

        @Override
        public List<Raid> getRaids() {
                try {
                        Class.forName("com.mysql.jdbc.Driver");
                } catch (ClassNotFoundException ex) {
                        Logger.getLogger(CharacterDB.class.getName()).log(Level.SEVERE, null, ex);
                }
                Connection c = null;
                List<Raid> raids = new ArrayList<Raid>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM raids JOIN zones ON raids.zone_id=zones.id");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                final Raid raid = new Raid(rs.getString("zones.name"), rs.getString("raids.comment"), rs.getString("raids.date"), rs.getInt("raids.id"));
                                System.out.println("getting stuff for raid: " + raid.getID());
                                raid.addRaidItems(getItemsForRaid(raid.getID()));
                                raid.addRaidChars(getCharsForRaid(raid.getID()));
                                raid.addRaidRewards(getRewardsForRaid(raid.getID()));
                                raids.add(raid);
                        }
                } catch (SQLException e) {
                }
                return raids;
        }

        public List<RaidItem> getItemsForRaid(int raidId) throws SQLException {

                Connection c = null;
                List<RaidItem> raidItems = new ArrayList<RaidItem>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM loots JOIN raids JOIN characters JOIN items WHERE loots.raid_id=raids.id AND character_id=characters.id AND loots.item_id=items.id AND raids.id=?");
                        p.setInt(1, raidId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                RaidItem item = new RaidItem();
                                item.setId(new Integer(rs.getInt("raids.id")));
                                item.setLooter(rs.getString("characters.name"));
                                item.setName(rs.getString("items.name"));
                                item.setPrice(rs.getDouble("loots.price"));
                                item.setHeroic(rs.getBoolean("loots.heroic"));
                                raidItems.add(item);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                c.close();
                        }
                }

                return raidItems;
        }

        public List<RaidChar> getCharsForRaid(int raidId) {
                Connection c = null;
                List<RaidChar> raidChars = new ArrayList<RaidChar>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM rewards JOIN character_rewards JOIN characters ON rewards.raid_id=? AND rewards.id=character_rewards.reward_id AND character_rewards.character_id=characters.id");
                        p.setInt(1, raidId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                RaidChar rchar = new RaidChar();
                                rchar.setId(rs.getInt("rewards.id"));
                                rchar.setName(rs.getString("characters.name"));
                                rchar.setShares(rs.getInt("rewards.number_of_shares"));
                                rchar.setComment(rs.getString("rewards.comment"));
                                rchar.setRaidId(rs.getInt("rewards.raid_id"));
                                raidChars.add(rchar);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                }

                return raidChars;
        }

        @Override
        public List<String> getRaidZoneList() {
                List<String> zones = new ArrayList<String>();
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement ps = c.prepareStatement("INSERT INTO raids (zone_id, date, comment) VALUES(?,?,?)");
                        PreparedStatement pzone = c.prepareStatement("SELECT * FROM zones");
                        ResultSet rzone = pzone.executeQuery();
                        while (rzone.next()) {
                                zones.add(rzone.getString("name"));
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                try {
                                        c.close();
                                } catch (SQLException ex) {
                                        Logger.getLogger(CharacterDB.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                }
                return zones;
        }

        @Override
        public int addNewRaid(String zone, String comment, String date) {

                Connection c = null;
                int result = 0;
                int zoneId = 0;

                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement ps = c.prepareStatement("INSERT INTO raids (zone_id, date, comment) VALUES(?,?,?)");
                        PreparedStatement pzone = c.prepareStatement("SELECT * FROM zones WHERE name=?");
                        pzone.setString(1, zone);
                        ResultSet rzone = pzone.executeQuery();
                        while (rzone.next()) {
                                zoneId = rzone.getInt("id");
                        }
                        ps.setInt(1, zoneId);
                        ps.setString(2, date);
                        ps.setString(3, comment);
                        result = ps.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                try {
                                        c.close();
                                } catch (SQLException ex) {
                                        Logger.getLogger(CharacterDB.class.getName()).log(Level.SEVERE, null, ex);
                                }
                        }
                }
                return result;
        }

        private int addCharsToReward(Connection c, List<Integer> newcharid, RaidReward reward) throws SQLException {
                PreparedStatement p = c.prepareStatement("INSERT INTO character_rewards (reward_id, character_id) VALUES (?,?)");
                int success = 0;
                for (Integer i : newcharid) {
                        p.setInt(1, reward.getId());
                        p.setInt(2, i);
                        success += p.executeUpdate();
                }
                return success;
        }

        private Collection<RaidReward> getRewardsForRaid(int raidId) throws SQLException {
                System.out.println("getting rewards for raid: " + raidId);
                Connection c = null;
                List<RaidReward> raidRewards = new ArrayList<RaidReward>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM rewards WHERE rewards.raid_id=?");
                        p.setInt(1, raidId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                RaidReward rrewards = new RaidReward();
                                rrewards.setId(rs.getInt("rewards.id"));
                                rrewards.setComment(rs.getString("rewards.comment"));
                                rrewards.setShares(rs.getInt("rewards.number_of_shares"));
                                rrewards.addRewardChars(getCharsForReward(rrewards.getId()));
                                raidRewards.add(rrewards);
                                System.out.println("rreward" + rrewards.toString());
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                c.close();
                        }
                }
                return raidRewards;

        }

        private Collection<RaidChar> getCharsForReward(int id) throws SQLException {
                Connection c = null;
                List<RaidChar> raidChars = new ArrayList<RaidChar>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement(" SELECT * FROM character_rewards JOIN rewards JOIN characters WHERE character_rewards.reward_id=? AND rewards.id=? AND characters.id=character_rewards.character_id");
                        p.setInt(1, id);
                        p.setInt(2, id);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                RaidChar rchar = new RaidChar();
                                rchar.setId(rs.getInt("character_rewards.id"));
                                rchar.setComment(rs.getString("rewards.comment"));
                                rchar.setName(rs.getString("characters.name"));
                                rchar.setShares(rs.getInt("rewards.number_of_shares"));
                                rchar.setRaidId(rs.getInt("rewards.raid_id"));
                                raidChars.add(rchar);
                                System.out.println("rchar" + rchar.toString());
                        }
                } catch (SQLException e) {
                        e.printStackTrace();

                } finally {
                        if (c != null) {
                                c.close();
                        }
                }
                return raidChars;
        }

        @Override
        public int doRaidUpdate(Raid raid, String raidzoneName, String raidcomment, String raiddate) throws SQLException {
                int success = 0;
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("UPDATE raids SET zone_id=? , date=? , comment=? WHERE id=?");
                        int zoneid = getZoneIdByName(c, raidzoneName);
                        p.setInt(1, zoneid);
                        p.setString(2, raiddate);
                        p.setString(3, raidcomment);
                        p.setInt(4, raid.getID());
                        success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                c.close();
                        }
                }
                return success;
        }

        private int getZoneIdByName(Connection c, String raidzoneName) throws SQLException {
                PreparedStatement pzone = c.prepareStatement("SELECT * FROM zones WHERE name=?");
                pzone.setString(1, raidzoneName);
                ResultSet rs = pzone.executeQuery();
                int zoneid = 0;
                while (rs.next()) {
                        zoneid = rs.getInt("id");
                }
                return zoneid;
        }

        @Override
        public int doUpdateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment) throws SQLException {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        success += doUpdateCharacters(c, reward, newAttendants);
                        success += doUpdateSharesAndComment(c, reward, newShares, newComment);
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                c.close();
                        }
                }
                return success;
        }

        private int doUpdateCharacters(Connection c, RaidReward reward, List<String> newAttendants) throws SQLException {
                List<Integer> newcharid = new ArrayList<Integer>();
                CharacterDAO characterDao = new CharacterDB();

                newAttendants = removeDuplicates(newAttendants);

                for (String s : newAttendants) {
                        newcharid.add(characterDao.GetCharacterId(c, s));
                }
                int removed = removeAllExistingCharactersFromReward(c, reward, newAttendants, newcharid);
                System.out.println("Removed: " + removed);
                int success = addCharsToReward(c, newcharid, reward);
                System.out.println("Sucess: " + success);
                return success;
        }

        private int removeAllExistingCharactersFromReward(Connection c, RaidReward reward, List<String> newAttendants, List<Integer> newcharclassid) throws SQLException {
                PreparedStatement p = c.prepareStatement("DELETE FROM character_rewards WHERE reward_id=?");
                p.setInt(1, reward.getId());
                return p.executeUpdate();
        }

        private int doUpdateSharesAndComment(Connection c, RaidReward reward, int newShares, String newComment) throws SQLException {
                PreparedStatement p = c.prepareStatement("UPDATE rewards SET number_of_shares=? , comment=? WHERE id=?");
                p.setInt(1, newShares);
                p.setString(2, newComment);
                p.setInt(3, reward.getId());
                return p.executeUpdate();
        }

        private List<String> removeDuplicates(List<String> attendants) {
                HashSet hs = new HashSet(attendants);
                attendants.clear();
                attendants.addAll(hs);
                return attendants;
        }

        @Override
        public List<String> getBossesForRaid(Raid raid) throws SQLException {
                Connection c = null;
                List<String> bosses = new ArrayList<String>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM mobs JOIN zones WHERE mobs.zone_id=zones.id AND zones.name=?");
                        p.setString(1, raid.getName());
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                String foo = rs.getString("mobs.name");
                                bosses.add(foo);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        if (c != null) {
                                c.close();
                        }
                }
                return bosses;
        }
}
