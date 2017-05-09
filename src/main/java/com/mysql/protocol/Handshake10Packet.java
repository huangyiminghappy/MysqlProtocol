package com.mysql.protocol;

/*
 * 服务器--》客户端  握手数据包
 * mysql4.1之后
 * 
 * mysql报文头格式固定为4字节:3字节数据报文长度，1字节序号
 * 
 * 报文体
 *    1                 协议版本号
 *    string[NUL]       服务器版本信息，(null-Termimated String)
 *    4                 服务器线程ID
 *    string[8]         随机挑战数(auth-plugin-data-part-1)
 *    1                 填充值0x00 filler
 *    2                 服务器权能标志(capability flags)(lower 2 bytes)
 *    if more data in the packet:
 *    1                 character set
 *    2                 status flags
 *    2                 capability flags (upper 2 bytes)
      if capabilities & CLIENT_PLUGIN_AUTH {
 *    1                 length of auth-plugin-data
      } else {
      1                 [00] filler
      }
 *    string[10]     reserved (all [00])
      if capabilities & CLIENT_SECURE_CONNECTION {
         string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
      if capabilities & CLIENT_PLUGIN_AUTH {
          string[NUL]    auth-plugin name
      }
 *                      
 */
@SuppressWarnings("serial")
public class Handshake10Packet extends Packet {

	// 协议版本号
	private byte protocolVersion;
	private byte[] serverVersion;
	private int connectionId;
	private byte[] authPluginDataPart1;
	private int serverCapability;
	private byte characterSet;
	private int serverStatus;
	private byte[] authPluginDataPart2;
	private byte[] authPluginName;
	
	private Handshake10Packet() {}
	
	
	/**
	 * 根据msg解析出该包
	 * @param msg
	 * @return
	 */
	public static final Handshake10Packet newInstance(MysqlMessage msg) {
		int recvBufferCapacity = msg.remaining();
		
		int dataLen = 0;
		if(recvBufferCapacity > 3) {
			dataLen = msg.getPacketLength();
		}
		
		if(recvBufferCapacity < HEAD_LENGTH + dataLen) { //不是一个完整的包名
			return null;
		}
		
		Handshake10Packet packet = new Handshake10Packet();
		msg.skipReadBytes(4);
		packet.setProtocolVersion(msg.get());
		packet.setServerVersion(msg.readNullTerminatedString());
		packet.setConnectionId(msg.getInt());
		byte[] authPluginDataPart1 = new byte[8];
		msg.get(authPluginDataPart1);
		packet.setAuthPluginDataPart1(authPluginDataPart1);
		
		msg.skipReadBytes(1);
		//两个字节的 capability flags
		packet.setServerCapability(msg.getUB2());//低位两字节的服务器权能标识符
		
		if(!msg.hasRemaining()) { //还有可读字节，继续解析
			return packet;
		}

		packet.setCharacterSet(msg.get()); //服务器编码
		packet.setServerStatus(msg.getUB2());
		int high = msg.getUB2();//服务器权能标志，高16位置
		int serverCapability = packet.getServerCapability() | ( high << 16 );
		packet.setServerCapability(serverCapability);
		
		int authPluginDataLen = 0;
		if( (serverCapability & CLIENT_PLUGIN_AUTH) != 0) {
			authPluginDataLen = msg.get();
		} else {
			msg.skipReadBytes(1);
		}
		
		msg.skipReadBytes(10);//10个填充字符
		
		if((serverCapability & CLIENT_SECURE_CONNECTION) != 0) {
			authPluginDataLen = Math.max( 13 , authPluginDataLen - 8);
			byte[] authPluginDataPart2 = new byte[authPluginDataLen];
			msg.get(authPluginDataPart2);
			packet.setAuthPluginDataPart2(authPluginDataPart2);
		}
		
		if( (serverCapability & CLIENT_PLUGIN_AUTH) != 0) {
			packet.setAuthPluginName( msg.readNullTerminatedString() );
		}
	
		return packet;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(100);
		str.append("协议版本号:").append(protocolVersion).append("\n")
			.append("服务器版本信息:").append(new String(serverVersion)).append("\n")
			.append("服务器连接线程ID:").append(connectionId).append("\n")
			.append("capabilityFlag:").append( Integer.toHexString(serverCapability)).append("\n")
			.append("serverCharact:").append(characterSet).append("\n")
			.append("statusFlags:").append(Integer.toHexString(serverStatus)).append("\n");
	
		if(authPluginDataPart1 != null && authPluginDataPart1.length > 0 ) {
			str.append("authPluginDataPart1:").append(new String(authPluginDataPart1)).append("\n");
		}
		
		if(authPluginDataPart2 != null && authPluginDataPart2.length > 0 ) {
			str.append("authPluginDataPart2:").append(new String(authPluginDataPart2)).append("\n");
		}
		
		if(authPluginName != null && authPluginName.length > 0 ) {
			str.append("authPluginName:").append(new String(authPluginName)).append("\n");
		}
		
		return str.toString();
	}

	public byte getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(byte protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public byte[] getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(byte[] serverVersion) {
		this.serverVersion = serverVersion;
	}

	public int getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(int connectionId) {
		this.connectionId = connectionId;
	}

	public byte[] getAuthPluginDataPart1() {
		return authPluginDataPart1;
	}

	public void setAuthPluginDataPart1(byte[] authPluginDataPart1) {
		this.authPluginDataPart1 = authPluginDataPart1;
	}

	public int getServerCapability() {
		return serverCapability;
	}

	public void setServerCapability(int serverCapability) {
		this.serverCapability = serverCapability;
	}

	public byte getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(byte characterSet) {
		this.characterSet = characterSet;
	}


	public int getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(int serverStatus) {
		this.serverStatus = serverStatus;
	}

	public byte[] getAuthPluginDataPart2() {
		return authPluginDataPart2;
	}

	public void setAuthPluginDataPart2(byte[] authPluginDataPart2) {
		this.authPluginDataPart2 = authPluginDataPart2;
	}

	public byte[] getAuthPluginName() {
		return authPluginName;
	}

	public void setAuthPluginName(byte[] authPluginName) {
		this.authPluginName = authPluginName;
	}

}
