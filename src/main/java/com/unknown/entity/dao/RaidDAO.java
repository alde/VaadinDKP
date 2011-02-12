/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.dao;

import com.unknown.entity.character.User;
import com.unknown.entity.raids.Raid;
import com.unknown.entity.raids.RaidChar;
import com.unknown.entity.raids.RaidItem;
import com.unknown.entity.raids.RaidReward;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author alde
 */
public interface RaidDAO {

        public List<Raid> getRaids();

        public void clearCache();

        public Collection<RaidChar> getCharsForReward(int id);
        
        public List<String> getRaidZoneList();

        public int addNewRaid(String zone, String comment, String date);

        public int doRaidUpdate(Raid raid, String raidzoneName, String raidcomment, String raiddate);

        public int doUpdateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment);

        public void addLootToRaid(Raid raid, String name, String loot, boolean heroic, double price);

        public int removeReward(RaidReward reward);

        public int addReward(RaidReward reward);

        public int removeLootFromRaid(RaidItem item);

        public List<String> findInvalidCharacters(List<String> attendantlist, CharacterDAO charDao);

        public Collection<RaidChar> getRaidCharsForRaid(List<String> attendantlist, int raidId);

        public List<Raid> getRaidsForCharacter(int charid);

        public int getTotalRaidsLastThirtyDays();

        public int getAttendedRaidsLastThirtyDays(User user);
        
        public Collection<RaidReward> getRewardsForRaid(int raidId) throws SQLException;

        public int doUpdateLoot(int loodid, String looter, String itemname, double price, boolean heroic, int raidid);

        public void removeZone(String zone);

        public void addZone(String zoneName);

        public boolean getLootedHeroic(String name, int id, double price);

        public String getZoneNameById(int raidzoneid);

        public void updateZoneName(String oldZone, String newZone);

        public int getZoneIdByName(String raidzoneName);

        public boolean isValidZone(String oldzone);

        public void safelyRemoveRaid(Raid raid);

        public Collection<RaidItem> getItemsForRaid(int id);

        public Raid getRaid(String raidname, String raiddate);
}
