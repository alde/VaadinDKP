

package com.unknown.entity.dao;

import com.unknown.entity.character.SiteUser;


public interface ILoginDao {

	SiteUser checkLogin(String username, String password);

}
