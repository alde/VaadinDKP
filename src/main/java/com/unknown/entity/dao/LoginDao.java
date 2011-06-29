package com.unknown.entity.dao;

import com.unknown.entity.UnknownEntityDKP;
import com.unknown.entity.character.SiteUser;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class LoginDao implements ILoginDao {

	@Override
	public SiteUser checkLogin(String username, String password) {
		try {
			PreparedStatement ps = UnknownEntityDKP.getInstance().getConn().prepareStatement("SELECT * from users where name=? and password = ? limit 1");
			ps.setString(1, username);
			ps.setString(2, password);
			final ResultSet res = ps.executeQuery();
			if (res.next()) {
				final int rank = res.getInt("rank");
                                final String name = res.getString("name");
				return new SUser(name, rank);

			}
		res.close();
		} catch (SQLException e) {
			e.printStackTrace();
		
		}
		return null;

	}

        private static class SUser implements SiteUser {

                private final int rank;
                private final String name;

                public SUser(String name, int rank) {
                        this.name = name;
                        this.rank = rank;
                }

                @Override
                public int getLevel() {
                        return rank;
                }

                @Override
                public String getName() {
                        return name;
                }
        }
}
