package com.mysql.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.mysql.MysqlClient;
import com.mysql.cmd.MysqlCmd;
import com.mysql.protocol.Auth41Packet;
import com.mysql.protocol.ErrorPacket;
import com.mysql.protocol.Handshake10Packet;
import com.mysql.protocol.MysqlMessage;
import com.mysql.protocol.OkPacket;
import com.mysql.protocol.Packet;
import com.mysql.utils.MysqlPacketUtils;

public class SocketWR {
	
	
	public static void doWrite(SelectionKey key, Connection conn) throws IOException {
		SocketChannel sc = conn.getChannel();
		Object attachment = key.attachment();
		if (attachment instanceof Handshake10Packet) {
			Handshake10Packet handshkakePacket = (Handshake10Packet) attachment;
			Auth41Packet handshakeResponse = Auth41Packet.newInstance(handshkakePacket);
			int wc = handshakeResponse.write(sc);
			System.out.println("写入通道数据：" + wc + "字节");
			clearOpWrite(key);

			conn.setHandshake(true);
			addOpRead(key);
			key.attach(null);
			clearOpWrite(key);
		} else if (attachment instanceof MysqlCmd) { // 命令处理
			MysqlCmd cmd = (MysqlCmd) attachment;
			ByteBuffer buffer = cmd.mysqlCmd().nioBuffer();
			buffer.flip();
			sc.write(buffer);// 请求包比较小，这里为了简单，就不考虑一次写不完的情况先
			conn.setCmd(cmd.cmdType());

			key.attach(null);
			clearOpWrite(key);
			addOpRead(key);

			// 不考虑一次无法写完整个请求包的情况
			// int totalWrite = 0;
			// int localWrite = 0;
			// while(buffer.hasRemaining()) {
			// localWrite = sc.write(buffer);
			// totalWrite += localWrite;
			// if(localWrite < 1 ) {
			// break;
			// }
			// }
			//
			// if(buffer.hasRemaining()) { //如果本次没有写完
			//
			// } else {
			// clearOpWrite(key);//取消写事件
			// }
			//
			// if(totalWrite > 0 ) {
			// addOpRead(key);
			// }

		}
	}
	
	
	
	/**
	 * 
	 * @param key
	 * @param conn
	 * @return 是否需要关闭通道
	 */
	
	public static void doRead(SelectionKey key, Connection conn) throws IOException {
		if(read0(key, conn)) {
			conn.close();
		}
		
		ByteBuffer readBuffer = conn.getReaderBuffer();
		
		//这里可以这样判断，是基于这样的约定，通过 ClientHelper.read 读取的字节，第一个元素即为可读，并且如果读缓存区没有处理完后，会压缩或再次放入Connection中
		if(readBuffer.position() <=  Packet.HEAD_LENGTH) {  //这里的长度可以更加优化，选mysql长度最小的包
			return; //不包括一个完整的数据包
		}
		
		
		if(conn.isHandshake() && conn.isAuth()) { //命令响应阶段
			//命令处理阶段，取消写事件，待有新的请求命令后，再关注写事件
			clearOpWrite(key);
			
			// 命令响应包
			readBuffer.flip();//变成可读模式
			
			
			for( ; ; ) { //循环处理
				
				if(readBuffer.hasRemaining() && readBuffer.remaining() >= Packet.HEAD_LENGTH ) {
					int packetLen = MysqlPacketUtils.getPacketLength(readBuffer); //该方法不会改变 position
					
					if((readBuffer.remaining() - Packet.HEAD_LENGTH) < packetLen ) { //如果消息剩余字节
						break;
					}
					
					//从recvBuf中读取一个包的数据
					byte[] packet = new byte[Packet.HEAD_LENGTH + packetLen];
					readBuffer.get(packet);
					
					MysqlMessage msg = new MysqlMessage(ByteBuffer.wrap(packet));
					
					conn.getIoHandler().handler(conn, msg);
				}
				
			}
		
		} else { //握手认证阶段
			//重构,主要的目的是为了方便当前任务的编写
			if(handsharkAndAuth(conn, readBuffer, key)) {
				conn.close();
			}
			
			addOpWrite(key);//握手成功后，会模拟发送命令，不这里需要关注写事件
			
		}
		
		
		if(readBuffer.hasRemaining()) { //如果有待处理字节，存起来，下次有更多数据到达时再处理
			readBuffer.clear();
		} else {
			readBuffer.compact();
		}
		
		
	}
	
	
	/**
	 * 
	 * @param conn
	 * @param recvBuffer
	 * @param key
	 * @return 返回是否退出
	 */
	private static final boolean handsharkAndAuth(Connection conn, ByteBuffer recvBuffer, SelectionKey key) {
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
				MysqlCmd cmd = MysqlClient.getTestTask(conn);
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
	
	
	private static ByteBuffer expandReaderByteBuf(ByteBuffer buf) {
		if(buf.remaining() == 0 ) {
			ByteBuffer tempBuf = ByteBuffer.allocate( buf.capacity() << 1  );
			buf.flip();//变成读模式
			tempBuf.put(buf);
			buf = null;
			return tempBuf;
		}
		return buf;
	}
	
	private static boolean read0(SelectionKey key, Connection conn) throws IOException {
		SocketChannel sc = (SocketChannel) key.channel();
		// Connection 中的 readerBuffer保持在可写入状态,并且是经过compact的，也就是第一元素即为可读的
		ByteBuffer recvBuffer = conn.getReaderBuffer();
		if(recvBuffer == null ) {
			recvBuffer = ByteBuffer.allocate(1024); // 固定1024字节用来接收数据
		}
		
		recvBuffer = expandReaderByteBuf(recvBuffer);
				
		int r = 0;
		int remaining = recvBuffer.remaining();
		int localRead = 0;
		while( (r = sc.read(recvBuffer) ) > 0 ) {   //一次性读完通道数据
			remaining -= r;
			localRead += r;
			if(r > 0 && remaining == 0 ) { //接收缓存区不足，扩容一倍
				recvBuffer = expandReaderByteBuf(recvBuffer);
				remaining = recvBuffer	.remaining();
			}
		}
		
		
		System.out.println("可读数据:" + localRead);
		if(r == -1 && localRead < 1) { //链路关闭了
			conn.setReaderBuffer(null);
			recvBuffer = null;
			return true;
		}
		
		if(r == 0 && recvBuffer.hasRemaining()) { //取消读事件
			clearOpRead(key);
			
		}
		
		conn.setReaderBuffer(recvBuffer);
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
