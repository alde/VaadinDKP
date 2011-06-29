package com.unknown.entity.database;

import com.unknown.entity.Logg;
import com.unknown.entity.Slots;
import com.unknown.entity.Type;
import com.unknown.entity.UnknownEntityDKP;
import com.unknown.entity.character.CharacterItem;
import com.unknown.entity.items.ItemLooter;
import com.unknown.entity.items.ItemPrices;
import com.unknown.entity.items.Items;
import com.unknown.entity.items.Multiplier;
import com.unknown.entity.raids.RaidItem;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemDB {

        private static List<Items> itemCache = new ArrayList<Items>();

        public static List<Items> getItems() {
                if (itemCache != null) {
                        if (!itemCache.isEmpty()) {
                                return new ArrayList(itemCache);
                        }
                }
                List<Items> items = new ArrayList<Items>();
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM items");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                String temp = rs.getString("type");
                                Type tempType = typeFromString(temp);
                                Items tempitem = new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), tempType, rs.getInt("ilvl"), rs.getString("quality"));
                                tempitem.addLooterList(getLootersFormItems(rs.getInt("id")));
                                items.add(tempitem);
                        }
                } catch (SQLException e) {
                
                }
                itemCache = items;
                return items;
        }

        public static void clearCache() {
                itemCache.clear();
        }

        private static Collection<ItemLooter> getLootersFormItems(int itemId) {
                List<ItemLooter> looters = new ArrayList<ItemLooter>();
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM loots JOIN characters JOIN items JOIN raids WHERE loots.character_id=characters.id AND loots.item_id=? AND items.id=loots.item_id AND raids.id=loots.raid_id");
                        p.setInt(1, itemId);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                ItemLooter templooter = new ItemLooter();
                                templooter.setName(rs.getString("characters.name"));
                                templooter.setPrice(rs.getDouble("loots.price"));
                                templooter.setRaid(rs.getString("raids.comment"));
                                templooter.setDate(rs.getString("raids.date"));
                                templooter.setId(rs.getInt("loots.id"));
                                looters.add(templooter);
                        }
                } catch (SQLException e) {
                
                }

                return looters;
        }

        public static List<ItemPrices> getDefaultPrices() {

                List<ItemPrices> prices = new ArrayList<ItemPrices>();
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM default_prices");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                Slots slot = Slots.valueOf(rs.getString("slot"));
                                prices.add(new ItemPrices(slot, rs.getDouble("price_normal"), rs.getDouble("price_heroic")));
                        }
                } catch (SQLException e) {
                
                }
                return prices;
        }

        public static List<Multiplier> getMultipliers() {

                List<Multiplier> multi = new ArrayList<Multiplier>();
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM multiplier");
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                multi.add(new Multiplier(rs.getInt("id"), rs.getInt("ilvl"), rs.getDouble("multiplier")));
                        }
                } catch (SQLException e) {
                
                }
                return multi;
        }

        public static void addMultiplier(int ilvl, double multiplier) {
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("INSERT INTO multiplier (ilvl, multiplier) VALUES(?, ?)");
                        p.setInt(1, ilvl);
                        p.setDouble(2, multiplier);
                        p.executeUpdate();
                } catch (SQLException ex) {
                
                }
                addLog("Added Multiplier [ilvl: " + ilvl + " | " + "multiplier: " + multiplier + "]");
        }

        public static Multiplier getMultiplierForItemlevel(int ilvl) {

                Multiplier mp = null;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM multiplier WHERE ilvl=?");
                        p.setInt(1, ilvl);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                mp = new Multiplier(rs.getInt("id"), rs.getInt("ilvl"), rs.getDouble("multiplier"));
                        }
                } catch (SQLException ex) {
                
                }
                if (mp == null) {
                        mp = new Multiplier(0, ilvl, 1);
                }
                return mp;
        }

        public static int updateItem(Items item, String newname, Slots newslot, Type newtype, int newwowid, int newwowidhc, double newprice, double newpricehc, int ilvl, String quality) {
                int success = 0;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE items SET name=? , wowid_normal=? , wowid_heroic=? , price_normal=? , price_heroic=? , slot=? , type=? , ilvl=? , quality=? WHERE id=?");
                        p.setString(1, newname);
                        p.setInt(2, newwowid);
                        p.setInt(3, newwowidhc);
                        p.setDouble(4, newprice);
                        p.setDouble(5, newpricehc);
                        p.setString(6, newslot.toString());
                        p.setString(7, newtype.toString());
                        p.setInt(8, ilvl);
                        p.setString(9, quality);
                        p.setInt(10, item.getId());
                        success = p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                
                }
                String foo = "";
                if (!newname.equalsIgnoreCase(item.getName())) {
                        foo = item.getName() + " -> " + newname + " | ";
                } else {
                        foo = item.getName() + " | ";
                }
                if (!newslot.toString().equalsIgnoreCase(item.getSlot())) {
                        foo += item.getSlot() + " -> " + newslot.toString() + " | ";
                }
                if (!newtype.toString().equalsIgnoreCase(item.getType().toString())) {
                        foo += item.getType().toString() + " -> " + newtype.toString() + " | ";
                }
                if (item.getPrice() != newprice) {
                        foo += "Price: " + item.getPrice() + " -> " + newprice + " | ";
                }
                if (item.getPrice_hc() != newpricehc) {
                        foo += "Price (H): " + item.getPrice_hc() + " -> " + newpricehc + " | ";
                }
                if (foo.endsWith(" | ")) {
                        foo = foo.substring(0, foo.length() - 3);
                }
                addLog("Updated Item [" + foo + "]");
                return success;
        }

        public static int addItem(String name, int wowid, int wowid_hc, double price, double price_hc, String slot, String type, int ilvl, String quality) {
                int result = 0;
                try {
                        if (!itemAlreadyInDatabase(name)) {
                                PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("INSERT INTO items (name, wowid_normal, wowid_heroic, price_normal, price_heroic, slot, type, ilvl, quality) VALUES(?,?,?,?,?,?,?,?,?)");
                                ps.setString(1, name);
                                ps.setInt(2, wowid);
                                ps.setInt(3, wowid_hc);
                                ps.setDouble(4, price);
                                ps.setDouble(5, price_hc);
                                ps.setString(6, slot);
                                ps.setString(7, type);
                                ps.setInt(8, ilvl);
                                ps.setString(9, quality);
                                result = ps.executeUpdate();
                        } else {
                                result = -1;
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                
                }
                addLog("Added Item [" + name + "]");
                return result;
        }

        public static Object getItemPrice(String itemname, boolean heroic) {
                Double price = 0.0;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM items WHERE name=?");
                        p.setString(1, itemname);
                        ResultSet rs = p.executeQuery();
                        if (rs.next()) {
                                if (heroic) {
                                        price = rs.getDouble("price_heroic");
                                } else {
                                        price = rs.getDouble("price_normal");
                                }
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                
                }
                return price;
        }

        public static int getItemId(String loot) {

                int itemid = 0;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM items WHERE name=?");
                        p.setString(1, loot);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                itemid = rs.getInt("id");
                        }
                } catch (SQLException ex) {
                
                }
                return itemid;
        }

        public static int getLootId(int itemid, int charid, double price, Boolean heroic, int raidid) {

                int i = 0;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM loots WHERE item_id=? AND character_id=? AND price=? AND heroic=? AND raid_id=?");
                        p.setInt(1, itemid);
                        p.setInt(2, charid);
                        p.setDouble(3, price);
                        if (heroic) {
                                p.setInt(4, 1);
                        } else {
                                p.setInt(4, 0);
                        }
                        p.setInt(5, raidid);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                i = rs.getInt("id");
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
                return i;
        }

        public static Items getSingleItem(String name) {
                Items item = null;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM items WHERE name=?");
                        p.setString(1, name);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                String temp = rs.getString("type");
                                Type tempType = typeFromString(temp);
                                item = new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), tempType, rs.getInt("ilvl"), rs.getString("quality"));
                                item.addLooterList(getLootersFormItems(rs.getInt("id")));
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                }
                return item;
        }

        public static void updateDefaultPrice(String slot, double normalprice) {
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE default_prices SET price_normal=? WHERE slot=? ");
                        p.setDouble(1, normalprice);
                        p.setString(2, slot);
                        p.executeUpdate();
                } catch (SQLException e) {
                        e.printStackTrace();
                }
                addLog("Updated Default Price [" + slot + " | " + normalprice + "]");
        }

        public static int deleteItem(int id) {

                int success = 0;
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("DELETE FROM items WHERE id=?");
                        p.setInt(1, id);
                        success += p.executeUpdate();
                        p.getResultSet().close();
                        p = UnknownEntityDKP.getInstance().getConn().prepareStatement("DELETE FROM loots WHERE item_id=?");
                        p.setInt(1, id);
                        success += p.executeUpdate();
                } catch (SQLException ex) {
                
                }
                addLog("Deleted Item {" + getItemById(id).getName() + "]");
                return success;
        }

        public static ArrayList<RaidItem> getItemsForRaid(int id) {

                ArrayList<RaidItem> items = new ArrayList<RaidItem>();
                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM loots JOIN items ON loots.item_id = items.id JOIN characters ON loots.character_id = characters.id WHERE loots.raid_id = ?");
                        p.setInt(1, id);
                        ResultSet rs = p.executeQuery();
                        while (rs.next()) {
                                RaidItem tmp = new RaidItem(rs.getString("items.name"), rs.getString("characters.name"), rs.getInt("loots.item_id"), rs.getDouble("loots.price"), rs.getBoolean("loots.heroic"), rs.getString("items.quality"));
                                items.add(tmp);
                        }
                } catch (SQLException ex) {
                
                }
                return items;
        }

        public static Items getItemById(int id) {
                Items tmp = null;
                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT DISTINCT * FROM items WHERE id=?");
                        ps.setInt(1, id);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                String temp = rs.getString("type");
                                Type tempType = typeFromString(temp);
                                tmp = new Items(rs.getInt("id"), rs.getString("name"), rs.getInt("wowid_normal"), rs.getDouble("price_normal"), rs.getInt("wowid_heroic"), rs.getDouble("price_heroic"), rs.getString("slot"), tempType, rs.getInt("ilvl"), rs.getString("quality"));
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
                return tmp;
        }

        private static Type typeFromString(String temp) {
                Type tempType;
                if (temp.toString().equals("Hunter, Shaman, Warrior")) {
                        tempType = Type.protector;
                } else if (temp.toString().equals("Death Knight, Druid, Mage, Rogue")) {
                        tempType = Type.vanquisher;
                } else if (temp.toString().equals("Paladin, Priest, Warlock")) {
                        tempType = Type.conqueror;
                } else {
                        tempType = Type.valueOf(temp);
                }
                return tempType;
        }

        private static boolean itemAlreadyInDatabase(String name) {
                Items item = getSingleItem(name);
                if (item != null) {
                        return true;
                } else {
                        return false;
                }
        }

        public static void updateLoots(Items item, String price, String pricehc) {
                updateHeroic(item, pricehc);
                updateNormal(item, price);
                addLog("Updated Prices for [" + item.getName() + "]");
        }

        private static void updateHeroic(Items item, String pricehc) {

                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE loots SET price=? WHERE item_id=? AND heroic=1");
                        ps.setDouble(1, Double.parseDouble(pricehc));
                        ps.setInt(2, item.getId());
                        ps.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
        }

        public static void updateItemLevels(int id, int ilvl, double multiplier) {

                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE multiplier SET ilvl=? , multiplier=? WHERE id=? ");
                        p.setInt(1, ilvl);
                        p.setDouble(2, multiplier);
                        p.setInt(3, id);
                        p.executeUpdate();
                } catch (SQLException e) {
                
                }
                addLog("Updated Multiplier [ilvl: " + ilvl + " | multiplier: " + multiplier + "]");
        }

        public static void deleteItemLevelsMultiplier(int id) {

                try {
                        PreparedStatement p = UnknownEntityDKP.getInstance().getConn().prepareStatement("DELETE FROM multiplier WHERE id=?");
                        p.setInt(1, id);
                        p.executeUpdate();
                } catch (SQLException ex) {
                
                }
                addLog("Deleted Multiplier [id: " + id + "]");
        }

        public static double getDefaultPrice(Items item) {

                double d = 0.0;
                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * FROM default_prices WHERE slot=?");
                        ps.setString(1, item.getSlot());
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                d = rs.getDouble("price_normal");
                        }
                } catch (SQLException ex) {
                
                }
                return d;
        }

        private static void updateNormal(Items item, String price) {

                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE loots SET price=? WHERE item_id=? AND heroic=0");
                        ps.setDouble(1, Double.parseDouble(price));
                        ps.setInt(2, item.getId());
                        ps.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
        }

        public static void updateItemPrices(int id, BigDecimal formattedprice, BigDecimal formattedpricehc) {

                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE items SET price_normal=? , price_heroic=? WHERE id=?");
                        ps.setDouble(1, Double.parseDouble(formattedprice.toString()));
                        ps.setDouble(2, Double.parseDouble(formattedpricehc.toString()));

                        ps.setInt(3, id);
                        ps.executeUpdate();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
        }

        public static void updateLootedPrices(int id, BigDecimal formattedprice, BigDecimal formattedpricehc) {

                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE loots SET price=? WHERE id=? AND HEROIC=0");
                        ps.setDouble(1, Double.parseDouble(formattedprice.toString()));
                        ps.setInt(2, id);
                        ps.executeUpdate();

                        ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("UPDATE loots SET price=? WHERE id=? AND HEROIC=1");
                        ps.setDouble(1, Double.parseDouble(formattedpricehc.toString()));
                        ps.setInt(2, id);
                        ps.executeUpdate();

                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
        }

        public static List<CharacterItem> getLootForCharacter(String name) {
                List<CharacterItem> foo = new ArrayList<CharacterItem>();
                try {
                        PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT loots.heroic,loots.price,items.name,items.id,items.quality FROM loots JOIN items ON items.id=loots.item_id JOIN characters ON loots.character_id=characters.id WHERE characters.name=?");
                        ps.setString(1, name);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                                CharacterItem item = new CharacterItem();
                                item.setHeroic(rs.getBoolean("heroic"));
                                item.setId(rs.getInt("id"));
                                item.setName(rs.getString("name"));
                                item.setPrice(rs.getDouble("price"));
                                item.setQuality(rs.getString("quality"));
                                foo.add(item);
                        }
                } catch (SQLException ex) {
                        ex.printStackTrace();
                
                }
                return foo;
        }

        public static String getSlotForItemByName(String name) {
                return getItemById(getItemId(name)).getSlot();
        }

        private static void addLog(String message) {
                Logg logg = new Logg();
                logg.addLog(message, "item");
        }
}
