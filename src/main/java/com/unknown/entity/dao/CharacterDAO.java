/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.dao;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.Role;
import com.unknown.entity.character.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author bobo
 */
public interface CharacterDAO {

        public List<User> getUsers();

        public ImmutableList<String> getUserNames();

        public Collection<User> getUsersWithRole(Role role);

        public int addNewCharacter(String name, String role, Boolean isActive);

        public int getCharacterClassId(String charclass);

        public int getCharacterId(String charname);

        public int updateCharacter(User user, String name, String charclass, boolean active);

        public int addNewSiteUser(String username, String password, int rank);

        public String getAttendanceRaids(User user);

        public void removeLootFromCharacter(String itemname, User user);

        public void updateLootForCharacter(String itemname, double price, boolean heroic, User user, int lootid);

        public void deleteCharacter(User user);

        public String getRoleForCharacter(String name);
}
