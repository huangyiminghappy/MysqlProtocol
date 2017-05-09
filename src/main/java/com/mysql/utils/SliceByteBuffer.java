package com.mysql.utils;

import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * 不支持mark
 * @author dingwei2
 *
 */
public class SliceByteBuffer {
	
	ByteBuffer innerBuf;// 10, len = 9
	
	private final int adjust;
	
	private int position;
	
	private int limit;
	
	private int capacity; //等于length
	
	public SliceByteBuffer(ByteBuffer buf, int start, int len) {
		this.innerBuf = buf;
		this.adjust = start;
		this.capacity = len;
	}
	
	/**
     * Returns this buffer's capacity. </p>
     *
     * @return  The capacity of this buffer
     */
    public final int capacity() {
        return capacity;
    }

    /**
     * Returns this buffer's position. </p>
     *
     * @return  The position of this buffer
     */
    public final int position() {
        return position;
    }

    /**
     * Sets this buffer's position.  If the mark is defined and larger than the
     * new position then it is discarded. </p>
     *
     * @param  newPosition
     *         The new position value; must be non-negative
     *         and no larger than the current limit
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newPosition</tt> do not hold
     */
    public final SliceByteBuffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0))
            throw new IllegalArgumentException();
        position = newPosition;
     //   if (mark > position) mark = -1;
        return this;
    }

    /**
     * Returns this buffer's limit. </p>
     *
     * @return  The limit of this buffer
     */
    public final int limit() {
        return limit;
    }

    /**
     * Sets this buffer's limit.  If the position is larger than the new limit
     * then it is set to the new limit.  If the mark is defined and larger than
     * the new limit then it is discarded. </p>
     *
     * @param  newLimit
     *         The new limit value; must be non-negative
     *         and no larger than this buffer's capacity
     *
     * @return  This buffer
     *
     * @throws  IllegalArgumentException
     *          If the preconditions on <tt>newLimit</tt> do not hold
     */
    public final SliceByteBuffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        limit = newLimit;
        if (position > limit) position = limit;
 //       if (mark > limit) mark = -1;
        return this;
    }
	
	

	public byte get() {
		checkBounds(position);
		return this.innerBuf.get( adjust + position ++ );
	}

	public SliceByteBuffer put(byte b) {
		checkBounds(position);
		this.innerBuf.put(adjust + position ++ , b);
		return this;
	}

	public byte get(int index) {
		checkBounds(index);
		return this.innerBuf.get( adjust + index );
	}

	public SliceByteBuffer put(int index, byte b) {
		checkBounds(index);
		this.innerBuf.put(adjust + index, b);
		return this;
	}

	public SliceByteBuffer compact() {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException();
	}
	
	
	public SliceByteBuffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers bytes from this buffer into the given
     * destination array.  An invocation of this method of the form
     * <tt>src.get(a)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     *          If there are fewer than <tt>length</tt> bytes
     *          remaining in this buffer
     */
    public SliceByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

	public boolean isDirect() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	void checkBounds(int index) {
		if( index < 0 ||  index >= limit)
			throw new IndexOutOfBoundsException();
	}
	
	static void checkBounds(int off, int len, int size) { // package-private
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
    }
	
	/**
     * Returns the number of elements between the current position and the
     * limit. </p>
     *
     * @return  The number of elements remaining in this buffer
     */
    public final int remaining() {
        return limit - position;
    }

    /**
     * Tells whether there are any elements between the current position and
     * the limit. </p>
     *
     * @return  <tt>true</tt> if, and only if, there is at least one element
     *          remaining in this buffer
     */
    public final boolean hasRemaining() {
        return position < limit;
    }


}
