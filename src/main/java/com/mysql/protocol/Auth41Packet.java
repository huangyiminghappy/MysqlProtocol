package com.mysql.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import com.mysql.MysqlClient;
import com.mysql.utils.SecurityUtil;
import com.mysql.utils.SeqUtils;

/**
 * 验证数据包
 * 客户端在收到服务端的握手协议后，需要向服务器验证权限（用户名进行登录）
 * @author dingwei2
 * 
 * Protocol::HandshakeResponse41:
 * 4              capability flags, CLIENT_PROTOCOL_41 always set
   4              max-packet size
   1              character set
string[23]       reserved (all [0])
string[NUL]      username
  if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
    lenenc-int     length of auth-response
    string[n]      auth-response
  } else if capabilities & CLIENT_SECURE_CONNECTION {
  1              length of auth-response
string[n]      auth-response
  } else {
string[NUL]    auth-response
  }
  if capabilities & CLIENT_CONNECT_WITH_DB {
string[NUL]    database
  }
  if capabilities & CLIENT_PLUGIN_AUTH {
string[NUL]    auth plugin name
  }
  if capabilities & CLIENT_CONNECT_ATTRS {
lenenc-int     length of all key-values
lenenc-str     key
lenenc-str     value
   if-more data in 'length of all key-values', more keys and value pairs
  }
 *
 */
public class Auth41Packet extends Packet {

	private Handshake10Packet handshake10Packet;

	private int capabilityFlags;
	private int maxPacketSize = 1 << 31 - 1;
	private byte characterSet;
	private final static byte[] reserved = new byte[23];
	private byte[] username;

	private byte authResponseLen;
	private byte[] authResponse;
	private byte[] database = MysqlClient.DB.getBytes();
	private byte[] authPluginName;

	static {

		for (int i = 0; i < 23; i++) {
			reserved[i] = FILLER;
		}
	}

	private Auth41Packet(Handshake10Packet handshake10Packet) {
		this.handshake10Packet = handshake10Packet;
	}

	public static final Auth41Packet newInstance(Handshake10Packet handshake10Packet) {
		Auth41Packet packet = new Auth41Packet(handshake10Packet);
		packet.setCapabilityFlags(getCapabilities());
		// packet.setCapabilityFlags(handshake10Packet.getServerCapability());
		packet.setCharacterSet(handshake10Packet.getCharacterSet());
		packet.setUsername(MysqlClient.USERNAME.getBytes());

		try {
			if (handshake10Packet.getAuthPluginDataPart2() == null) {
				packet.setAuthResponse(SecurityUtil.scramble411_2(MysqlClient.PWD.getBytes("UTF-8"),
						handshake10Packet.getAuthPluginDataPart1()));
			} else {
				final byte[] auth1 = handshake10Packet.getAuthPluginDataPart1();
				final byte[] auth2 = handshake10Packet.getAuthPluginDataPart2();

				byte[] seed = new byte[auth1.length + auth2.length - 1];
				System.arraycopy(auth1, 0, seed, 0, auth1.length);
				System.arraycopy(auth2, 0, seed, auth1.length, auth2.length - 1);

				// 关于seed 为什么只取auth2的 长度-1，是因为
				// Due to Bug#59453 the auth-plugin-name is missing the
				// terminating NUL-char
				// in versions prior to 5.5.10 and 5.6.2.;
				// 由于本示例代码的目的是为了学习mysql通信协议，所以这里就不做版本方面的兼容了。直接取auth2
				// 0-length-1个字节参与密码的加密

				byte[] authResponse = SecurityUtil.scramble411_2(MysqlClient.PWD.getBytes(), seed);
				packet.setAuthResponse(authResponse);
				packet.setAuthResponseLen((byte) authResponse.length);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		packet.setAuthPluginName(handshake10Packet.getAuthPluginName());

		return packet;

	}

	public int getPacketLength() {
		// 32 +
		int len = 32;
		if (username != null) {
			len += username.length + 1;// 1字节填充
		}

		if (authResponseLen > 0) {
			len += 1 + authResponseLen; // 1字节填充
		}

		if (database != null) {
			len += database.length + 1; // 1字节填充
		}

		if (authPluginName != null) {
			len += authPluginName.length + 1; // 1字节填充
		}

		return len;
	}

	/**
	 * 
	 * @param channel
	 * @return
	 */
	public int write(SelectableChannel channel) throws IOException {
		int packetLen = this.getPacketLength() + HEAD_LENGTH;
		byte seq = SeqUtils.getSeq(channel);

		ByteBuffer buf = ByteBuffer.allocate(packetLen);

		MysqlMessage msg = new MysqlMessage(buf);
		msg.putUB3(packetLen - HEAD_LENGTH);
		msg.put(seq); // 包头 3字节长度 + 1 字节序列号
		msg.putInt(this.getCapabilityFlags());
		msg.putInt(this.maxPacketSize);
		msg.put(this.getCharacterSet());
		msg.putBytes(reserved);
		msg.putBytes(this.username);
		msg.put(FILLER);
		msg.putBytes(this.authResponse);
		msg.put(FILLER);
		msg.putBytes(this.database);
		msg.put(FILLER);
		msg.putBytes(authPluginName);
		msg.put(FILLER);
		msg.flip();
		SocketChannel c = (SocketChannel) channel;
		return c.write(msg.nioBuffer());
	}

	public static final int getCapabilities() {
		return CLIENT_LONG_PASSWORD | CLIENT_FOUND_ROWS | CLIENT_CONNECT_WITH_DB |
		// CLIENT_COMPRESS ,压缩协议，为了简单，暂不开启
				CLIENT_LOCAL_FILES | CLIENT_IGNORE_SPACE | CLIENT_PROTOCOL_41 | CLIENT_INTERACTIVE
				| CLIENT_IGNORE_SIGPIPE | CLIENT_TRANSACTIONS |
				// CLIENT_SECURE_CONNECTION |
				CLIENT_MULTI_STATEMENTS |
				CLIENT_MULTI_RESULTS | CLIENT_PS_MULTI_RESULTS | CLIENT_PLUGIN_AUTH |
		// CLIENT_CONNECT_ATTRS |
		// CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS |
		// CLIENT_SESSION_TRACK |
		 CLIENT_DEPRECATE_EOF;
	}

	public Handshake10Packet getHandshake10Packet() {
		return handshake10Packet;
	}

	public void setHandshake10Packet(Handshake10Packet handshake10Packet) {
		this.handshake10Packet = handshake10Packet;
	}

	public int getCapabilityFlags() {
		return capabilityFlags;
	}

	public void setCapabilityFlags(int capabilityFlags) {
		this.capabilityFlags = capabilityFlags;
	}

	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	public void setMaxPacketSize(int maxPacketSize) {
		this.maxPacketSize = maxPacketSize;
	}

	public byte getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(byte characterSet) {
		this.characterSet = characterSet;
	}

	public byte[] getUsername() {
		return username;
	}

	public void setUsername(byte[] username) {
		this.username = username;
	}

	public static byte[] getReserved() {
		return reserved;
	}

	public byte getAuthResponseLen() {
		return authResponseLen;
	}

	public void setAuthResponseLen(byte authResponseLen) {
		this.authResponseLen = authResponseLen;
	}

	public byte[] getAuthResponse() {
		return authResponse;
	}

	public void setAuthResponse(byte[] authResponse) {
		this.authResponse = authResponse;
	}

	public byte[] getDatabase() {
		return database;
	}

	public void setDatabase(byte[] database) {
		this.database = database;
	}

	public byte[] getAuthPluginName() {
		return authPluginName;
	}

	public void setAuthPluginName(byte[] authPluginName) {
		this.authPluginName = authPluginName;
	}

}
