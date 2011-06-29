
package com.unknown.entity.items;


public class Multiplier {

        private int id;
        private int ilvl;
        private double multiplier;

        public Multiplier(int id, int ilvl, double multiplier) {
                this.id = id;
                this.ilvl = ilvl;
                this.multiplier = multiplier;
        }

        public void setId(int id) {
                this.id = id;
        }
        public int getId() {
                return id;
        }
        public void setIlvl(int ilvl) {
                this.ilvl = ilvl;
        }
        public int getIlvl() {
                return ilvl;
        }
        public void setMultiplier(double multiplier) {
                this.multiplier = multiplier;
        }
        public double getMultiplier() {
                return multiplier;
        }
}
