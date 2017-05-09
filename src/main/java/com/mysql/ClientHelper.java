package com.mysql;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.mysql.cmd.SelectQueryCmd;
import com.mysql.connection.Connection;
import com.mysql.protocol.ErrorPacket;
import com.mysql.protocol.Handshake10Packet;
import com.mysql.protocol.MysqlMessage;
import com.mysql.protocol.OkPacket;

public class ClientHelper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static final ByteBuffer read(SelectionKey key, SocketChannel sc, Connection conn) throws IOException{
		
		// Connection 中的 readerBuffer保持在可写入状态,并且是经过compact的，也就是第一元素即为可读的
		ByteBuffer recvBuffer = conn.getReaderBuffer();
		if(recvBuffer == null ) {
			recvBuffer = ByteBuffer.allocate(1024); // 固定1024字节用来接收数据
		}
				
		int r = 0;
		int remaining = recvBuffer.remaining();
		int localRead = 0;
		while( (r = sc.read(recvBuffer) ) > 0 ) {   //一次性读完通道数据
			remaining -= r;
			localRead += r;
			if(r > 0 && remaining == 0 ) { //接收缓存区不足，扩容一倍
				ByteBuffer tempBuf = ByteBuffer.allocate( recvBuffer.capacity() << 1  );
				recvBuffer.flip();//变成读模式
				tempBuf.put(recvBuffer);
				remaining = recvBuffer.remaining();
				recvBuffer = tempBuf;
			}
		}
		
		
		System.out.println("可读数据:" + localRead);
		if(r == -1 && localRead < 1) { //链路关闭了
			conn.setReaderBuffer(null);
			recvBuffer = null;
			return null;
		}
		
		if(r == 0 && recvBuffer.hasRemaining()) { //取消读事件
			clearOpRead(key);
			
		}
		
		conn.setReaderBuffer(recvBuffer);
		return recvBuffer;
	}
	
	
	
	/**
	 * 
	 * @param conn
	 * @param recvBuffer
	 * @param key
	 * @return 返回是否退出
	 */
	public static final boolean handsharkAndAuth(Connection conn, ByteBuffer recvBuffer, SelectionKey key) {
		if(!conn.isHandshake()) { //未验证，发送握手协议包
			//开始解析服务端发送过来的握手协议
			recvBuffer.flip();//变成可读模式
			Handshake10Packet handshkakePacket = Handshake10Packet.newInstance( new MysqlMessage(recvBuffer));
			System.out.println(handshkakePacket);
			if(handshkakePacket != null && !recvBuffer.hasRemaining()) { //如果解析出完整的包，并且recvBuffer
				//取消读事件
				clearOpRead(key);   //这里不考虑只接收到一半的数据包，继续下一次包解析，本示例主要关注的点mysql通信协议
			}
			
			//注册写事件
			key.attach(handshkakePacket);
			return false;
		} else if (!conn.isAuth()) { // 未成功授权，尝试解析服务端包
			//开始解析服务器授权响应报文
			recvBuffer.flip();//变成可读模式
			MysqlMessage msg = new MysqlMessage(recvBuffer);
			
			int packetLen = msg.getPacketLength();
			byte packetSeq = msg.getPacketSeq();
			
			short pType = msg.getPTypeByFrom1Byte();
			
			System.out.println("数据包类型:" + pType);
			
			if(pType == 0) { //OK数据包  //此处不考虑其他情况
				OkPacket ok = OkPacket.newInstance(msg, packetSeq, packetLen);
				System.err.println(ok); 
				
				conn.setAuth(true);
				System.out.println("成功通过验证");
				//接下来，取消读事件，开始发送命令给服务端，-----测试mysql的请求命令。
				clearOpRead(key);
				
				//测试 select 
				SelectQueryCmd cmd = new SelectQueryCmd(MysqlClient.TEST_SELECT_CMD, conn.getSeq());
				key.attach(cmd);
				
				return false;
				
				//目前暂时退出客户端
				//break loop;
				
			} else if(pType == 0xFF) { // error 包
				System.out.println("错误包");
				ErrorPacket errorPacket = ErrorPacket.newInstance(msg, packetSeq, packetLen);
				System.out.println(errorPacket);
				
				//然后退出 客户端
				return true;
				
			} else { 
				System.out.println("收到暂不支持的包，将退出");
				return true;
			}
			
			
		}
		
		return false;
	}
	
	public static void addOpRead(SelectionKey key) {
		key.interestOps(  key.interestOps() | SelectionKey.OP_READ  );
	}
	
	public static void clearOpRead(SelectionKey key) {
		key.interestOps(  key.interestOps() & ~SelectionKey.OP_READ  );
	}
	
	public static void clearOpWrite(SelectionKey key) {
		key.interestOps(  key.interestOps() & ~SelectionKey.OP_WRITE  );
	}
	
	public static void addOpWrite(SelectionKey key) {
		key.interestOps(  key.interestOps() | SelectionKey.OP_WRITE  );
	}

}
