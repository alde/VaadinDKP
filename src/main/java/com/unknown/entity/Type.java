package com.unknown.entity;

/**
 *
 * @author alde
 */
public enum Type{
        Shields {
                @Override
                public String toString() {
                        return "Shields";
                }
        },
        Trinkets {

                @Override
                public String toString() {
                        return "Trinket";
                }
        },
        Cloth {

                @Override
                public String toString() {
                        return "Cloth";
                }
        },
        Leather {

                @Override
                public String toString() {
                        return "Leather";
                }
        },
        Mail {

                @Override
                public String toString() {
                        return "Mail";
                }
        },
        Plate {

                @Override
                public String toString() {
                        return "Plate";
                }
        },
        Weapons {

                @Override
                public String toString() {
                        return "Weapon";
                }
        },
        Weapon {
                @Override
                public String toString() {
                        return "Weapon";
                }
        },
        protector {

                @Override
                public String toString() {
                        return "Hunter, Shaman, Warrior";
                }
        },
        conqueror {

                @Override
                public String toString() {
                        return "Paladin, Priest, Warlock";
                }
        },
        vanquisher {

                @Override
                public String toString() {
                        return "Death Knight, Druid, Mage, Rogue";
                }
        },
        Other {

                @Override
                public String toString() {
                        return "Other";
                }
        };
}
