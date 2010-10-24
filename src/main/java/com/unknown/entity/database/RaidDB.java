/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.database;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.dao.*;
import com.unknown.entity.DBConnection;
import com.unknown.entity.SQLRuntimeException;
import com.unknown.entity.character.User;
import com.unknown.entity.raids.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author alde
 */
public class RaidDB implements RaidDAO {

        @Override
        public List<Raid> getRaids() {
                Connection c = null;
                List<Raid> raids = new ArrayList<Raid>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM raids JOIN zones ON raids.zone_id=zones.id");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                final Raid raid = new Raid(rs.getString("zones.name"), rs.getString("raids.comment"), rs.getString("raids.date"), rs.getInt("raids.id"));
                                // System.out.println("getting stuff for raid: " + raid.getId());
                                raid.addRaidItems(getItemsForRaid(raid.getId()));
                                raid.addRaidChars(getCharsForRaid(raid.getId()));
                                raid.addRaidRewards(getRewardsForRaid(raid.getId()));
                                raids.add(raid);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
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
                                RaidItem item = new RaidItem(rs.getString("items.name"), rs.getString("characters.name"), rs.getInt("loots.id"), rs.getDouble("loots.price"), rs.getBoolean("loots.heroic"));
                                raidItems.add(item);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
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
                                rchar.setRaidId(rs.getInt("rewards.raid_id"));
                                raidChars.add(rchar);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
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
                        closeConnection(c);
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
                        closeConnection(c);
                }
                return result;
        }

        private void addCharsToReward(List<Integer> newcharid, RaidReward reward) throws SQLException {
                DBConnection connection = new DBConnection();
                try {
                        PreparedStatement p = connection.prepareStatement("INSERT INTO character_rewards (reward_id, character_id) VALUES (?,?)");
                        for (Integer i : newcharid) {
                                p.setInt(1, reward.getId());
                                p.setInt(2, i);
                                p.addBatch();
                        }
                        p.executeBatch();
                } finally {
                        connection.close();
                }
        }

        @Override
        public Collection<RaidReward> getRewardsForRaid(int raidId) throws SQLException {
                // System.out.println("getting rewards for raid: " + raidId);
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
                                // System.out.println("rreward" + rrewards.toString());
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return raidRewards;

        }

        @Override
        public Collection<RaidChar> getCharsForReward(int id) throws SQLException {
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
                                rchar.setName(rs.getString("characters.name"));
                                rchar.setShares(rs.getInt("rewards.number_of_shares"));
                                rchar.setRaidId(rs.getInt("rewards.raid_id"));
                                raidChars.add(rchar);
                                // System.out.println("rchar" + rchar.toString());
                        }
                } catch (SQLException e) {
                        e.printStackTrace();

                } finally {
                        closeConnection(c);
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
                        p.setInt(4, raid.getId());
                        success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
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
                        doUpdateCharacters(c, reward, newAttendants);
                        success += doUpdateSharesAndComment(c, reward, newShares, newComment);
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return success;
        }

        private void doUpdateCharacters(Connection c, RaidReward reward, List<String> newAttendants) throws SQLException {
                List<Integer> newcharid = new ArrayList<Integer>();
                CharacterDAO characterDao = new CharacterDB();

                newAttendants = removeDuplicates(newAttendants);

                for (String s : newAttendants) {
                        newcharid.add(characterDao.getCharacterId(s));
                }
                removeAllExistingCharactersFromReward(reward, newAttendants, newcharid);
                addCharsToReward(newcharid, reward);

        }

        private void removeAllExistingCharactersFromReward(RaidReward reward, List<String> newAttendants, List<Integer> newcharclassid) throws SQLException {
                DBConnection c = new DBConnection();
                try {

                        PreparedStatement p = c.prepareStatement("DELETE FROM character_rewards WHERE reward_id=?");
                        p.setInt(1, reward.getId());
                        p.executeUpdate();
                } finally {
                        c.close();
                }
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
                List<String> clean = new ArrayList<String>();
                clean.addAll(hs);
                return clean;
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
                        closeConnection(c);
                }
                return bosses;
        }

        @Override
        public void addLootToRaid(Raid raid, String boss, String name, String loot, boolean heroic, double price) throws SQLException {
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        CharacterDAO characterDao = new CharacterDB();
                        ItemDAO itemDao = new ItemDB();
                        int itemid = itemDao.getItemId(loot);
                        int charid = characterDao.getCharacterId(name);
                        int mobid = getMobId(c, boss);
                        PreparedStatement ps = c.prepareStatement("INSERT INTO loots (item_id, raid_id, mob_id, character_id, price, heroic) VALUES(?,?,?,?,?,?)");
                        ps.setInt(1, itemid);
                        ps.setInt(2, raid.getId());
                        ps.setInt(3, mobid);
                        ps.setInt(4, charid);
                        ps.setDouble(5, price);
                        ps.setBoolean(6, heroic);
                        ps.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
        }

        private int getMobId(Connection c, String boss) throws SQLException {
                PreparedStatement p = c.prepareStatement("SELECT * FROM mobs WHERE name=?");
                p.setString(1, boss);
                ResultSet rs = p.executeQuery();
                int bossid = 0;
                while (rs.next()) {
                        bossid = rs.getInt("id");
                }
                return bossid;
        }

        @Override
        public int removeReward(RaidReward reward) throws SQLException {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("DELETE FROM rewards WHERE id=?");
                        p.setInt(1, reward.getId());
                        success += p.executeUpdate();
                        p = c.prepareStatement("DELETE FROM character_rewards WHERE reward_id=?");
                        p.setInt(1, reward.getId());
                        success += p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return success;
        }

        @Override
        public int addReward(RaidReward reward) {
                DBConnection c = new DBConnection();
                int success = 0;
                try {
                        reward = doAddReward(reward);
                        doAddCharacterReward(reward);
                } catch (SQLException ex) {
                        throw new SQLRuntimeException(ex);
                } finally {
                        c.close();
                }

                return success;
        }

        private RaidReward doAddReward(RaidReward reward) throws SQLException {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("INSERT INTO rewards (number_of_shares, comment, raid_id) values(?,?,?)", Statement.RETURN_GENERATED_KEYS);
                        p.setInt(1, reward.getShares());
                        p.setString(2, reward.getComment());
                        p.setInt(3, reward.getRaidId());
                        p.executeUpdate();
                        ResultSet rs = p.getGeneratedKeys();
                        while (rs.next()) {
                                reward.setId(rs.getInt(1));
                        }
                } finally {
                        c.close();
                }
                return reward;
        }

        private void doAddCharacterReward(RaidReward reward) throws SQLException {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("INSERT INTO character_rewards (reward_id, character_id) values(?,?)");
                        for (RaidChar eachid : reward.getRewardChars()) {
                                p.setInt(1, reward.getId());
                                p.setInt(2, eachid.getId());
                                p.executeUpdate();
                        }
                } finally {
                        c.close();
                }
        }

        @Override
        public int removeLootFromRaid(RaidItem item) throws SQLException {
                Connection c = null;
                int success = 0;
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("DELETE FROM loots WHERE id=?");
                        p.setInt(1, item.getId());
                        success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return success;
        }

        @Override
        public List<String> findInvalidCharacters(List<String> attendantlist, CharacterDAO charDao) {
                List<String> invalid = new ArrayList<String>(attendantlist);
                invalid.removeAll(charDao.getUserNames());
                return ImmutableList.copyOf(invalid);
        }

        @Override
        public Collection<RaidChar> getRaidCharsForRaid(List<String> attendantlist, int raidId) {
                Set<RaidChar> chars = new HashSet<RaidChar>();
                final CharacterDB characterDB = new CharacterDB();
                for (String string : attendantlist) {

                        int characterId = characterDB.getCharacterId(string);
                        // System.out.println("Name: " + string + "ID: " + characterId);
                        chars.add(getRaidChar(characterId, raidId));

                }
                return chars;

        }

        public RaidChar getRaidChar(int charId, int raidId) {
                DBConnection c = new DBConnection();
                RaidChar rchar = new RaidChar();
                try {
                        PreparedStatement p = c.prepareStatement(" SELECT DISTINCT * FROM rewards JOIN character_rewards JOIN characters WHERE rewards.raid_id=? AND rewards.id=character_rewards.reward_id AND character_rewards.character_id=? AND character_rewards.character_id=characters.id");
                        p.setInt(1, raidId);
                        p.setInt(2, charId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                rchar.setId(rs.getInt("characters.id"));
                                rchar.setName(rs.getString("characters.name"));
                                rchar.setShares(rs.getInt("rewards.number_of_shares"));
                                rchar.setRaidId(rs.getInt("rewards.raid_id"));

                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        c.close();
                }

                return rchar;
        }

        @Override
        public Iterable<Raid> getRaidsForCharacter(int charid) throws SQLException {
                List<Raid> raids = new ArrayList<Raid>();
                DBConnection c = new DBConnection();
                PreparedStatement p = c.prepareStatement("SELECT DISTINCT raids.* FROM raids JOIN rewards JOIN characters JOIN character_rewards WHERE characters.id=? AND rewards.raid_id=raids.id AND character_rewards.character_id=characters.id AND character_rewards.reward_id=rewards.id");
                p.setInt(1, charid);
                ResultSet rs = p.executeQuery();
                while (rs.next()) {
                        Raid raidtemp = new Raid("", rs.getString("raids.comment"), rs.getString("raids.date"), rs.getInt("raids.id"));
                        raids.add(raidtemp);
                }

                c.close();
                return raids;
        }

        @Override
        public int getTotalRaidsLastThirtyDays() {
                int i = 0;
                DateTime dt = new DateTime();
                String startdate = dt.toYearMonthDay().minusDays(30).toString();
                String enddate = dt.toYearMonthDay().toString();
                // System.out.println("Today: " + enddate);
                // System.out.println("30 Days ago: " + startdate);
                DBConnection c = new DBConnection();
                try {

                        PreparedStatement p = c.prepareStatement("SELECT * FROM raids WHERE date BETWEEN ? AND ?");
                        p.setString(1, startdate);
                        p.setString(2, enddate);
                        ResultSet rs = p.executeQuery();
                        // System.out.println(p);
                        while (rs.next()) {
                                i++;
                        }

                } catch (SQLException e) {
                } finally {
                        c.close();
                }
                return i;
        }

        @Override
        public int getAttendedRaidsLastThirtyDays(User user) {
                int i = 0;
                DateTime dt = new DateTime();
                String startdate = dt.toYearMonthDay().minusDays(30).toString();
                String enddate = dt.toYearMonthDay().toString();
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM characters JOIN character_rewards JOIN rewards JOIN raids WHERE characters.id=character_rewards.character_id AND characters.id=? AND rewards.id=character_rewards.reward_id AND raids.id = rewards.raid_id AND raids.date BETWEEN ? AND ?");
                        p.setInt(1, user.getId());
                        p.setString(2, startdate);
                        p.setString(3, enddate);
                        ResultSet rs = p.executeQuery();
                        // System.out.println(p);
                        while (rs.next()) {
                                i++;
                        }
                        
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        c.close();
                }
                return i;
        }

        @Override
        public int doUpdateLoot(int id, String looter, String itemname, double price, boolean heroic) {
                ItemDAO itemDao = new ItemDB();
                CharacterDAO charDao = new CharacterDB();
                int itemid = itemDao.getItemId(itemname);
                int charid = charDao.getCharacterId(looter);
                // System.out.println("Itemid: " + itemid + " Charid: " + charid);
                int success = 0;
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("UPDATE loots SET item_id=? , character_id=?, price=?, heroic=? WHERE id=?");
                        p.setInt(1, itemid);
                        p.setInt(2, charid);
                        p.setDouble(3, price);
                        p.setBoolean(4, heroic);
                        p.setInt(5, id);
                        // System.out.println(p.toString());
                        success = p.executeUpdate();
                        // System.out.println(success + " items updated.");
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return success;
        }

        private void closeConnection(Connection c) {
                try {
                        c.close();
                } catch (SQLException ex) {
                        Logger.getLogger(RaidDB.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}
