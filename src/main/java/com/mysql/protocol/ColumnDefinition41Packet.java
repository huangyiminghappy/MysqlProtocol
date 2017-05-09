package com.mysql.protocol;

/**
 * 
 * @author dingwei2
 * lenenc_str     catalog
 * lenenc_str     schema
 * lenenc_str     table
 * lenenc_str     org_table
 * lenenc_str     name
 * lenenc_str     org_name
 * lenenc_int     length of fixed-length fields [0c]
 * 2              character set
 * 4              column length
 * 1              type
 * 2              flags
 * 1              decimals
 * 2              filler [00] [00]
 * if command was COM_FIELD_LIST {
 *   lenenc_int     length of default-values
 *   string[$len]   default values
 * }
 *
 */
public class ColumnDefinition41Packet extends Packet {
	/** */
	private String catalog;
	
	private String schema;
	
	/** 逻辑表名，别名，，也就是sql 语句中 as 后面的名称*/
	private String table;
	
	/** 数据库中的表名，俗称物理表名*/
	private String orgTable;
	
	private String name;
	
	private String orgName;
	
	private long fieldsLen; // lenenc_int     length of fixed-length fields [0c]
	
	private int characterSet; // 协议中 占用2字节
	
	private int columnLen;// 协议中 占用4字节
	
	private byte type; // 协议中占1字节,,如果是varchar类型的，话，为显示-3，因为byte的返回为-128 - 127;最高位8位为符合位
	
	private int flags; // 协议中占2字节
	
	private byte decimals; //协议中占1字节
	
	// 接下来两个字节的填充  0x00;
	
	
	//默认值长度
	//private long defaultValueLen;
	
	private String defaultValue;
	
	
	private ColumnDefinition41Packet() {}
	
	/**
	 * 
	 * @param msg
	 * @param seq
	 * @param packetLent
	 * @return
	 */
	public static final ColumnDefinition41Packet newInstance(MysqlMessage msg, boolean comFieldList) {
		msg.skipReadBytes(Packet.HEAD_LENGTH);
		ColumnDefinition41Packet packet = new ColumnDefinition41Packet();
		byte [] data;
		packet.setCatalog( ( data = msg.getStringLengthCode()) == null ? "" :new String(data));
		packet.setSchema(( data = msg.getStringLengthCode()) == null ? "" :new String(data));
		packet.setTable(( data = msg.getStringLengthCode()) == null ? "" :new String(data));
		packet.setOrgTable(( data = msg.getStringLengthCode()) == null ? "" :new String(data));
		packet.setName(( data = msg.getStringLengthCode()) == null ? "" :new String(data));
		packet.setOrgName(( data = msg.getStringLengthCode()) == null ? "" :new String(data));
		packet.setFieldsLen(msg.getBinaryLengthCode());
		packet.setCharacterSet(msg.getUB2());
		packet.setColumnLen(msg.getInt());
		packet.setType(msg.get());
		packet.setFlags(msg.getUB2());
		packet.setDecimals(msg.get());
		msg.skipReadBytes(2);//2个填充值 0x00;
		
		if(comFieldList) {
			packet.setDefaultValue(new String(msg.getStringLengthCode()));
		}
		
		System.out.println(packet);
		return packet;
	}
	

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getOrgTable() {
		return orgTable;
	}

	public void setOrgTable(String orgTable) {
		this.orgTable = orgTable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public long getFieldsLen() {
		return fieldsLen;
	}

	public void setFieldsLen(long fieldsLen) {
		this.fieldsLen = fieldsLen;
	}

	public int getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(int characterSet) {
		this.characterSet = characterSet;
	}

	public int getColumnLen() {
		return columnLen;
	}

	public void setColumnLen(int columnLen) {
		this.columnLen = columnLen;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public byte getDecimals() {
		return decimals;
	}

	public void setDecimals(byte decimals) {
		this.decimals = decimals;
	}

//	public long getDefaultValueLen() {
//		return defaultValueLen;
//	}
//
//	public void setDefaultValueLen(long defaultValueLen) {
//		this.defaultValueLen = defaultValueLen;
//	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n====Column Definition41 Info ====\n")
		  .append("catalog:").append(catalog).append("\n")
		  .append("schema:").append(schema).append("\n")
		  .append("table:").append(table).append("\n")
		  .append("orgTable:").append(orgTable).append("\n")
		  .append("name:").append(name).append("\n")
		  .append("orgName:").append(orgName).append("\n")
		  .append("fieldsLen:").append(fieldsLen).append("\n")
		  .append("characterSet:").append(characterSet).append("\n")
		  .append("columnLen:").append(columnLen).append("\n")
		  .append("type:").append(type).append("\n")
		  .append("flags:").append(Integer.toHexString(flags)).append("\n")
		  .append("decimals:").append(decimals);
		
		return sb.toString();
	}
	
	
	

}
