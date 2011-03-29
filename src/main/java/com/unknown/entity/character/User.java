/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

import com.google.common.collect.ImmutableList;
import com.unknown.entity.Armor;
import com.unknown.entity.Role;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author bobo
 */
public class User implements SiteUser {

        private final int id;
        private final String username;
        private final Role role;
        private int shares;
        private double dkp_earned;
        private double dkp_spent;
        private double dkp;
        private boolean active = true;
        private Armor armor;
        private int level = 0;
        private String siteusername;
        private final List<CharacterItem> charItems = new ArrayList<CharacterItem>();

        public User(int id, String username, Role role, boolean active, int shares, double dkp_earned, double dkp_spent, double dkp) {
                this.id = id;
                this.username = username;
                this.role = role;
                this.shares = shares;
                this.dkp = dkp;
                this.dkp_earned = dkp_earned;
                this.dkp_spent = dkp_spent;
                this.active = active;
                this.armor = role.getArmor();
        }

        public int getId() {
                return id;
        }

        public int getShares() {
                return shares;
        }

        public void setShares(int x) {
                shares = x;
        }

        public void inactivate() {
                active = false;
        }

        public void activate() {
                active = true;
        }

        public boolean isActive() {
                return active;
        }

        public Role getRole() {
                return role;
        }

        /** Character name */
        public String getUsername() {
                return username;
        }

        @Override
        public String toString() {
                return username;
        }

        public double getDKP() {
                return dkp;
        }

        public double getDKPSpent() {
                return dkp_spent;
        }

        public double getDKPEarned() {
                return dkp_earned;
        }

        public Armor getArmor() {
                return armor;
        }

        public void addCharItems(Collection<CharacterItem> items) {
                charItems.addAll(items);
        }

        public ImmutableList<CharacterItem> getCharItems() {
                return ImmutableList.copyOf(charItems);
        }

        @Override
        public int getLevel() {
                return this.level;
        }

        public void setLevel(int level) {
                this.level = level;
        }

        public void setSiteUserName(String name) {
                this.siteusername = name;
        }

        /** SiteUserName */
        @Override
        public String getName() {
                return this.siteusername;
        }
}
