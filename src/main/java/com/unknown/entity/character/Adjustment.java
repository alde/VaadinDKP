/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity.character;

/**
 *
 * @author alde
 */
public class Adjustment {

        private int id;
        private int charId;
        private int shares;
        private String date;
        private String comment;

        public String getComment() {
                return comment;
        }

        public void setComment(String comment) {
                this.comment = comment;
        }

        public String getDate() {
                return date;
        }

        public void setDate(String date) {
                this.date = date;
        }

        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public int getShares() {
                return shares;
        }

        public void setShares(int shares) {
                this.shares = shares;
        }

        public int getCharId() {
                return charId;
        }

        public void setCharId(int charId) {
                this.charId = charId;
        }
}
