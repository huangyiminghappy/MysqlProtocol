package com.mysql.connection;

import com.mysql.protocol.MysqlMessage;

public interface IOHandler {
	
	void handler(Connection conn, MysqlMessage msg);

}
