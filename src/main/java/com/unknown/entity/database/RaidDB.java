package com.unknown.entity.database;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.character.Adjustment;
import com.unknown.entity.character.User;
import com.unknown.entity.raids.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.unknown.entity.Logg;
import com.unknown.entity.UnknownEntityDKP;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

public class RaidDB
{

    private static List<Raid> raidCache = new ArrayList<Raid>();

    public static List<Raid> getRaids()
    {
        if (raidCache != null) {
            if (!raidCache.isEmpty()) {
                return new ArrayList(raidCache);
            }
        }
        List<Raid> raids = new ArrayList<Raid>();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT * FROM raids JOIN zones ON raids.zone_id=zones.id");
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                final Raid raid = new Raid(rs.getString("zones.name"), rs.
                        getString("raids.comment"), rs.getString("raids.date"), rs.
                        getInt("raids.id"));
                raids.add(raid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        raidCache = raids;
        return raids;
    }

    public static void clearCache()
    {
        raidCache.clear();
    }

    private static PreparedStatement prepareStatement(String query) throws SQLException
    {
        return UnknownEntityDKP.getInstance().getConn().prepareStatement(query);
    }

    public static List<RaidChar> getCharsForRaid(int raidId)
    {
        List<RaidChar> raidChars = new ArrayList<RaidChar>();
        try {
            PreparedStatement p = prepareStatement("SELECT * FROM rewards JOIN character_rewards JOIN characters ON rewards.raid_id=? AND rewards.id=character_rewards.reward_id AND character_rewards.character_id=characters.id");
            p.setInt(1, raidId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                RaidChar rchar = new RaidChar();
                rchar.setId(rs.getInt("rewards.id"));
                rchar.setName(rs.getString("characters.name"));
                rchar.setShares(rs.getDouble("rewards.number_of_shares"));
                rchar.setRaidId(rs.getInt("rewards.raid_id"));
                raidChars.add(rchar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return raidChars;
    }

    public static List<String> getRaidZoneList()
    {
        List<String> zones = new ArrayList<String>();
        try {
            PreparedStatement pzone = prepareStatement("SELECT * FROM zones");
            ResultSet rzone = pzone.executeQuery();
            while (rzone.next()) {
                zones.add(rzone.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return zones;
    }

    public static int addNewRaid(String zone, String comment, String date)
    {
        int result = 0;
        int zoneId = 0;
        try {
            PreparedStatement ps = prepareStatement("INSERT INTO raids (zone_id, date, comment) VALUES(?,?,?)");
            PreparedStatement pzone = prepareStatement("SELECT * FROM zones WHERE name=?");
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
        }
        addLog("Added Raid [" + zone + " | " + comment + "]");
        return result;
    }

    private static void addCharsToReward(List<Integer> newcharid, RaidReward reward)
    {
        try {
            PreparedStatement p = prepareStatement("INSERT INTO character_rewards (reward_id, character_id) VALUES (?,?)");
            for (Integer i : newcharid) {
                p.setInt(1, reward.getId());
                p.setInt(2, i);
                p.addBatch();
            }
            p.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Collection<RaidReward> getRewardsForRaid(int raidId)
    {
        List<RaidReward> raidRewards = new ArrayList<RaidReward>();
        try {
            PreparedStatement p = prepareStatement("SELECT * FROM rewards WHERE rewards.raid_id=?");
            p.setInt(1, raidId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                RaidReward rrewards = new RaidReward();
                rrewards.setId(rs.getInt("rewards.id"));
                rrewards.setComment(rs.getString("rewards.comment"));
                rrewards.setShares(rs.getDouble("rewards.number_of_shares"));
                rrewards.setOriginalShares(rs.
                        getDouble("rewards.original_shares"));
                rrewards.setRewardChars(getCharsForReward(rrewards.getId()));
                raidRewards.add(rrewards);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return raidRewards;
    }

    public static Collection<RaidItem> getItemsForRaid(int raidId)
    {
        List<RaidItem> raidItems = new ArrayList<RaidItem>();
        try {
            PreparedStatement p =
                    prepareStatement("SELECT characters.name,items.id,items.name,loots.price,items.quality,loots.heroic FROM loots JOIN items JOIN characters WHERE loots.item_id=items.id AND loots.character_id=characters.id AND loots.raid_id=?");
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
        }
        return raidItems;
    }

    public static Collection<RaidChar> getCharsForReward(int id)
    {
        List<RaidChar> raidChars = new ArrayList<RaidChar>();
        try {
            PreparedStatement p =
                    prepareStatement(" SELECT * FROM character_rewards JOIN rewards JOIN characters WHERE character_rewards.reward_id=? AND rewards.id=? AND characters.id=character_rewards.character_id");
            p.setInt(1, id);
            p.setInt(2, id);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                RaidChar rchar = new RaidChar();
                rchar.setId(rs.getInt("character_rewards.id"));
                rchar.setName(rs.getString("characters.name"));
                rchar.setShares(rs.getDouble("rewards.number_of_shares"));
                rchar.setRaidId(rs.getInt("rewards.raid_id"));
                raidChars.add(rchar);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return raidChars;
    }

    public static int doRaidUpdate(Raid raid, String raidzoneName, String raidcomment, String raiddate)
    {
        int success = 0;
        int zoneid = getZoneIdByName(raidzoneName);
        try {
            PreparedStatement p =
                    prepareStatement("UPDATE raids SET zone_id=? , date=? , comment=? WHERE id=?");
            p.setInt(1, zoneid);
            p.setString(2, raiddate);
            p.setString(3, raidcomment);
            p.setInt(4, raid.getId());
            success = p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String foo = "";
        if (raid.getComment().equalsIgnoreCase(raidcomment)) {
            foo = raid.getComment() + " | ";
        } else {
            foo = raid.getComment() + " -> " + raidcomment + " | ";
        }
        if (!raid.getDate().equalsIgnoreCase(raiddate)) {
            foo = foo + raid.getDate() + " -> " + raiddate + " | ";
        }
        if (!raid.getRaidname().equalsIgnoreCase(raidzoneName)) {
            foo = foo + raid.getRaidname() + " -> " + raidzoneName;
        }
        addLog("Updated Raid [" + foo + "]");
        return success;
    }

    public static int getZoneIdByName(String raidzoneName)
    {
        int zoneid = 1;
        try {
            PreparedStatement pzone =
                    prepareStatement("SELECT * FROM zones WHERE name=?");
            pzone.setString(1, raidzoneName);
            ResultSet rs = pzone.executeQuery();
            while (rs.next()) {
                zoneid = rs.getInt("id");
            }
        } catch (SQLException ex) {
        }
        return zoneid;
    }

    public static String getZoneNameById(int raidzoneid)
    {
        String zonename = "";
        try {
            PreparedStatement pzone =
                    prepareStatement("SELECT * FROM zones WHERE id=?");
            pzone.setInt(1, raidzoneid);
            ResultSet rs = pzone.executeQuery();
            while (rs.next()) {
                zonename = rs.getString("name");
            }
        } catch (SQLException ex) {
        }
        return zonename;
    }

    public static int doUpdateReward(RaidReward reward, List<String> newAttendants, double newShares, String newComment)
    {
        int success = 0;
        try {
            if (newAttendants != null) {
                doUpdateCharacters(reward, newAttendants);
            }
            success += doUpdateSharesAndComment(reward, newShares, newComment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String foo = "";
        if (reward.getComment().equalsIgnoreCase(newComment)) {
            foo = reward.getComment() + " | ";
        } else {
            foo = reward.getComment() + " -> " + newComment + " | ";
        }
        if (Math.abs(reward.getShares() - newShares) > 0.001) {
            foo = foo + reward.getShares() + " -> " + newShares + " shares | ";
        }
        if (!newAttendants.isEmpty()) {
            foo = foo + "New Attendants: ( ";
            for (String a : newAttendants) {
                foo = foo + a + " ";
            }
            foo = foo + ")";
        }

        addLog("Updated Reward [" + foo + "]");
        return success;
    }

    private static void doUpdateCharacters(RaidReward reward, List<String> newAttendants) throws SQLException
    {
        List<Integer> newcharid = new ArrayList<Integer>();
        newAttendants = removeDuplicates(newAttendants);
        for (String s : newAttendants) {
            newcharid.add(CharDB.getCharacterId(s));
        }
        removeAllExistingCharactersFromReward(reward);
        addCharsToReward(newcharid, reward);

    }

    private static void removeAllExistingCharactersFromReward(RaidReward reward)
    {
        try {

            PreparedStatement p =
                    prepareStatement("DELETE FROM character_rewards WHERE reward_id=?");
            p.setInt(1, reward.getId());
            p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int doUpdateSharesAndComment(RaidReward reward, double newShares, String newComment) throws SQLException
    {
        PreparedStatement p =
                prepareStatement("UPDATE rewards SET number_of_shares=?, original_shares=?, comment=? WHERE id=?");
        p.setDouble(1, newShares);
        p.setDouble(2, newShares);
        p.setString(3, newComment);
        p.setInt(4, reward.getId());
        return p.executeUpdate();
    }

    private static List<String> removeDuplicates(List<String> attendants)
    {
        HashSet hs = new HashSet(attendants);
        List<String> clean = new ArrayList<String>();
        clean.addAll(hs);
        return clean;
    }

    public static void addLootToRaid(Raid raid, String name, String loot, boolean heroic, double price)
    {
        try {
            int itemid = ItemDB.getItemId(loot);
            int charid = CharDB.getCharacterId(name);
            PreparedStatement ps =
                    prepareStatement("INSERT INTO loots (item_id, raid_id, character_id, price, heroic) VALUES(?,?,?,?,?)");
            ps.setInt(1, itemid);
            ps.setInt(2, raid.getId());
            ps.setInt(3, charid);
            ps.setDouble(4, price);
            ps.setBoolean(5, heroic);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addLog("Added Loot [" + name + " looted " + loot + " in " + raid.
                getComment() + "]");
    }

    public static int removeReward(RaidReward reward)
    {
        int success = 0;
        try {
            PreparedStatement p =
                    prepareStatement("DELETE FROM rewards WHERE id=?");
            p.setInt(1, reward.getId());
            success += p.executeUpdate();
            p = prepareStatement("DELETE FROM character_rewards WHERE reward_id=?");
            p.setInt(1, reward.getId());
            success += p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addLog("Removed Reward [" + reward.getComment() + "]");
        return success;
    }

    public static int addReward(RaidReward reward)
    {
        reward = doAddReward(reward);
        doAddCharacterReward(reward);
        return reward.getId();
    }

    private static RaidReward doAddReward(RaidReward reward)
    {
        double shares = new BigDecimal(reward.getShares()).
                setScale(2, RoundingMode.HALF_UP).doubleValue();
        double original_shares = new BigDecimal(reward.getOriginalShares()).
                setScale(2, RoundingMode.HALF_UP).doubleValue();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("INSERT INTO rewards (number_of_shares, original_shares, comment, raid_id) values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            p.setDouble(1, shares);
            p.setDouble(2, original_shares);
            p.setString(3, reward.getComment());
            p.setInt(4, reward.getRaidId());
            p.executeUpdate();
            ResultSet rs = p.getGeneratedKeys();
            while (rs.next()) {
                reward.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        addLog("Added Reward [" + reward.getComment() + " | " + reward.
                getShares() + " shares]");
        return reward;
    }

    private static void doAddCharacterReward(RaidReward reward)
    {
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("INSERT INTO character_rewards (reward_id, character_id) values(?,?)");
            for (RaidChar eachid : reward.getRewardChars()) {
                p.setInt(1, reward.getId());
                p.setInt(2, eachid.getId());
                p.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static int removeLootFromRaid(RaidItem item)
    {
        int success = 0;
        int charid = CharDB.getCharacterId(item.getLooter());
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("DELETE FROM loots WHERE item_id=? AND character_id=?");
            p.setInt(1, item.getId());
            p.setInt(2, charid);
            success = p.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addLog("Removed Loot [" + item.getName() + " from " + item.getLooter() + "]");
        return success;
    }

    public static List<String> findInvalidCharacters(List<String> attendantlist)
    {
        List<String> invalid = new ArrayList<String>(attendantlist);
        invalid.removeAll(CharDB.getUserNames());
        return ImmutableList.copyOf(invalid);
    }

    public static Collection<RaidChar> getRaidCharsForRaid(List<String> attendantlist, int raidId)
    {
        Set<RaidChar> chars = new HashSet<RaidChar>();
        for (String string : attendantlist) {
            int characterId = CharDB.getCharacterId(string);
            chars.add(getRaidChar(characterId, raidId));
        }
        return chars;
    }

    public static RaidChar getRaidChar(int charId, int raidId)
    {
        RaidChar rchar = new RaidChar();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement(" SELECT DISTINCT * FROM rewards JOIN character_rewards JOIN characters WHERE rewards.raid_id=? AND rewards.id=character_rewards.reward_id AND character_rewards.character_id=? AND character_rewards.character_id=characters.id");
            p.setInt(1, raidId);
            p.setInt(2, charId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                rchar.setId(rs.getInt("characters.id"));
                rchar.setName(rs.getString("characters.name"));
                rchar.setShares(rs.getDouble("rewards.number_of_shares"));
                rchar.setRaidId(rs.getInt("rewards.raid_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rchar;
    }

    public static List<Raid> getRaidsForCharacter(int charid)
    {
        List<Raid> raids = new ArrayList<Raid>();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT DISTINCT raids.* FROM raids JOIN rewards JOIN characters JOIN character_rewards WHERE characters.id=? AND rewards.raid_id=raids.id AND character_rewards.character_id=characters.id AND character_rewards.reward_id=rewards.id");
            p.setInt(1, charid);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                Raid raidtemp = new Raid("", rs.getString("raids.comment"), rs.
                        getString("raids.date"), rs.getInt("raids.id"));
                raids.add(raidtemp);
            }
        } catch (SQLException ex) {
        }
        return raids;
    }

    public static int getTotalRaidsLastNDays(int n)
    {
        int total_raids = 0;
        DateTime dt = new DateTime();
        String startdate = dt.toYearMonthDay().minusDays(n).toString();
        String enddate = dt.toYearMonthDay().toString();
        try {

            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT COUNT(distinct raids.id) as total_raids FROM raids JOIN rewards WHERE raids.id=rewards.raid_id AND date BETWEEN ? AND ?");
            p.setString(1, startdate);
            p.setString(2, enddate);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                total_raids = rs.getInt("total_raids");
            }
        } catch (SQLException e) {
        }
        return total_raids;
    }

    public static int getAttendedRaidsLastNDays(int n, User user)
    {
        int i = 0;
        DateTime dt = new DateTime();
        String startdate = dt.toYearMonthDay().minusDays(n).toString();
        String enddate = dt.toYearMonthDay().toString();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT DISTINCT raid_id FROM characters JOIN character_rewards JOIN rewards JOIN raids WHERE characters.id=character_rewards.character_id AND characters.id=? AND rewards.id=character_rewards.reward_id AND raids.id = rewards.raid_id AND raids.date BETWEEN ? AND ?");
            p.setInt(1, user.getId());
            p.setString(2, startdate);
            p.setString(3, enddate);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    public static boolean getLootedHeroic(String charname, int itemid, double price)
    {
        boolean isheroic = false;
        int charid = CharDB.getCharacterId(charname);
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT DISTINCT * FROM loots WHERE item_id=? AND character_id=? AND price=?");
            p.setInt(1, itemid);
            p.setInt(2, charid);
            p.setDouble(3, price);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                isheroic = rs.getBoolean("heroic");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isheroic;
    }

    public static int doUpdateLoot(int lootid, String looter, String itemname, double price, boolean heroic, int raidid)
    {
        int itemid = ItemDB.getItemId(itemname);
        int charid = CharDB.getCharacterId(looter);
        int success = 0;
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("UPDATE loots SET item_id=? , raid_id=? , character_id=? , price=? , heroic=? WHERE id=?");
            p.setInt(1, itemid);
            p.setInt(2, raidid);
            p.setInt(3, charid);
            p.setDouble(4, price);
            p.setBoolean(5, heroic);
            p.setInt(6, lootid);
            success = p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        addLog("Updated Loot [" + itemname + " looted by " + looter + " (id: " + lootid + ")]");
        return success;
    }

    public static void removeZone(String zone)
    {
        fixExistingZonesToDefaultWhenRemovingThatZone(zone);
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("DELETE FROM zones WHERE name=?");
            p.setString(1, zone);
            p.executeUpdate();
        } catch (SQLException ex) {
        }
        addLog("Removed Zone [" + zone + "]");
    }

    public static void addZone(String zone)
    {
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("INSERT INTO zones (name) VALUES(?)");
            p.setString(1, zone);
            p.executeUpdate();
        } catch (SQLException ex) {
        }
        addLog("Added Zone [" + zone + "]");
    }

    private static void fixExistingZonesToDefaultWhenRemovingThatZone(String zone)
    {
        int zoneid = 1;
        try {
            zoneid = getZoneIdByName(zone);
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("UPDATE raids SET zone_id=? WHERE zone_id=?");
            p.setInt(1, 1);
            p.setInt(2, zoneid);
            p.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    public static void updateZoneName(String oldZone, String newZone)
    {
        int zoneid = 0;
        try {
            zoneid = getZoneIdByName(oldZone);
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("UPDATE zones SET name=? WHERE id=?");
            p.setString(1, newZone);
            p.setInt(2, zoneid);
            p.executeUpdate();
        } catch (SQLException ex) {
        }
        addLog("Renamed Zone [" + oldZone + " to " + newZone + "]");
    }

    private static int getValidZoneByName(String raidzoneName)
    {
        int zoneid = 0;
        try {
            PreparedStatement pzone = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT * FROM zones WHERE name=?");
            pzone.setString(1, raidzoneName);
            ResultSet rs = pzone.executeQuery();
            while (rs.next()) {
                zoneid = rs.getInt("id");
            }
        } catch (SQLException ex) {
        }
        return zoneid;
    }

    public static boolean isValidZone(String oldzone)
    {
        int id = 0;
        id = getValidZoneByName(oldzone);
        if (id == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static void safelyRemoveRaid(Raid raid)
    {
        deleteRewardsForRaid(raid);
        deleteLootsForRaid(raid);
        deleteRaid(raid);
    }

    private static void deleteRewardsForRaid(Raid raid)
    {
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("DELETE FROM rewards WHERE raid_id=?");
            p.setInt(1, raid.getId());
            p.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    private static void deleteLootsForRaid(Raid raid)
    {
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("DELETE FROM loots WHERE raid_id=?");
            p.setInt(1, raid.getId());
            p.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    private static void deleteRaid(Raid raid)
    {
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("DELETE FROM raids WHERE id=?");
            p.setInt(1, raid.getId());
            p.executeUpdate();
        } catch (SQLException ex) {
        }
        addLog("Removed Raid [" + raid.getDate() + " | " + raid.getComment() + " | " + raid.
                getRaidname() + "]");
    }

    public static Raid getRaid(String raidcomment, String raiddate)
    {
        for (Raid r : getRaids()) {
            if (r.getComment().equalsIgnoreCase(raidcomment) && r.getDate().
                    equalsIgnoreCase(raiddate)) {
                return r;
            }
        }
        return null;
    }

    public static List<Adjustment> getAdjustmentsForCharacter(int charid)
    {
        List<Adjustment> pun = new ArrayList<Adjustment>();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT * FROM adjustments WHERE character_id=?");
            p.setInt(1, charid);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                Adjustment pTemp = new Adjustment();
                pTemp.setCharId(rs.getInt("character_id"));
                pTemp.setComment(rs.getString("comment"));
                pTemp.setDate(rs.getString("date"));
                pTemp.setId(rs.getInt("id"));
                pTemp.setShares(rs.getDouble("shares"));
                pTemp.setOriginalShares(rs.getDouble("original_shares"));
                pun.add(pTemp);
            }
        } catch (SQLException ex) {
        }
        return pun;
    }

    public static int addAdjustment(Adjustment p)
    {
        p = doAddAdjustment(p);
        return p.getId();
    }

    private static Adjustment doAddAdjustment(Adjustment p)
    {
        double shares = new BigDecimal(p.getShares()).
                setScale(2, RoundingMode.HALF_UP).doubleValue();
        double original_shares = new BigDecimal(p.getOriginalShares()).
                setScale(2, RoundingMode.HALF_UP).doubleValue();
        try {
            PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("INSERT INTO adjustments (shares, original_shares, comment, character_id, date) values(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setDouble(1, shares);
            ps.setDouble(2, original_shares);
            ps.setString(3, p.getComment());
            ps.setInt(4, p.getCharId());
            ps.setString(5, p.getDate());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                p.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        addLog("Added adjustment [" + p.getDate() + " | " + p.getComment() + " (" + p.
                getShares() + " shares)] to " + getCharacter(p.getCharId()));
        return p;
    }

    public static void removeAdjustment(Adjustment p)
    {
        try {
            PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("DELETE FROM adjustments WHERE id=?");
            ps.setInt(1, p.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        addLog("Removed adjustment [" + p.getDate() + " | " + p.getComment() + " (" + p.
                getShares() + " shares)] from " + getCharacter(p.getCharId()));
    }

    private static void addLog(String message)
    {
        Logg logg = new Logg();
        logg.addLog(message, "raid");
    }

    private static String getCharacter(int id)
    {
        String foo = "";
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT name FROM characters WHERE id=?");
            p.setInt(1, id);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                foo = rs.getString("name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return foo;
    }

    private static Date getDateNMonthsAgo(int n)
    {
        DateTime dt = new DateTime();
        return dt.toYearMonthDay().minusMonths(n).toDateTimeAtCurrentTime().
                toDate();
    }

    public static HashMap<String, Integer> countRewardsAndAdjustmentsToBeDecayed() throws ParseException
    {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Date raidDate, adjustmentDate;
        int count_adj_old = 0;
        int count_rew_old = 0;
        int count_adj_mid = 0;
        int count_rew_mid = 0;

        Date two_months_ago = getDateNMonthsAgo(2);
        Date four_months_ago = getDateNMonthsAgo(4);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Raid r : getRaids()) {
            raidDate = sdf.parse(r.getDate());
            for (RaidReward rr : getRewardsForRaid(r.getId())) {
                if (raidDate.before(four_months_ago)) {
                    count_rew_old += 1;
                } else if (raidDate.before(two_months_ago)) {
                    count_rew_mid += 1;
                }
            }
        }
        for (Adjustment a : getAllAdjustment()) {
            adjustmentDate = sdf.parse(a.getDate());
            if (adjustmentDate.before(four_months_ago)) {
                count_adj_old += 1;
            } else if (adjustmentDate.before(two_months_ago)) {
                count_adj_mid += 1;
            }
        }

        map.put("adjustments_mid", count_adj_mid);
        map.put("adjustments_old", count_adj_old);
        map.put("rewards_mid", count_rew_mid);
        map.put("rewards_old", count_rew_old);

        return map;
    }

    public static int decayRaid(double decay_one, double decay_two) throws ParseException
    {
        int count = 0;
        Date raidDate;

        Date two_months_ago = getDateNMonthsAgo(2);
        Date four_months_ago = getDateNMonthsAgo(4);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Raid r : getRaids()) {
            raidDate = sdf.parse(r.getDate());
            for (RaidReward rr : getRewardsForRaid(r.getId())) {
                if (raidDate.before(four_months_ago)) {
                    count += doDecayReward(rr, decay_two);
                } else if (raidDate.before(two_months_ago)) {
                    count += doDecayReward(rr, decay_one);
                }
            }
        }
        addLog("Shares were decayed.");
        return count;
    }

    public static int decayAdjustments(double decay_one, double decay_two) throws ParseException
    {
        int count = 0;
        Date adjustmentDate;

        Date two_months_ago = getDateNMonthsAgo(2);
        Date four_months_ago = getDateNMonthsAgo(4);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Adjustment a : getAllAdjustment()) {
            adjustmentDate = sdf.parse(a.getDate());
            if (adjustmentDate.before(four_months_ago)) {
                count += doDecayAdjustments(a, decay_two);
            } else if (adjustmentDate.before(two_months_ago)) {
                count += doDecayAdjustments(a, decay_one);
            }
        }
        addLog("Adjustments were decayed.");
        return count;
    }

    private static int doDecayReward(RaidReward reward, double percent)
    {
        int result = 0;

        Double newShares = reward.getOriginalShares() - (reward.
                getOriginalShares() * (percent / 100));
        double shares = new BigDecimal(newShares).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        PreparedStatement p;
        try {
            p = prepareStatement("UPDATE rewards SET number_of_shares=? WHERE id=?");
            p.setDouble(1, shares);
            p.setInt(2, reward.getId());

            result = p.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(RaidDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static int doDecayAdjustments(Adjustment adj, double percent)
    {
        int result = 0;

        Double newShares = adj.getOriginalShares() - (adj.getOriginalShares() * (percent / 100));
        double shares = new BigDecimal(newShares).
                setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        try {
            PreparedStatement p = prepareStatement("UPDATE adjustments SET shares=? WHERE id=?");
            p.setDouble(1, shares);
            p.setInt(2, adj.getId());

            result = p.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(RaidDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static void restoreAdjustments() throws SQLException
    {
        PreparedStatement p = prepareStatement("SELECT id, original_shares FROM adjustments");
        ResultSet rs = p.executeQuery();

        while (rs.next()) {
            Adjustment adj = getAdjustment(rs.getInt("id"));
            adj.setShares(rs.getDouble("original_shares"));
            updateAdjustment(adj);
        }
    }

    public static void restoreRewards() throws SQLException
    {
        PreparedStatement p = prepareStatement("SELECT id, original_shares FROM rewards");
        ResultSet rs = p.executeQuery();

        while (rs.next()) {
            RaidReward reward = getReward(rs.getInt("id"));
            reward.setShares(rs.getDouble("original_shares"));
            updateReward(reward);
        }
    }

    private static Adjustment getAdjustment(int id) throws SQLException
    {
        PreparedStatement p = prepareStatement("SELECT `character_id`, `comment`, `date`, `shares`, `original_shares` FROM `adjustments` WHERE id=?");
        p.setInt(1, id);
        ResultSet rs = p.executeQuery();

        Adjustment a = new Adjustment();
        while (rs.next()) {
            a.setId(id);
            a.setCharId(rs.getInt("character_id"));
            a.setComment(rs.getString("comment"));
            a.setDate(rs.getString("date"));
            a.setShares(rs.getDouble("shares"));
            a.setOriginalShares(rs.getDouble("original_shares"));
        }
        return a;
    }

    private static void updateAdjustment(Adjustment adj) throws SQLException
    {
        PreparedStatement ps = prepareStatement("UPDATE adjustments SET character_id=?, comment=?, date=?, shares=? WHERE id=?");
        ps.setInt(1, adj.getCharId());
        ps.setString(2, adj.getComment());
        ps.setString(3, adj.getDate());
        ps.setDouble(4, adj.getShares());
        ps.setInt(5, adj.getId());
        ps.executeUpdate();
    }

    private static RaidReward getReward(int id) throws SQLException
    {
        PreparedStatement p = prepareStatement("SELECT `number_of_shares`, `original_shares`, `comment`, `raid_id`, `mob_id`, `loot_id` FROM `rewards` WHERE id=?");
        p.setInt(1, id);
        ResultSet rs = p.executeQuery();

        RaidReward reward = new RaidReward();
        while (rs.next()) {
            reward.setId(id);
            reward.setComment(rs.getString("comment"));
            reward.setRaidId(rs.getInt("raid_id"));
            reward.setShares(rs.getDouble("number_of_shares"));
            reward.setOriginalShares(rs.getDouble("original_shares"));
            reward.setRewardChars(getCharsForReward(id));
        }
        return reward;
    }

    private static void updateReward(RaidReward reward) throws SQLException
    {
        PreparedStatement p = prepareStatement("UPDATE rewards SET raid_id=?, comment=?, number_of_shares=? WHERE id=?");
        p.setInt(1, reward.getRaidId());
        p.setString(2, reward.getComment());
        p.setDouble(3, reward.getShares());
        p.setInt(4, reward.getId());
        p.executeUpdate();
    }

    private static Iterable<Adjustment> getAllAdjustment()
    {
        List<Adjustment> pun = new ArrayList<Adjustment>();
        try {
            PreparedStatement p = UnknownEntityDKP.getInstance().getConn().
                    prepareStatement("SELECT * FROM adjustments");
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                Adjustment pTemp = new Adjustment();
                pTemp.setCharId(rs.getInt("character_id"));
                pTemp.setComment(rs.getString("comment"));
                pTemp.setDate(rs.getString("date"));
                pTemp.setId(rs.getInt("id"));
                pTemp.setShares(rs.getDouble("shares"));
                pTemp.setOriginalShares(rs.getDouble("original_shares"));
                pun.add(pTemp);
            }
        } catch (SQLException ex) {
        }
        return pun;
    }
}
