package com.mysql.protocol;

import java.io.Serializable;
import java.nio.ByteBuffer;

@SuppressWarnings("serial")
public class Packet implements Serializable {
	
	public static final byte FILLER = 0; //填充值 0x00;
	
	public static final int HEAD_LENGTH = 4;//mysql协议头固定长度为4字节
	
	
	/**
	 * Mysql 服务器权能标识
	 * @see https://dev.mysql.com/doc/internals/en/capability-flags.html#flag-CLIENT_PLUGIN_AUTH
	 */
	/** Use the improved version of Old Password Authentication. */
	public static final int CLIENT_LONG_PASSWORD = 0x00000001;
	
	/** Send found rows instead of affected rows in EOF_Packet. */
	public static final int CLIENT_FOUND_ROWS = 0x00000002;
	
	/** Longer flags in Protocol::ColumnDefinition320. */
	public static final int CLIENT_LONG_FLAG = 0x00000004;
	
	/** Database (schema) name can be specified on connect in Handshake Response Packet. */
	
	
	/**
	 * 
	 * Database (schema) name can be specified on connect in Handshake Response Packet.
	 * Server
	 * 		Supports schema-name in Handshake Response Packet.
	 * 
	 * Client
	 * 		Handshake Response Packet contains a schema-name.
	 */
	public static final int CLIENT_CONNECT_WITH_DB = 0x00000008;
	
	/**
	 * Server
	 * 		Do not permit database.table.column.
	 */
	public static final int CLIENT_NO_SCHEMA = 0x00000010;
	
	/**
	 * Compression protocol supported.
	 * Server
	 * 		Supports compression.
	 * Client
	 * 		Switches to Compression compressed protocol after successful authentication.
	 */
	public static final int CLIENT_COMPRESS = 0x00000020;
	
	/**
	 * Special handling of ODBC behavior.  since 3.22
	 */
	public static final int CLIENT_ODBC = 0x00000040;
	
	
	/**
	 * Can use LOAD DATA LOCAL.
	 * Server
	 * 		Enables the LOCAL INFILE request of LOAD DATA|XML.
	 * Client
	 * 		Will handle LOCAL INFILE request.
	 * 
	 */
	public static final int CLIENT_LOCAL_FILES = 0x00000080;
	
	/**
	 * Server
	 * 		Parser can ignore spaces before '('.
	 * Client
	 * 		Let the parser ignore spaces before '('.
	 */
	public static final int CLIENT_IGNORE_SPACE = 0x00000100;
	
	/**
	 * Server
	 * 		Supports the 4.1 protocol.
	 * Client
	 * 		Uses the 4.1 protocol.
	 */
	public static final int CLIENT_PROTOCOL_41 = 0x00000200;
	
	/**
	 * 支持 wait_timeout versus wait_interactive_timeout.
	 * Server 
	 * 		Supports interactive and noninteractive clients.
	 * Client
	 * 		Client is interactive.
	 * see  mysql_real_connect()
	 * 
	 */
	public static final int CLIENT_INTERACTIVE = 0x00000400;
	
	/**
	 * Server
	 * 		Supports SSL.
	 * Client
	 * 		Switch to SSL after sending the capability-flags.
	 */
	public static final int CLIENT_SSL = 0x00000800;
	
	/**
	 * client
	 * 		Do not issue SIGPIPE if network failures occur (libmysqlclient only).
	 *      如果网络出现故障时，不要发送SIGPIPE信号，什么是SIGPIPE,待查
	 */
	public static final int CLIENT_IGNORE_SIGPIPE = 0x00001000;
	
	/**
	 * 
	 * Server
	 * 		Can send status flags in EOF_Packet.
	 * Client
	 * 		Expects status flags in EOF_Packet.
	 * 
	 * Note
	 * 		This flag is optional in 3.23, but always set by the server since 4.0.
	 */
	public static final int CLIENT_TRANSACTIONS = 0x00002000;
	
	public static final int CLIENT_RESERVED = 0x00004000;// unused
	
	/**
	 * Server
	 * 		Supports Authentication::Native41.
	 * Client
	 * 		Supports Authentication::Native41.
	 */
	public static final int CLIENT_SECURE_CONNECTION = 0x00008000;
	
	/**
	 * Server
	 * 		Can handle multiple statements per COM_QUERY and COM_STMT_PREPARE.
	 * Client
	 * 		May send multiple statements per COM_QUERY and COM_STMT_PREPARE.
	 * 
	 * Note
	 * 		Was named CLIENT_MULTI_QUERIES in 4.1.0, renamed later.
	 * 
	 * Requires
	 * 		CLIENT_PROTOCOL_41
	 */
	public static final int CLIENT_MULTI_STATEMENTS = 0x00010000;
	
	
	/**
	 * Server
	 * 		Can send multiple resultsets for COM_QUERY.
	 * Client
	 * 		Can handle multiple resultsets for COM_QUERY.
	 * Requires
	 * 		CLIENT_PROTOCOL_41
	 */
	public static final int CLIENT_MULTI_RESULTS = 0x00020000;
	
	/**
	 * Server 
	 * 		Can send multiple resultsets for COM_STMT_EXECUTE.
	 * Client
	 * 		Can handle multiple resultsets for COM_STMT_EXECUTE.
	 * Requieres
	 * 		CLIENT_PROTOCOL_41
	 */
	public static final int CLIENT_PS_MULTI_RESULTS = 0x00040000;
	
	
	
	/**
	 * Server
	 * 		Sends extra data in Initial Handshake Packet and supports the pluggable authentication protocol.
	 * Client
	 * 		Supports authentication plugins.
	 * Requires
	 * 		CLIENT_PROTOCOL_41
	 */
	public static final int CLIENT_PLUGIN_AUTH = 0x00080000;//  00001000 00000000 00000000
	
	/**
	 * Server
	 * 		Permits connection attributes in Protocol::HandshakeResponse41.
	 * Client
	 * 		Sends connection attributes in Protocol::HandshakeResponse41.
	 */
	public static final int CLIENT_CONNECT_ATTRS = 0x00100000;
	
	/**
	 * Server
	 * 		Understands length-encoded integer for auth response data in Protocol::HandshakeResponse41.
	 * Client
	 * 		Length of auth response data in Protocol::HandshakeResponse41 is a length-encoded integer.
	 * 		
	 * Note
	 * 		The flag was introduced in 5.6.6, but had the wrong value.
	 * 建议：不要使用该标记
	 *	
	 */
	public static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;
	
	/**
	 * Server
	 * 		Announces support for expired password extension.
	 * Client
	 * 		Can handle expired passwords.
	 */
	public static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = 0x00400000;
	
	/**
	 * Server
	 * 		Can set SERVER_SESSION_STATE_CHANGED in the Status Flags and send session-state change data after a OK packet.
	 * Client
	 * 		Expects the server to send sesson-state changes after a OK packet.
	 */
	public static final int CLIENT_SESSION_TRACK = 0x00800000;
	
	/**
	 * 
	 * Server
	 * 		Can send OK after a Text Resultset.
	 * Client
	 * 		Expects an OK (instead of EOF) after the resultset rows of a Text Resultset.
	 * Background
	 * 		To support CLIENT_SESSION_TRACK, additional information must be sent after all successful commands. 
	 * 		Although the OK packet is extensible, the EOF packet is not 
	 * 		due to the overlap of its bytes with the content of the Text Resultset Row.
	 * 		Therefore, the EOF packet in the Text Resultset is replaced with an OK packet.
	 * 		EOF packets are deprecated as of MySQL 5.7.5.
	 * 
	 * 		要支持CLIENT_SESSION_TRACK，必须在所有成功命令之后发送附加信息。
	 * 		虽然OK数据包是可扩展的，但EOF数据包不是由于其字节与文本结果集行的内容重叠。
	 * 		因此，文本结果集中的EOF数据包将被替换为OK数据包。
	 * 		从MySQL 5.7.5起，EOF数据包已被弃用。
	 * 
	 */
	public static final int CLIENT_DEPRECATE_EOF = 0x01000000;
	
	
	
	/** Msyql 响应包 类型 */
	public static final int RESPONSE_OK_PACKAGE = 1; //OK响应包
	public static final int RESPONSE_ERR_PACKAGE = 4;//ERROR响应包
	
	
	

	/*
	 * 客户端请求命令类型
	 */
	/**
     * none, this is an internal thread state
     */
    public static final byte COM_SLEEP = 0;

    /**
     * mysql_close
     */
    public static final byte COM_QUIT = 1;

    /**
     * mysql_select_db
     */
    public static final byte COM_INIT_DB = 2;

    /**
     * mysql_real_query
     */
    public static final byte COM_QUERY = 3;

    /**
     * mysql_list_fields
     */
    public static final byte COM_FIELD_LIST = 4;

    /**
     * mysql_create_db (deprecated)
     */
    public static final byte COM_CREATE_DB = 5;

    /**
     * mysql_drop_db (deprecated)
     */
    public static final byte COM_DROP_DB = 6;

    /**
     * mysql_refresh
     */
    public static final byte COM_REFRESH = 7;

    /**
     * mysql_shutdown
     */
    public static final byte COM_SHUTDOWN = 8;

    /**
     * mysql_stat
     */
    public static final byte COM_STATISTICS = 9;

    /**
     * mysql_list_processes
     */
    public static final byte COM_PROCESS_INFO = 10;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_CONNECT = 11;

    /**
     * mysql_kill
     */
    public static final byte COM_PROCESS_KILL = 12;

    /**
     * mysql_dump_debug_info
     */
    public static final byte COM_DEBUG = 13;

    /**
     * mysql_ping
     */
    public static final byte COM_PING = 14;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_TIME = 15;

    /**
     * none, this is an internal thread state
     */
    public static final byte COM_DELAYED_INSERT = 16;

    /**
     * mysql_change_user
     */
    public static final byte COM_CHANGE_USER = 17;

    /**
     * used by slave server mysqlbinlog
     */
    public static final byte COM_BINLOG_DUMP = 18;

    /**
     * used by slave server to get master table
     */
    public static final byte COM_TABLE_DUMP = 19;

    /**
     * used by slave to log connection to master
     */
    public static final byte COM_CONNECT_OUT = 20;

    /**
     * used by slave to register to master
     */
    public static final byte COM_REGISTER_SLAVE = 21;

    /**
     * mysql_stmt_prepare
     */
    public static final byte COM_STMT_PREPARE = 22;

    /**
     * mysql_stmt_execute
     */
    public static final byte COM_STMT_EXECUTE = 23;

    /**
     * mysql_stmt_send_long_data
     */
    public static final byte COM_STMT_SEND_LONG_DATA = 24;

    /**
     * mysql_stmt_close
     */
    public static final byte COM_STMT_CLOSE = 25;

    /**
     * mysql_stmt_reset
     */
    public static final byte COM_STMT_RESET = 26;

    /**
     * mysql_set_server_option
     */
    public static final byte COM_SET_OPTION = 27;

    /**
     * mysql_stmt_fetch
     */
    public static final byte COM_STMT_FETCH = 28;

    /**
     * Mycat heartbeat
     */
    public static final byte COM_HEARTBEAT = 64;
    
    
    /** 查询类型  select  */
    public static final int QUERY_TYPE_SELECT = 1;
    
    
    
    /** 服务器状态  Status Flags   start
     * 
     * @see https://dev.mysql.com/doc/internals/en/status-flags.html
     * 
     * 
     */
    public static final int SERVER_STATUS_IN_TRANS = 0x0001;
    
    
    public static final int SERVER_STATUS_AUTOCOMMIT = 0x0002;
    
    /**
     * 
     */
    public static final int SERVER_MORE_RESULTS_EXISTS = 0x0008;
    
    // 后续请参考上述连接，目前没有显示全部状态
	
    /** 服务器状态  Status Flags   end */
	
}
