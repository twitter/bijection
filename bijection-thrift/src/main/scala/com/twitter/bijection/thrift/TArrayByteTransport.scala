package com.twitter.bijection.thrift

import org.apache.thrift.protocol._
import org.apache.thrift.TException
import org.apache.thrift.transport.TTransport

case class TArrayByteTransport(buf: Array[Byte]) extends TTransport {
  private[thrift] final var bufferPos = 0
  private[this] final val bufferSiz_ = buf.size

  @inline
  final def bufferSiz = bufferSiz_

  override final def isOpen: Boolean = bufferPos < bufferSiz_

  override final def open() {}

  override final def close() {}

  override final def readAll(destBuf: Array[Byte], off: Int, len: Int): Int =
    read(destBuf, off, len)

  override final def read(destBuf: Array[Byte], destOffset: Int, len: Int): Int = {
    System.arraycopy(buf, bufferPos, destBuf, destOffset, len)
    bufferPos = bufferPos + len
    len
  }

  override final def getBufferPosition: Int = bufferPos
  override final def getBytesRemainingInBuffer: Int = bufferSiz_ - bufferPos
  override final def getBuffer: Array[Byte] = buf

  override final def write(buf: Array[Byte], off: Int, len: Int): Unit = {
    sys.error("not implemented")
  }

  override final def consumeBuffer(len: Int): Unit = {
    bufferPos = bufferPos + len
  }
}
