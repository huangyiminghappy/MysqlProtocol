package com.mysql.connection;

import java.util.ArrayList;
import java.util.List;

import com.mysql.protocol.MysqlMessage;
import com.mysql.protocol.Packet;
import com.mysql.protocol.ResultSetPacket;

/**
 * @todo 这里可以先了解一下命令模式，看是否合适用命令模式来处理SQ命令
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public class CmdHandler implements IOHandler {
	
	private boolean isDebug = true; //这里只是开一个口子

	 
	public void handler(Connection conn, MysqlMessage msg) {
		// TODO Auto-generated method stub
		
		if(conn.getCmd() == Packet.QUERY_TYPE_SELECT) {
			List<ResultSetPacket> cmdData = conn.getCmdData();
			//ResultSetPacket rsePacket = (ResultSetPacket)conn.getCmdData();
			if(cmdData == null ) {
				cmdData = new ArrayList<ResultSetPacket>();
				cmdData.add(new ResultSetPacket(conn));
				conn.setCmdData(cmdData);
			}
			
			ResultSetPacket rsePacket = cmdData.get( cmdData.size() - 1 );
			rsePacket.read(msg);
			
			if(rsePacket.isEnd()) {
				
				if(isDebug) {
					System.out.println("\n====Select Query 解析结果 =====");
					
					List<Object[]> rowDatas = rsePacket.getRowDatas();
					if(rowDatas != null && !rowDatas.isEmpty()) {
						for(int i = 0, size = rowDatas.size(); i < size; i ++) {
							System.out.println("\n====第" + i + "行数据为:=====");
							Object[] vObjects = rowDatas.get(i);
							for(int j = 0, vs = vObjects.length; j <vs; j ++) {
								System.out.print( vObjects[j] + " ");
							}
							
						}
					}
				}
				
				
				if(rsePacket.isHasNext()) { // 是否还有下一个数据包
					cmdData.add(new ResultSetPacket(conn));
				} else {
					conn.endCmd();//命令结束
					
					System.out.println("\n====Select Query 最终解析结果 start =====");
					List<ResultSetPacket> cmdData2 = conn.getCmdData();
					if(cmdData2 != null && !cmdData2.isEmpty()) {
						int n = 1;
						for(ResultSetPacket p : cmdData2 ) {
							
							if(p.getResponseType() != 4 ) continue;
							
							System.out.println("\n====第" + n++ + "个ResultSet包信息=========");
							List<Object[]> rowDatas = p.getRowDatas();
							if(rowDatas != null && !rowDatas.isEmpty()) {
								for(int i = 0, size = rowDatas.size(); i < size; i ++) {
									System.out.println("\n====第" + i + "行数据为:=====");
									Object[] vObjects = rowDatas.get(i);
									for(int j = 0, vs = vObjects.length; j <vs; j ++) {
										System.out.print( vObjects[j] + " ");
									}
									
								}
							}
						}
					}
					System.out.println("\n====Select Query 最终解析结果 end =====");
					
					System.out.println("\n====命令解析完毕，待新的命令输入====");
				}
				
				
			}
		}

	}

}
