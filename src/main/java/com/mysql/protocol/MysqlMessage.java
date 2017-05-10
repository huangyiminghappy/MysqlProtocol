package com.mysql.protocol;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 解析mysql数据包
 * 数字型，使用小端序列进行传输
 *
 */
public class MysqlMessage implements Serializable {
	
	private ByteBuffer innerBuf;
	
	public MysqlMessage(ByteBuffer innerBuf) {
		this.innerBuf = innerBuf;
	}
	
	
	public static void main(String[] args) {
		ByteBuffer buf = ByteBuffer.allocate(1024);
		/*MysqlMessage msg = new MysqlMessage(buf);
 		msg.putUB3(90);*/
		/*buf.put((byte)8);
		buf.put((byte)9);
		System.out.println(buf.position());
		buf.flip();
		System.out.println(buf.get());
		System.out.println(buf.get());

		System.out.println(buf.position());*/
		
		MysqlMessage msg = new MysqlMessage(buf);
		msg.putUB3(1000);
		msg.innerBuf.flip();
		/*System.out.println(msg.get());
		System.out.println(msg.get());

		System.out.println(msg.get());*/
	 
		byte b1 = buf.get();
		byte b2 = buf.get();
		byte b3 = buf.get();
		int i = (0xFF &b1);
		i |= ((b2 & 0xFF) <<8);
		i |= ((b3 & 0xFF) <<16);
		System.out.println(i);


	}
	
	
	public final void put(byte b) {
		this.innerBuf.put(b);
	}
	
	public final void putUB3(int v) { //需要使用小端序列存放
		final ByteBuffer buf = this.innerBuf;
		System.out.println(buf.position());
		buf.put( (byte)(v & 0xff) );
		System.out.println(buf.position());

		buf.put( (byte)(v >>> 8) );
		System.out.println(buf.position());

		buf.put( (byte)(v >>> 16) );
		System.out.println(buf.position());

	}
	
	
	public final void putInt(int v) { // 需要使用小端序列存放
		final ByteBuffer buf = this.innerBuf;
		buf.put( (byte) v );
		buf.put( (byte) (v >> 8));
		buf.put( (byte) (v >> 16));
		buf.put( (byte) (v >> 24));
	}
	
	public final void putBytes(byte[] src) {
		final ByteBuffer buf = this.innerBuf;
		if(buf.remaining() < src.length ) {
			throw new ArrayIndexOutOfBoundsException(String.format("remaing:%d,incoming length:%d", buf.remaining(), src.length));
		}
		
		buf.put(src);
	}
	
	
	/**
	 * 从当前位置读取一个字节，改变position
	 * @return
	 */
	public final byte get() {
		final ByteBuffer buf = this.innerBuf;
		return buf.get();
	}
	
	/**
	 * 从当前位置读取4个字节，返回int类型，
	 * 改变position
	 * @return
	 */
	public final int getInt() {
		final ByteBuffer buf = this.innerBuf;
		byte b1 = buf.get();
		byte b2 = buf.get();
		byte b3 = buf.get();
		byte b4 = buf.get();
		//mysql是小端传输,int是大端,  转化回来
 		return (0xFF & b4) << 24 | (0xFF & b3) << 16 | (0xFF & b2) << 8 | (0xFF & b1);
	}
	
	public final int getUB2() {
 		final ByteBuffer buf = this.innerBuf;
		byte b1 = buf.get();
		byte b2 = buf.get();
		
		int i = (0xFF &b1);
		i |= ((b2 & 0xFF) <<8);
		return i;
	}
	
	/**
	 * 读取三个字节，返回int
	 * 不改变position,limit等值
	 * @param buf
	 * @return
	 */
	public final int getPacketLength() {
		final ByteBuffer buf = this.innerBuf;
		int position = buf.position();
		int b1 = buf.get(position) & 0xFF;
		int b2 = buf.get(position +1) & 0xFF;
		int b3 = buf.get(position + 2) & 0xFF;//这里简单起见，没有检测越界，由调用方做判断
		return  b3 << 16 | b2 << 8 | b1;
	}
	
	public final byte getPacketSeq() {
		final ByteBuffer buf = this.innerBuf;
		int position = buf.position();
		return buf.get(position + 3);
	}
	
	/**
	 * 获取数据包类型，将报文头部跳过，postion会落在第一个报文体上
	 * @return
	 */
	public final short getPTypeByFrom1Byte() {
		final ByteBuffer buf = this.innerBuf;
		skipReadBytes(Packet.HEAD_LENGTH);
		return (short)(buf.get(buf.position()) & 0xFF);
	}
	
	
	/**
	 * 从当前位置，读取一个 mysql  null-terminated-string
	 * @return
	 */
	public byte[] readNullTerminatedString() {
		final ByteBuffer buf = this.innerBuf;
		int remaining = buf.remaining();
		int position = buf.position();
		byte b;
		int i = 0;
		// 从position开始直到读到一个值为0为止并记录该位置
		for(i = 0; i < remaining; i ++ ) {
			b = buf.get(position + i);
			if(b == 0) break;
		}
		byte[] sb = new byte[i];
		//读取到从position到字节值为0的这段字节数组的值
		buf.get(sb);
		//下标加1
		skipReadBytes(1);
		return sb;
	}
	
	/**
	 * LengthCodeString协议，lenenc_string
	 * 获取使用LengthCodeString编码的字符串的内容，
	 * 存放形式为：
	 * LengthCodeString  + 字符串（长度由LengthCodeString表示)
	 * @return
	 */
	public byte[] getStringLengthCode() {
		long dataLen = getBinaryLengthCode();//获取字符串的长度
		if(dataLen == 0 ) return null;
		byte[] data = new byte[(int)dataLen];//这里可以安全的转换，因为mysql单个数据的最大长度为 longtext,2^32-1
		get(data);
		return data;
	}
	

	
	
	/**
	 * 读取一个 Length Code Binary 数据
	 * @return 返回
	 */
	public long getBinaryLengthCode() {
		final ByteBuffer buf = this.innerBuf;
		int position = buf.position();
		byte b = get();
		
		if( b < 251) {
			return b;
		}
		
		if( b == 251 ) {
			return 0;
		}
		
		if(b == 252 ) { //后续2个字节代表数据
			long v = get() &0xFF ;
			v |=   ( (get() & 0xFF) << 8);
			return v;
		}
		
		if(b == 253 ) { //后续3个字节代表数据
			long v = get() &0xFF ;
			v |=   ( (get() & 0xFF) << 8);
			v |=   ( (get() & 0xFF) << 16);
			return v;
		}
		
		if(b == 254 ) { //后续8个字节代表数据
			long v = get() &0xFF ;
			v |=   ( (get() & 0xFF) << 8);
			v |=   ( (get() & 0xFF) << 16);
			v |=   ( (get() & 0xFF) << 24);
			v |=   ( (get() & 0xFF) << 32);
			v |=   ( (get() & 0xFF) << 40);
			v |=   ( (get() & 0xFF) << 48);
			v |=   ( (get() & 0xFF) << 56);
			return v;
		}
		
		buf.position( position - 1 );
		throw new UnsupportedOperationException("不遵循LengthCodeInt标准");
		
	}
	
	public void get(byte[] dst) {
		this.innerBuf.get(dst);
	}
	
	
	
	public  String byte2hex(byte b){  
        String h = "";  
          
        String temp = Integer.toHexString(b & 0xFF);  
        if(temp.length() == 1){  
            temp = "0" + temp;  
        }  
        h = h + " "+ temp;  
        return h;  
          
    } 
	
	
	public final boolean hasRemaining() {
		final ByteBuffer buf = this.innerBuf;
		return buf.hasRemaining();
	}
	
	public final int remaining() {
		final ByteBuffer buf = this.innerBuf;
		return buf.remaining();
	}
	
	
	
	public final void skipReadBytes(int skipCount) {
		final ByteBuffer buf = this.innerBuf;
		buf.position(buf.position() + skipCount);
	}
	
	public final void flip() {
		this.innerBuf.flip();
	}
	
	public final ByteBuffer nioBuffer() {
		return this.innerBuf;
	}
	
	/**
	 * 接下来的值，是否是null value,不改变postion的值
	 * @return
	 */
	public final boolean isNullValue() {
		final ByteBuffer buf = this.innerBuf;
		int position = buf.position();
		int v = buf.get(position) & 0xFF;
		return v == 0xfb;
	}

	 
	
}
