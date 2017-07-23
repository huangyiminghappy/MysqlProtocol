# MysqlProtocol
Using Java NIO to analyze MySQL protocol. and query databases,show the detail datas which return back to the client.
虽然想用英文介绍，发现还是中文介绍更懂点。
## 1.大小端
理解mysql协议一个很重要的内容就是理解大小端。
mysql 通信协议使用小端序列进行传输。
大端序列与小端序列：
1.小端法(Little-Endian)就是低位字节排放在内存的低地址端即该值的起始地址，高位字节排放在内存的高地址端。 
2.大端法(Big-Endian)就是高位字节排放在内存的低地址端即该值的起始地址，低位字节排放在内存的高地址端。
通俗的讲，小端法，接收方先接收到整数的低位部分。大端法，接收方先接收到正式的高位部分。
比如我们经过网络发送0x12345678这个整形，在80X86平台中，它是以小端法存放的，在发送前需要使用系统提供的htonl将其转换成大端法存放。
欢迎fork and star.

