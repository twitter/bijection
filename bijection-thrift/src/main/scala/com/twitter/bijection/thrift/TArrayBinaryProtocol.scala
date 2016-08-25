/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.twitter.bijection.thrift

import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer

import org.apache.thrift.protocol._
import org.apache.thrift.TException
import org.apache.thrift.transport.TTransport

/**
  * Binary protocol implementation for thrift.
  *
  */
object TArrayBinaryProtocol {
  private val ANONYMOUS_STRUCT: TStruct = new TStruct()
}

case class TArrayBinaryProtocol(transport: TArrayByteTransport) extends TProtocol(transport) {
  import TArrayBinaryProtocol._

  def readMessageBegin(): TMessage = {
    sys.error("Protocol for serialized structures only")
  }

  def writeBinary(x$1: java.nio.ByteBuffer): Unit = ???
  def writeBool(x$1: Boolean): Unit = ???
  def writeByte(x$1: Byte): Unit = ???
  def writeDouble(x$1: Double): Unit = ???
  def writeFieldBegin(x$1: org.apache.thrift.protocol.TField): Unit = ???
  def writeFieldEnd(): Unit = ???
  def writeFieldStop(): Unit = ???
  def writeI16(x$1: Short): Unit = ???
  def writeI32(x$1: Int): Unit = ???
  def writeI64(x$1: Long): Unit = ???
  def writeListBegin(x$1: org.apache.thrift.protocol.TList): Unit = ???
  def writeListEnd(): Unit = ???
  def writeMapBegin(x$1: org.apache.thrift.protocol.TMap): Unit = ???
  def writeMapEnd(): Unit = ???
  def writeMessageBegin(x$1: org.apache.thrift.protocol.TMessage): Unit = ???
  def writeMessageEnd(): Unit = ???
  def writeSetBegin(x$1: org.apache.thrift.protocol.TSet): Unit = ???
  def writeSetEnd(): Unit = ???
  def writeString(x$1: String): Unit = ???
  def writeStructBegin(x$1: org.apache.thrift.protocol.TStruct): Unit = ???
  def writeStructEnd(): Unit = ???

  def readMessageEnd() {}

  override final def readStructBegin: TStruct = ANONYMOUS_STRUCT

  override final def readStructEnd: Unit = {}

  override final def readFieldBegin: TField = {
    val tpe: Byte = readByte
    val id: Short = if (tpe == TType.STOP) 0 else readI16
    new TField("", tpe, id)
  }

  override final def readFieldEnd(): Unit = {}

  override final def readMapBegin: TMap = new TMap(readByte, readByte, readI32)

  override final def readMapEnd(): Unit = {}

  override final def readListBegin: TList = new TList(readByte, readI32)

  override final def readListEnd(): Unit = {}

  override final def readSetBegin: TSet = new TSet(readByte, readI32)

  override final def readSetEnd(): Unit = {}

  override final def readBool: Boolean = readByte == 1

  @inline
  private[this] final def advance(by: Int) {
    transport.bufferPos = transport.bufferPos + by
  }

  @inline
  def readByte: Byte = {
    val r: Byte = transport.buf(transport.bufferPos)
    advance(1)
    r
  }

  @inline
  def readI32: Int = {
    val off = transport.bufferPos
    advance(4)
    ((transport.buf(off) & 0xff) << 24) |
      ((transport.buf(off + 1) & 0xff) << 16) |
      ((transport.buf(off + 2) & 0xff) << 8) |
      ((transport.buf(off + 3) & 0xff))
  }

  @inline
  def readI16: Short = {
    val off = transport.bufferPos
    advance(2)
    (((transport.buf(off) & 0xff) << 8) | ((transport.buf(off + 1) & 0xff))).toShort
  }

  @inline
  def readI64: Long = {
    val off = transport.bufferPos
    advance(8)
    ((transport.buf(off) & 0xffL) << 56) |
      ((transport.buf(off + 1) & 0xffL) << 48) |
      ((transport.buf(off + 2) & 0xffL) << 40) |
      ((transport.buf(off + 3) & 0xffL) << 32) |
      ((transport.buf(off + 4) & 0xffL) << 24) |
      ((transport.buf(off + 5) & 0xffL) << 16) |
      ((transport.buf(off + 6) & 0xffL) << 8) |
      ((transport.buf(off + 7) & 0xffL))
  }

  def readDouble: Double =
    java.lang.Double.longBitsToDouble(readI64)

  def readString: String =
    try {
      val size = readI32
      val s = new String(transport.buf, transport.bufferPos, size, "UTF-8")
      advance(size)
      s
    } catch {
      case e: UnsupportedEncodingException =>
        throw new TException("JVM DOES NOT SUPPORT UTF-8")
    }

  def readBinary: ByteBuffer = {
    val size = readI32
    val bb = ByteBuffer.wrap(transport.buf, transport.bufferPos, size)
    advance(size)
    bb
  }

}
