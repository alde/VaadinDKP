

package com.unknown.entity;

import java.sql.SQLException;


public class SQLRuntimeException extends RuntimeException {
	private final SQLException ex;

	public SQLRuntimeException(SQLException ex) {
		this.ex = ex;

	}

	@Override
	public void printStackTrace() {
		ex.printStackTrace();
	}




}
