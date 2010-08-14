/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.dao;

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

        public Collection<User> getUsersWithRole(Role role);

        public int addNewCharacter(String name, String role, Boolean isActive) throws SQLException;
        
        public int getCharacterClassId(Connection c, String charclass) throws SQLException;

        public int getCharacterId(Connection c, String charclass) throws SQLException;

        public int updateCharacter(User user, String name, String charclass, boolean active);
}
