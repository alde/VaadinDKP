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

        public Collection<RaidChar> getCharsForReward(int id) throws SQLException;
        
        public List<String> getRaidZoneList();

        public int addNewRaid(String zone, String comment, String date);

        public int doRaidUpdate(Raid raid, String raidzoneName, String raidcomment, String raiddate) throws SQLException;

        public int doUpdateReward(RaidReward reward, List<String> newAttendants, int newShares, String newComment) throws SQLException;

        public void addLootToRaid(Raid raid, String name, String loot, boolean heroic, double price) throws SQLException;

        public int removeReward(RaidReward reward) throws SQLException;

        public int addReward(RaidReward reward);

        public int removeLootFromRaid(RaidItem item) throws SQLException;

        public List<String> findInvalidCharacters(List<String> attendantlist, CharacterDAO charDao);

        public Collection<RaidChar> getRaidCharsForRaid(List<String> attendantlist, int raidId);

        public Iterable<Raid> getRaidsForCharacter(int charid) throws SQLException;

        public int getTotalRaidsLastThirtyDays();

        public int getAttendedRaidsLastThirtyDays(User user);
        
        public Collection<RaidReward> getRewardsForRaid(int raidId) throws SQLException;

        public int doUpdateLoot(int id, String looter, String itemname, double price, boolean heroic);

        public void removeZone(String zone);

        public void addZone(String zoneName);
}
