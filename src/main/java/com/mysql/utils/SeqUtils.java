package com.mysql.utils;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SeqUtils {
	
	private static final ConcurrentHashMap<SelectableChannel, AtomicInteger> seqHashMap = new ConcurrentHashMap<SelectableChannel, AtomicInteger>();
	
	public static final byte getSeq(SelectableChannel channel) {
		AtomicInteger seq = seqHashMap.get(channel);
		if(seq == null) {
			seq = new AtomicInteger(1);
			seqHashMap.put(channel, seq);
		}
		
		
		
		int s = seq.getAndAdd(1);
		if(s >= 255 ) {
			seq.set(1);
		}
		return (byte)s;
	}
	

}
