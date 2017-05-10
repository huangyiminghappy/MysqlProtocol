package com.mysql.protocol;


/**
 * 
 * 
 * @see https://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html
 * Type	Name	Description
 * int<1>	header	[fe] EOF header
 * if capabilities & CLIENT_PROTOCOL_41 {
 *  int<2>	warnings	number of warnings
 *  int<2>	status_flags	Status Flags
 * }

  *
 */
public class EOFPacket extends Packet {
	
	/** 服务器标志 */
	private int statusFlags;
	
	/** 警告行数 */
	private int warningNumber;
	
	private EOFPacket() {}
	
	/**
	 * 
	 * @param msg
	 * @param seq
	 * @param packetLent
	 * @return
	 */
	public static final EOFPacket newInstance(MysqlMessage msg, int seq, int packetLent) {
		msg.skipReadBytes(1);//跳过第一类型字节
		EOFPacket packet = new EOFPacket();
		packet.setWarningNumber(msg.getUB2());
		packet.setStatusFlags(msg.getUB2());
		return packet;
	}
	
	
	

	public int getStatusFlags() {
		return statusFlags;
	}

	public void setStatusFlags(int statusFlags) {
		this.statusFlags = statusFlags;
	}

	public int getWarningNumber() {
		return warningNumber;
	}

	public void setWarningNumber(int warningNumber) {
		this.warningNumber = warningNumber;
	}
	
	

}
