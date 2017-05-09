package com.mysql.protocol;


import java.util.List;

@SuppressWarnings("serial")
public class ResultSetDataPacket extends Packet {

	@SuppressWarnings("unused")
	private List<ColumnDefinition41Packet> columnDefinition41Packets;

	private Object[] values;

	private ResultSetDataPacket() {
	}

	public static final ResultSetDataPacket newInstance(List<ColumnDefinition41Packet> columnDefinition41Packets,
			MysqlMessage msg) {
		
	//	msg.skipReadBytes(Packet.HEAD_LENGTH);
		
		int columnCount = columnDefinition41Packets.size();
		
		//这里就不按照 ColumnDefinition41Packet 的 type 字段 进行类型转换了，直接用字符串了
		Object[] values = new Object[columnCount];
		for(int i = 0; i < columnCount; i ++ ) {
			if(msg.isNullValue()) {
				values[i] = null;
				msg.skipReadBytes(1);
			} else {
				byte[] d = msg.getStringLengthCode();
				values[i] = d == null ? null: new String(d);
			}
			
		}
		
		ResultSetDataPacket packet = new ResultSetDataPacket();
		packet.setValues(values);
		
		return packet;
	}
	
	public Object[] values() {
		return this.values;
	}
	
	private void setValues(Object[] values) {
		this.values = values;
	}

}
