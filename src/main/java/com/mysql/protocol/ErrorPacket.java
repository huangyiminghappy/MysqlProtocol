package com.mysql.protocol;

/**
 * 服务端--》 客户端(Error报文)
 * This packet signals that an error occurred. 
 * It contains a SQL state value if CLIENT_PROTOCOL_41 is enabled.
 * 
 * @see https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html
 * 
 * Type	Name	Description
 * int<1>	header	[ff] header of the ERR packet
 * int<2>	error_code	error-code
 * if capabilities & CLIENT_PROTOCOL_41 {
 *   string[1]	sql_state_marker	# marker of the SQL State
 *   string[5]	sql_state	SQL State
 * }
 * string<EOF>	error_message	human readable error message
 * 
 * @author dingwei2
 *
 */
public class ErrorPacket extends Packet {
	
	private int errorCode;
	
	private byte sqlStateMarker;
	
	private byte[] sqlState;
	
	private byte[] errorMessage;
	
	private ErrorPacket() {}
	
	/**
	 * 
	 * @param msg
	 * @param seq  请求序列号
	 * @param packetLent   该数据包有效长度
	 * @return
	 */
	public static final ErrorPacket newInstance(MysqlMessage msg, int seq, int packetLent) {
		msg.skipReadBytes(1);
		ErrorPacket packet = new ErrorPacket();
		packet.setErrorCode(msg.getUB2());
		packet.setSqlStateMarker(msg.get());
		byte[] dst = new byte[5];
		msg.get(dst);
		packet.setSqlState(dst);
		dst = new byte[msg.remaining()];
		msg.get(dst);
		packet.setErrorMessage(dst);
		return packet;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("errorCode:").append(errorCode).append("\n")
		  .append("sqlStateMarker:").append(new String(new byte[]{sqlStateMarker})).append("\n");
		
		if( errorMessage != null ) {
			sb.append("errorMessage").append(new String(errorMessage)).append("\n");
		}
		  
		return sb.toString();
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public byte getSqlStateMarker() {
		return sqlStateMarker;
	}

	public void setSqlStateMarker(byte sqlStateMarker) {
		this.sqlStateMarker = sqlStateMarker;
	}

	public byte[] getSqlState() {
		return sqlState;
	}

	public void setSqlState(byte[] sqlState) {
		this.sqlState = sqlState;
	}

	public byte[] getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(byte[] errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
	
	

}
