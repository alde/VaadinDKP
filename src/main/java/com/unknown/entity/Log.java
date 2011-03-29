/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.unknown.entity;

/**
 *
 * @author alde
 */
public class Log {

        private String date;
        private String username;
        private String message;
        private String type;

        public Log(String date, String username, String message, String type) {
                this.date = date;
                this.username = username;
                this.message = message;
                this.type = type;
        }

        public String getDate() {
                return date;
        }

        public void setDate(String date) {
                this.date = date;
        }

        public String getMessage() {
                return message;
        }

        public void setMessage(String message) {
                this.message = message;
        }

        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String getType() {
                return type;
        }

        public void setType(String type) {
                this.type = type;
        }
}
