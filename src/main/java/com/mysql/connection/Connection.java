package com.mysql.connection;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public class Connection implements Runnable {
	
	private Integer connectionId;
	
	private String host;
	private int port;

	/** 底层socket通道 */
	private SocketChannel channel;

	/** 该连接是否通过认证 */
	private boolean auth = false;
	
	/** 是否解析了握手验证包 */
	private boolean handshake = false;

	private AtomicInteger seq = new AtomicInteger(0);
	
	//读缓存区，（累积缓存区），如果数据包不是一个完整的包，需要等待更多的数据到达。
	private ByteBuffer readerBuffer;
	
	/** 当前执行的命令*/
	private int cmd = 0;
	
	/** 命令执行时解析的包 ,由于需要支持 多ResultSet 包，故将该数据结构扩展为List*/
	private List cmdData;
	
	private boolean running = true;
	
	private IOHandler ioHandler;
	
	public Connection(String host, int port) {
		// TODO Auto-generated constructor stub
		this.host = host;
		this.port = port;
		ioHandler = new CmdHandler();
	}
	
	public void close() {
		this.running = false;
	}
	
 
	public void run() {
		// TODO Auto-generated method stub
		Selector selector = null;
		try {
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_CONNECT);

			channel.connect(new InetSocketAddress(host, port));

			Set<SelectionKey> selOps = null;

			while (running) {
				int n = selector.select();
				selOps = selector.selectedKeys();
				if (selOps == null || selOps.isEmpty()) {
					continue;
				}

				try {
					for (Iterator<SelectionKey> it = selOps.iterator(); it.hasNext();) {
						SelectionKey key = it.next();
						if (!key.isValid()) {
							key.cancel();
						}

						if (key.isReadable()) { // 可读
							System.out.println("读事件触发");
							SocketWR.doRead(key, this);

						} else if (key.isWritable()) { // 可写
							System.out.println("写事件触发");
							SocketWR.doWrite(key, this);
							

						} else if (key.isConnectable()) {
							if (channel.isConnectionPending()) {
								channel.finishConnect();
								System.out.println("完成tcp连接");
							}
							channel.register(selector, SelectionKey.OP_READ);
						}

						it.remove();

					}
				} catch (Throwable e) {
					e.printStackTrace();
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			System.out.println("链路关闭");
		}
	}

	public void endCmd() {
		this.cmd = 0;
		//this.cmdData = null;
	}
	
	public final byte getSeq() {
		int s = seq.getAndAdd(1);
		if (s >= 255) {
			synchronized (this) {
				if (s >= 255) {
					seq.set(1);
				}
			}
		}
		return (byte) s;
	}

	public SelectableChannel channel() {
		return this.channel;
	}

	public boolean isAuth() {
		return this.auth;
	}

	public boolean isHandshake() {
		return handshake;
	}

	public void setHandshake(boolean handshake) {
		this.handshake = handshake;
	}

	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	public ByteBuffer getReaderBuffer() {
		return readerBuffer;
	}

	public void setReaderBuffer(ByteBuffer readerBuffer) {
		this.readerBuffer = readerBuffer;
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) { //设置一个新的命令，同时清空上次命令的结果信息
		this.cmd = cmd;
		this.cmdData = null;
	}

	

	public List getCmdData() {
		return cmdData;
	}

	public void setCmdData(List cmdData) {
		this.cmdData = cmdData;
	}

	public IOHandler getIoHandler() {
		return ioHandler;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public Integer getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(Integer connectionId) {
		this.connectionId = connectionId;
	}
	
}
