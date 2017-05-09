package com.mysql;

 import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mysql.cmd.MysqlCmd;
import com.mysql.cmd.SelectQueryCmd;
import com.mysql.connection.Connection;


public class MysqlClient {
	
	private Map<Integer, Connection> connMap = new HashMap<Integer, Connection>();
	
	private AtomicInteger seq = new AtomicInteger(1);
	
	
	
	public static final String MYSQL_HOST = "10.40.6.187";
	public static final String DB = "db1";
	public static final String USERNAME = "root";
	public static final String PWD = "123456";
	public static final String TEST_SELECT_CMD = "select * from hotnews limit 10";
	//public static final String TEST_EXECUTE_PROCUDURE = "call dw_multi()";//调用存储过程
	public static final int MYSQL_PORT = 3306;
	
	

	
	
	/**
	 * mysql client 统一指定测试命令
	 * @param conn
	 * @return
	 */
	public static MysqlCmd getTestTask(Connection conn) {
		
		/**
		 * 测试 select 语句  == start 
		 */
		SelectQueryCmd cmd = new SelectQueryCmd(MysqlClient.TEST_SELECT_CMD, conn.getSeq());
		return cmd;
		// 测试 select 语句  == end
		
		
		/**
		 * 测试 调用存储过程
		 */
	//	SelectQueryCmd cmd = new SelectQueryCmd(TEST_EXECUTE_PROCUDURE, conn.getSeq());
	//	return cmd;
		
		
	}
	
	
	public Connection newConnection() {
		Connection conn = new Connection(MYSQL_HOST, MYSQL_PORT);
		conn.setConnectionId(seq.getAndAdd(1));
		connMap.put(conn.getConnectionId(), conn);
		
		(new Thread(conn)).start();
		return conn;
	}
	
	public static void main(String[] args) {
		MysqlClient client = new MysqlClient();
		client.newConnection();
	}
	
}
