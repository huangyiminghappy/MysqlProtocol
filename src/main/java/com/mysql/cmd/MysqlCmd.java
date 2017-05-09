package com.mysql.cmd;

import com.mysql.protocol.MysqlMessage;

public interface MysqlCmd {
	
	MysqlMessage mysqlCmd();
	
	int cmdType();

}
