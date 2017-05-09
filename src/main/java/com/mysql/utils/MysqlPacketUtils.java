package com.mysql.utils;

import java.nio.ByteBuffer;

import com.mysql.protocol.Packet;

public abstract class MysqlPacketUtils {
	
	
	/**
	 * 读取三个字节，不改变position,limit等值
	 * @param buf
	 * @return
	 */
	public static final int getPacketLength(ByteBuffer buf) {
		int position = buf.position();
		int b1 = buf.get(position) & 0xFF;
		int b2 = buf.get(position +1) & 0xFF;
		int b3 = buf.get(position + 2) & 0xFF;//这里简单起见，没有检测越界，由调用方做判断
		return  b3 << 16 | b2 << 8 | b1;
	}
	
	/**
	 * 获取数据包类型，将报文头部跳过，postion会落在第一个报文体上
	 * @return
	 */
	public static final short getPTypeByFrom1Byte(ByteBuffer buf) {
		skipReadBytes(buf, Packet.HEAD_LENGTH);
		return (short)(buf.get(buf.position()) & 0xFF);
	}
	
	private static final void skipReadBytes(ByteBuffer buf, int skipCount) {
		buf.position(buf.position() + skipCount);
	}

}
