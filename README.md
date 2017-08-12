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
2、Mysql 握手验证协议
客户端首先发起TCP连接，连接服务端，TCP经过三次握手协议之后，建立可靠的传输通道。
1）成功建立TCP之后，首先由Mysql服务器发送一个握手包，包括协议版本号、服务器版本号，服务器授权认证信息、服务器权能标识等等。
2）客户端收到握手包后向服务端发送登录验证报文，主要包括用户名，数据库名，密码（密文）
3） 服务器向客户端发送认证结果报文（OK Package或 Error Package）或其他响应结果。

## 2. Mysql通信协议基础：
#### 1、报文结构
mysql通信报文分为 消息头与消息体两部分。
其中消息头固定4个字节，前个字节为消息体的长度，1个处理序列号。
#### 2、Mysql 基本类型
##### 2.1 整数值
Mysql报文中整数值分别有 1、2、3、4、8字节长度，使用小端序列传输。（接收方先接收到 整数的低位部分）
##### 2.2 字符串(以Null结尾 0x00)(Null-Terminated String)
字符串长度不固定，当遇到'NULL'(0x00) 字符时结束。
##### 2.3 二进制数据(长度编码)(Length Coded Binary)
数据长度不固定，字节数由第一个字节决定。
第一个字节值	后续字节数	长度值说明
0-250	0	第一个字节值即为数据的真实长度
251	0	空数据，数据的真实长度为零
252	2	后续额外2个字节标识了数据的真实长度
253	3	后续额外3个字节标识了数据的真实长度
254	8	后续额外8个字节标识了数据的真实长度
##### 2.4 字符串（长度编码）(Length Coded String)
字符串长度不固定，无'NULL'(0x00)的介绍符，编码方式与上面的Length Code Binary。</br>
#### 3、协议描述mysql通信协议描述</br>
### Type	Description</br>
int<1>	1 byte Protocol::FixedLengthInteger </br>
int<2>	2 byte Protocol::FixedLengthInteger </br>
int<3>	3 byte Protocol::FixedLengthInteger </br>
int<4>	4 byte Protocol::FixedLengthInteger </br>
int<6>	6 byte Protocol::FixedLengthInteger </br>
int<8>	8 byte Protocol::FixedLengthInteger </br>
int<lenenc>	Protocol::LengthEncodedInteger </br>
string<lenenc>	Protocol::LengthEncodedString </br>
string<fix>	Protocol::FixedLengthString </br>
string<var>	Protocol::VariableLengthString: </br>
string<EOF>	Protocol::RestOfPacketString </br>
string<NUL>	Protocol::NulTerminatedString </br>

