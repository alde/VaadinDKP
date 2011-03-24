/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.database;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.character.Adjustment;
import com.unknown.entity.dao.*;
import com.unknown.entity.DBConnection;
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

        private static List<Raid> raidCache = new ArrayList<Raid>();

        @Override
        public List<Raid> getRaids() {
                if (raidCache != null) {
                        if (!raidCache.isEmpty()) {
                                return new ArrayList(raidCache);
                        }
                }
                Connection c = null;
                List<Raid> raids = new ArrayList<Raid>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT * FROM raids JOIN zones ON raids.zone_id=zones.id");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                final Raid raid = new Raid(rs.getString("zones.name"), rs.getString("raids.comment"), rs.getString("raids.date"), rs.getInt("raids.id"));
                                raids.add(raid);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                raidCache = raids;
                return raids;
        }

        @Override
        public void clearCache() {
                raidCache.clear();
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
        public Collection<RaidReward> getRewardsForRaid(int raidId) {
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
                                rrewards.setRewardChars(getCharsForReward(rrewards.getId()));
                                raidRewards.add(rrewards);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return raidRewards;
        }

        @Override
        public Collection<RaidItem> getItemsForRaid(int raidId) {
                Connection c = null;
                List<RaidItem> raidItems = new ArrayList<RaidItem>();
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("SELECT characters.name,items.id,items.name,loots.price,items.quality,loots.heroic FROM loots JOIN items JOIN characters WHERE loots.item_id=items.id AND loots.character_id=characters.id AND loots.raid_id=?");
                        p.setInt(1, raidId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {

                                RaidItem rItem = new RaidItem();
                                rItem.setId(rs.getInt("items.id"));
                                rItem.setName(rs.getString("items.name"));
                                rItem.setLooter(rs.getString("characters.name"));
                                rItem.setPrice(rs.getDouble("loots.price"));
                                rItem.setHeroic(rs.getBoolean("loots.heroic"));
                                rItem.setQuality(rs.getString("items.quality"));
                                raidItems.add(rItem);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
                return raidItems;
        }

        @Override
        public Collection<RaidChar> getCharsForReward(int id) {
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
                        }
                } catch (SQLException e) {
                        e.printStackTrace();

                } finally {
                        closeConnection(c);
                }
                return raidChars;
        }

        @Override
        public int doRaidUpdate(Raid raid, String raidzoneName, String raidcomment, String raiddate) {
                int success = 0;
                Connection c = null;
                int zoneid = getZoneIdByName(raidzoneName);
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("UPDATE raids SET zone_id=? , date=? , comment=? WHERE id=?");
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

        @Override
        public int getZoneIdByName(String raidzoneName) {
                DBConnection c = new DBConnection();
                int zoneid = 1;
                try {
                        PreparedStatement pzone = c.prepareStatement("SELECT * FROM zones WHERE name=?");
                        pzone.setString(1, raidzoneName);
                        ResultSet rs = pzone.executeQuery();
                        while (rs.next()) {
                                zoneid = rs.getInt("id");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return zoneid;
        }

        @Override
        public String getZoneNameById(int raidzoneid) {
                DBConnection c = new DBConnection();
                String zonename = "";
                try {
                        PreparedStatement pzone = c.prepareStatement("SELECT * FROM zones WHERE id=?");
                        pzone.setInt(1, raidzoneid);
                        ResultSet rs = pzone.executeQuery();
                        while (rs.next()) {
                                zonename = rs.getString("name");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return zonename;
        }

        @Override
        public int doUpdateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment) {
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
        public void addLootToRaid(Raid raid, String name, String loot, boolean heroic, double price) {
                Connection c = null;
                try {
                        c = new DBConnection().getConnection();
                        CharacterDAO characterDao = new CharacterDB();
                        ItemDAO itemDao = new ItemDB();
                        int itemid = itemDao.getItemId(loot);
                        int charid = characterDao.getCharacterId(name);
                        PreparedStatement ps = c.prepareStatement("INSERT INTO loots (item_id, raid_id, character_id, price, heroic) VALUES(?,?,?,?,?)");
                        ps.setInt(1, itemid);
                        ps.setInt(2, raid.getId());
                        ps.setInt(3, charid);
                        ps.setDouble(4, price);
                        ps.setBoolean(5, heroic);
                        ps.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                } finally {
                        closeConnection(c);
                }
        }

        @Override
        public int removeReward(RaidReward reward) {
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
                reward = doAddReward(reward);
                doAddCharacterReward(reward);
                return reward.getId();
        }

        private RaidReward doAddReward(RaidReward reward) {
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
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return reward;
        }

        private void doAddCharacterReward(RaidReward reward) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("INSERT INTO character_rewards (reward_id, character_id) values(?,?)");
                        for (RaidChar eachid : reward.getRewardChars()) {
                                p.setInt(1, reward.getId());
                                p.setInt(2, eachid.getId());
                                p.executeUpdate();
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
        }

        @Override
        public int removeLootFromRaid(RaidItem item) {
                Connection c = null;
                int success = 0;
                CharacterDAO characterDao = new CharacterDB();
                int charid = characterDao.getCharacterId(item.getLooter());
                try {
                        c = new DBConnection().getConnection();
                        PreparedStatement p = c.prepareStatement("DELETE FROM loots WHERE item_id=? AND character_id=?");
                        p.setInt(1, item.getId());
                        p.setInt(2, charid);
                        System.out.println(p.toString());
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
        public List<Raid> getRaidsForCharacter(int charid) {
                List<Raid> raids = new ArrayList<Raid>();
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT DISTINCT raids.* FROM raids JOIN rewards JOIN characters JOIN character_rewards WHERE characters.id=? AND rewards.raid_id=raids.id AND character_rewards.character_id=characters.id AND character_rewards.reward_id=rewards.id");
                        p.setInt(1, charid);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                Raid raidtemp = new Raid("", rs.getString("raids.comment"), rs.getString("raids.date"), rs.getInt("raids.id"));
                                raids.add(raidtemp);
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return raids;
        }

        @Override
        public int getTotalRaidsLastThirtyDays() {
                int total_raids = 0;
                DateTime dt = new DateTime();
                String startdate = dt.toYearMonthDay().minusDays(30).toString();
                String enddate = dt.toYearMonthDay().toString();
                DBConnection c = new DBConnection();
                try {

                        PreparedStatement p = c.prepareStatement("SELECT COUNT(distinct raids.id) as total_raids FROM raids JOIN rewards WHERE raids.id=rewards.raid_id AND date BETWEEN ? AND ?");
                        p.setString(1, startdate);
                        p.setString(2, enddate);
                        ResultSet rs = p.executeQuery();
                        if (rs.next()) {
                                total_raids = rs.getInt("total_raids");
                        }
                } catch (SQLException e) {
                } finally {
                        c.close();
                }
                return total_raids;
        }

        @Override
        public int getAttendedRaidsLastThirtyDays(User user) {
                int i = 0;
                DateTime dt = new DateTime();
                String startdate = dt.toYearMonthDay().minusDays(30).toString();
                String enddate = dt.toYearMonthDay().toString();
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT DISTINCT raid_id FROM characters JOIN character_rewards JOIN rewards JOIN raids WHERE characters.id=character_rewards.character_id AND characters.id=? AND rewards.id=character_rewards.reward_id AND raids.id = rewards.raid_id AND raids.date BETWEEN ? AND ?");
                        p.setInt(1, user.getId());
                        p.setString(2, startdate);
                        p.setString(3, enddate);
                        ResultSet rs = p.executeQuery();
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
        public boolean getLootedHeroic(String charname, int itemid, double price) {
                DBConnection c = new DBConnection();
                boolean isheroic = false;
                CharacterDAO characterDao = new CharacterDB();
                int charid = characterDao.getCharacterId(charname);
                try {
                        PreparedStatement p = c.prepareStatement("SELECT DISTINCT * FROM loots WHERE item_id=? AND character_id=? AND price=?");
                        p.setInt(1, itemid);
                        p.setInt(2, charid);
                        p.setDouble(3, price);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                isheroic = rs.getBoolean("heroic");
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return isheroic;
        }

        @Override
        public int doUpdateLoot(int lootid, String looter, String itemname, double price, boolean heroic, int raidid) {
                ItemDAO itemDao = new ItemDB();
                CharacterDAO charDao = new CharacterDB();
                int itemid = itemDao.getItemId(itemname);
                int charid = charDao.getCharacterId(looter);
                int success = 0;
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("UPDATE loots SET item_id=? , raid_id=? , character_id=? , price=? , heroic=? WHERE id=?");
                        p.setInt(1, itemid);
                        p.setInt(2, raidid);
                        p.setInt(3, charid);
                        p.setDouble(4, price);
                        p.setBoolean(5, heroic);
                        p.setInt(6, lootid);
                        success = p.executeUpdate();
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

        @Override
        public void removeZone(String zone) {
                fixExistingZonesToDefaultWhenRemovingThatZone(zone);
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("DELETE FROM zones WHERE name=?");
                        p.setString(1, zone);
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        @Override
        public void addZone(String zone) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("INSERT INTO zones (name) VALUES(?)");
                        p.setString(1, zone);
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        private void fixExistingZonesToDefaultWhenRemovingThatZone(String zone) {
                DBConnection c = new DBConnection();
                int zoneid = 1;
                try {
                        zoneid = getZoneIdByName(zone);
                        PreparedStatement p = c.prepareStatement("UPDATE raids SET zone_id=? WHERE zone_id=?");
                        p.setInt(1, 1);
                        p.setInt(2, zoneid);
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        @Override
        public void updateZoneName(String oldZone, String newZone) {
                DBConnection c = new DBConnection();
                int zoneid = 0;
                try {
                        zoneid = getZoneIdByName(oldZone);
                        PreparedStatement p = c.prepareStatement("UPDATE zones SET name=? WHERE id=?");
                        p.setString(1, newZone);
                        p.setInt(2, zoneid);
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        private int getValidZoneByName(String raidzoneName) {
                DBConnection c = new DBConnection();
                int zoneid = 0;
                try {
                        PreparedStatement pzone = c.prepareStatement("SELECT * FROM zones WHERE name=?");
                        pzone.setString(1, raidzoneName);
                        ResultSet rs = pzone.executeQuery();
                        while (rs.next()) {
                                zoneid = rs.getInt("id");
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return zoneid;
        }

        @Override
        public boolean isValidZone(String oldzone) {
                int id = 0;
                id = getValidZoneByName(oldzone);
                if (id == 0) {
                        return false;
                } else {
                        return true;
                }
        }

        @Override
        public void safelyRemoveRaid(Raid raid) {
                deleteRewardsForRaid(raid);
                deleteLootsForRaid(raid);
                deleteRaid(raid);
        }

        private void deleteRewardsForRaid(Raid raid) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("DELETE FROM rewards WHERE raid_id=?");
                        p.setInt(1, raid.getId());
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        private void deleteLootsForRaid(Raid raid) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("DELETE FROM loots WHERE raid_id=?");
                        p.setInt(1, raid.getId());
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        private void deleteRaid(Raid raid) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("DELETE FROM raids WHERE id=?");
                        p.setInt(1, raid.getId());
                        p.executeUpdate();
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
        }

        @Override
        public Raid getRaid(String raidcomment, String raiddate) {
                for (Raid r : getRaids()) {
                        if (r.getComment().equalsIgnoreCase(raidcomment) && r.getDate().equalsIgnoreCase(raiddate)) {
                                return r;
                        }
                }
                return null;
        }

        @Override
        public List<Adjustment> getAdjustmentsForCharacter(int charid) {
                List<Adjustment> pun = new ArrayList<Adjustment>();
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement p = c.prepareStatement("SELECT * FROM adjustments WHERE character_id=?");
                        p.setInt(1, charid);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                Adjustment pTemp = new Adjustment();
                                pTemp.setCharId(rs.getInt("character_id"));
                                pTemp.setComment(rs.getString("comment"));
                                pTemp.setDate(rs.getString("date"));
                                pTemp.setId(rs.getInt("id"));
                                pTemp.setShares(rs.getInt("shares"));
                                pun.add(pTemp);
                        }
                } catch (SQLException ex) {
                } finally {
                        c.close();
                }
                return pun;
        }

        @Override
        public int addAdjustment(Adjustment p) {
                p = doAddAdjustment(p);
                return p.getId();
        }

        private Adjustment doAddAdjustment(Adjustment p) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement ps = c.prepareStatement("INSERT INTO adjustments (shares, comment, character_id, date) values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                        ps.setInt(1, p.getShares());
                        ps.setString(2, p.getComment());
                        ps.setInt(3, p.getCharId());
                        ps.setString(4, p.getDate());
                        ps.executeUpdate();
                        ResultSet rs = ps.getGeneratedKeys();
                        while (rs.next()) {
                                p.setId(rs.getInt(1));
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
                return p;
        }

        @Override
        public void removeAdjustment(Adjustment p) {
                DBConnection c = new DBConnection();
                try {
                        PreparedStatement ps = c.prepareStatement("DELETE FROM adjustments WHERE id=?");
                        ps.setInt(1, p.getId());
                        ps.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                } finally {
                        c.close();
                }
        }
}
