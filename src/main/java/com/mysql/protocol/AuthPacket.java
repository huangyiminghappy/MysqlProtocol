package com.mysql.protocol;

/**
 * 
 * 
 * 
 * 
 * 
 * 2              capability flags, CLIENT_PROTOCOL_41 never set
 * 3              max-packet size
 * string[NUL]    username
 * if capabilities & CLIENT_CONNECT_WITH_DB {
 * string[NUL]    auth-response
 * string[NUL]    database
 * } else {
 * string[EOF]    auth-response
 * }
 *
 */
public class AuthPacket extends Packet {

}
