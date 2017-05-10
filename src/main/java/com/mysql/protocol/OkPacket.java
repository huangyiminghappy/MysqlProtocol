package com.mysql.protocol;
/**
 * 
 * 
 * 服务端--》 客户端(Ok报文)
 * 
 * Table 14.1 Payload of OK Packet

 * Type	Name	Description
 * int<1>	header	[00] or [fe] the OK packet header
 * int<lenenc>	affected_rows	affected rows
 * int<lenenc>	last_insert_id	last insert-id
 
 * if capabilities & CLIENT_PROTOCOL_41 {
 * 	int<2>	status_flags	Status Flags
 * 	int<2>	warnings	number of warnings
 *  } elseif capabilities & CLIENT_TRANSACTIONS {
 * 	int<2>	status_flags	Status Flags
 * }
 *  if capabilities & CLIENT_SESSION_TRACK {
 *    string<lenenc>	info	human readable status information
 *  if status_flags & SERVER_SESSION_STATE_CHANGED {
 *   string<lenenc>	session_state_changes	session state info
 *  }
 * } else {
 * string<EOF>	info	human readable status information
 * }
 * 
 * 
 * These rules distinguish whether the packet represents OK or EOF:

 * OK: header = 0 and length of packet > 7

 * EOF: header = 0xfe and length of packet < 9

 * To ensure backward compatibility between old (prior to 5.7.5) and new (5.7.5 and up) versions of MySQL, new clients advertise the CLIENT_DEPRECATE_EOF flag:

 * Old clients do not know about this flag and do not advertise it. Consequently, the server does not send OK packets that represent EOF. (Old servers never do this, anyway. New servers recognize the absence of the flag to mean they should not.)

 * New clients advertise this flag. Old servers do not know this flag and do not send OK packets that represent EOF. New servers recognize the flag and can send OK packets that represent EOF.
 *
 *
 *@see https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html
 */
public class OkPacket extends Packet {
	
	/** 受影响的行 */
	private long affectedRows;
	
	/** 上一次插入id */
	private long lastInsertId;
	
	/** 服务器标志 */
	private int statusFlags;
	
	/** 警告行数 */
	private int warningNumber;
	
	private byte[] message;
	
	
	private OkPacket() {}
	
	/**
	 * 
	 * @param msg
	 * @param seq  请求序列号
	 * @param packetLent   该数据包有效长度
	 * @return
	 */
	public static final OkPacket newInstance(MysqlMessage msg, int seq, int packetLent) {
		
		msg.skipReadBytes(1);//跳过第一类型字节
		OkPacket packet = new OkPacket();
		packet.setAffectedRows(msg.getBinaryLengthCode());
		packet.setLastInsertId(msg.getBinaryLengthCode());
		packet.setStatusFlags(msg.getUB2());
		packet.setWarningNumber(msg.getUB2());
		
		if(msg.hasRemaining()) {
			byte[] messArr = new byte[msg.remaining()];
			msg.get(messArr);
			packet.setMessage(messArr);
		}
		
		return packet;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(200);
		sb.append("受影响的行数affectedRows:").append(affectedRows).append("\n")
		  .append("上一次插入lastInsertId:").append(lastInsertId).append("\n")
		  .append("服务器标志:").append(Integer.toHexString(statusFlags)).append("\n")
		  .append("警告行数:").append(warningNumber).append("\n");
		
		if(message != null) {
			sb.append("消息：").append(new String(message));
		}
		
		return sb.toString();
	}

	public long getAffectedRows() {
		return affectedRows;
	}

	public void setAffectedRows(long affectedRows) {
		this.affectedRows = affectedRows;
	}

	public long getLastInsertId() {
		return lastInsertId;
	}

	public void setLastInsertId(long lastInsertId) {
		this.lastInsertId = lastInsertId;
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

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}
	

}
