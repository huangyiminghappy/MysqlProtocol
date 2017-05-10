package com.mysql.protocol;

 import java.util.ArrayList;
import java.util.List;

import com.mysql.connection.Connection;

/**
 * 注：本次解析，重在将select 查询出来的数据，使用List<Object[]>返回，甚至转换为List<Bean>
 * 
 *
 */
@SuppressWarnings("serial")
public class ResultSetPacket extends Packet {
	
	private static final int STATUS_NONE = 0; //未开始解析
	private static final int STATUS_COLUMN = 1;//列信息解析中
	
	private static final int STATUS_COLUMN_END = 2;//列信息解析完成
	
	
	private static final int STATUS_ROWDATA = 4;//数据解析中
	
	private static final int STATUS_END = 8;    //包解析结束
	
	
	private Connection conn;

	/** 列的长度 */
	private int columnCount;
	private List<ColumnDefinition41Packet> columnDefinition41Packets;
	private List<Object[]> rowDatas;
	
	private int status; // 0:未开始;1: 解析column definition;2:解析rowdata中 ,3:结束
	
	private boolean hasNext = false; //是否有下一个ResultSet包
	
	/** 响应包类型   1:OK包;2:Error包;3:LoadDataFile包;4:ResultSetData包*/
	private int responseType;
	
	public ResultSetPacket(Connection conn) {
		this.conn = conn;
		this.rowDatas = new ArrayList<Object[]>();
//		this.columnCount = columnCount;
//		columnDefinition41Packets = new ArrayList<ColumnDefinition41Packet>(columnCount);
	}
	
	/**
	 * 由于是演示代码，内存使用的是堆内存，故内存的管理交给了垃圾回收器
	 * @param msg
	 */
	public void read(MysqlMessage msg) {
		if(responseType < STATUS_COLUMN ) {//说明该包还是第一次解析，需要判断响应包的类型
			int packetLen = msg.getPacketLength();
			byte packetSeq = msg.getPacketSeq();
			short pType = msg.getPTypeByFrom1Byte();
			System.out.println("数据包类型:" + pType + ",数据实体长度:" + packetLen);
			
			if(pType == 0xFf) { // Error Packet
				ErrorPacket errorPacket = ErrorPacket.newInstance(msg, packetSeq, packetLen);
				System.out.println(errorPacket);
				conn.endCmd();
				
				this.responseType = 2;
				this.status = STATUS_END; //包解析结束
				return;
			} else if(pType == 0) { //OK Packet,,目前这里发的是EOF包
				OkPacket ok = OkPacket.newInstance(msg, packetSeq, packetLen);
				System.err.println(ok); 
				
				conn.endCmd();
				this.responseType = 1;
				this.status = STATUS_END; //包解析结束
				return;
			} else if(pType == 0xFB) { // load_data_request 包
				conn.endCmd();
				this.responseType = 3;
				this.status = STATUS_END; //包解析结束
				return;
			} else {
				
				this.responseType = 4;
				
				//判断是否是LengthCodeInt类型
				try {
					long columnCount = msg.getBinaryLengthCode();
					System.out.println("字段长度：" + columnCount);
					this.columnCount = (int) columnCount;
					this.columnDefinition41Packets = new ArrayList<ColumnDefinition41Packet>(this.columnCount);
					this.status = STATUS_COLUMN; //column definition 解析中
					
				} catch (UnsupportedOperationException e) {
					System.out.println("不是一个合法的LengthCodeBinary包");
					conn.endCmd();
					this.responseType = 4;
					this.status = STATUS_END; //包解析结束
					return;
				}
				
			}
		}
		
		
		//开始包的解析
		if(status == STATUS_COLUMN) { //列信息解析
			int i = 0;
			
			while (msg.hasRemaining() && i++ < this.columnCount) {
				System.out.println("正在解析第" + (this.columnDefinition41Packets.size() + 1 ) + "列");
				this.columnDefinition41Packets.add( ColumnDefinition41Packet.newInstance(msg, false) );	
			}
			
			if( this.columnDefinition41Packets.size() < this.columnCount) {  //列描述包未全部解析完，待下次数据的到来
				return;
			}
			
			//列信息解析完，进入到 ResultData解析
			this.status = STATUS_COLUMN_END;//列信息解析完成后，会发送一个新的mysql数据包,故本方法就会结束，因为上层调用方只会传入一个完整的数据包
			
		} else if(status == STATUS_COLUMN_END ) { //这是一个OK包或EOF包，在这里，只需忽略掉这个包即可
//			while(msg.hasRemaining()) {
//				System.out.print(msg.byte2hex(msg.get()));
//			}
			this.status = STATUS_ROWDATA;
		} else if( status == STATUS_ROWDATA) {
			//需要判断该包是结束包，还是ResultData包
//			while(msg.hasRemaining()) {
//				System.out.print(msg.byte2hex(msg.get()));
//			}
			
			int packetLen = msg.getPacketLength();
			byte packetSeq = msg.getPacketSeq();
			short pType = msg.getPTypeByFrom1Byte();
			
			//结尾需要判断一下是 EOF包，还是OK包，重点关注服务器状态字段，判断是有更多ResultSet
			
			if(pType == 0xFE && packetLen < 9) { //EOF 包
				//msg.skipReadBytes(packetLen); //跳过协议头部和整个EOF包,,这里不能，得解析 是否还有ResultSet,因为可能支持多ResultSet
				//整个解析结束
				EOFPacket packet = EOFPacket.newInstance(msg, packetSeq, packetLen);
				
				if( (packet.getStatusFlags() & Packet.SERVER_MORE_RESULTS_EXISTS ) != 0 ) { //表明还有下一个
					this.hasNext = true;
				} 
				
				this.status = STATUS_END;
				
			} else if (pType == 0x00 && packetLen >= 7) { // OK包
				OkPacket packet = OkPacket.newInstance(msg, packetSeq, packetLen);
				if( (packet.getStatusFlags() & Packet.SERVER_MORE_RESULTS_EXISTS ) != 0 ) { //表明还有下一个
					this.hasNext = true;
				} 
				
				this.status = STATUS_END;
				
			} else {
				while(msg.hasRemaining()) {
					rowDatas.add( ResultSetDataPacket.newInstance(columnDefinition41Packets, msg).values()  );
				}
			}
			
			
		}
		
		
		
	}
	
	
	public boolean isEnd() {
		return this.status == STATUS_END;
	}
	

	public int getColumnCount() {
		return columnCount;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public List<ColumnDefinition41Packet> getColumnDefinition41Packets() {
		return columnDefinition41Packets;
	}

	public void setColumnDefinition41Packets(List<ColumnDefinition41Packet> columnDefinition41Packets) {
		this.columnDefinition41Packets = columnDefinition41Packets;
	}

	public List<Object[]> getRowDatas() {
		return rowDatas;
	}

	public void setRowDatas(List<Object[]> rowDatas) {
		this.rowDatas = rowDatas;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isHasNext() {
		return hasNext;
	}

	public int getResponseType() {
		return responseType;
	}
	
	

}
