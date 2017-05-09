package com.mysql.cmd;

/**
 * 查询
 * @author Administrator
 *
 */
import java.nio.ByteBuffer;

import com.mysql.protocol.MysqlMessage;
import com.mysql.protocol.Packet;
public class SelectQueryCmd implements MysqlCmd {
	
	private String cmd;
	private byte seq;
	
	public SelectQueryCmd(String cmd, byte seq) {
		this.cmd = cmd;
		this.seq = seq;
	}
	
	
	
	public MysqlMessage mysqlCmd() {
		int packetLen = Packet.HEAD_LENGTH;
		byte[] data = cmd.getBytes();
		packetLen += data.length + 1;
		
		//内存的分配，在这里不关注，目的只是演示协议
		ByteBuffer buf = ByteBuffer.allocate(packetLen);
		MysqlMessage msg = new MysqlMessage(buf);
		msg.putUB3( packetLen - Packet.HEAD_LENGTH);
		msg.put(seq);
		msg.put(Packet.COM_QUERY);
		msg.putBytes(data);
		return msg;
	}



	public int cmdType() {
 		return Packet.QUERY_TYPE_SELECT;
	}



	 
	
	


}
