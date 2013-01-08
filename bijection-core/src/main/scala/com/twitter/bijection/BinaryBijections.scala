/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.twitter.bijection

import java.util.zip.{ GZIPInputStream, GZIPOutputStream }
import java.nio.ByteBuffer
import org.apache.commons.codec.binary.Base64
import java.io.{OutputStream, InputStream, ByteArrayInputStream, ByteArrayOutputStream}
import annotation.tailrec

// TODO: Convert to value classes on Scala 2.10 upgrade.
case class GZippedBytes(bytes: Array[Byte])

object GZippedBytes {
  val unwrap: Bijection[GZippedBytes, Array[Byte]] =
    new Bijection[GZippedBytes, Array[Byte]] {
      override def apply(gzb: GZippedBytes) = gzb.bytes
      override def invert(bytes: Array[Byte]) = GZippedBytes(bytes)
    }
}

case class GZippedBase64String(str: String)

object GZippedBase64String {
  val unwrap: Bijection[GZippedBase64String, String] =
    new Bijection[GZippedBase64String, String] {
      override def apply(gzbs: GZippedBase64String) = gzbs.str
      override def invert(str: String) = GZippedBase64String(str)
    }
}

case class Base64String(str: String)

object Base64String {
  val unwrap: Bijection[Base64String, String] =
    new Bijection[Base64String, String] {
      override def apply(bs: Base64String) = bs.str
      override def invert(str: String) = Base64String(str)
    }
}

/**
 * A collection of utilities for encoding strings and byte arrays to
 * and decoding from strings compressed from with gzip.
 *
 * This object is thread-safe because there are no streams shared
 * outside of method scope, and therefore no contention for shared byte arrays.
 */
trait BinaryBijections {
  /**
   * Bijection between byte array and java.nio.ByteBuffer.
   */
  implicit val bytes2Buffer: Bijection[Array[Byte], ByteBuffer] =
    new Bijection[Array[Byte], ByteBuffer] {
      def apply(b: Array[Byte]) = ByteBuffer.wrap(b)
      override def invert(byteBuffer: ByteBuffer) = {
        val buf = byteBuffer.duplicate()
        val ret = Array.ofDim[Byte](buf.remaining())
        buf.get(ret)
        ret
      }
    }

  @tailrec
  private def copy(inputStream:  InputStream, outputStream: OutputStream, bufferSize: Int = 1024) {
    val buf = new Array[Byte](bufferSize)
    inputStream.read(buf, 0, buf.size) match {
      case -1 => ()
      case n =>
        outputStream.write(buf, 0, n)
        copy(inputStream, outputStream, bufferSize)
    }
  }
  /**
   * Bijection between byte array and GZippedBytes.
   */
  implicit val bytes2GzippedBytes: Bijection[Array[Byte], GZippedBytes] =
    new Bijection[Array[Byte], GZippedBytes] {
      def apply(bytes: Array[Byte]) = {
        val baos = new ByteArrayOutputStream
        val gos = new GZIPOutputStream(baos)
        gos.write(bytes)
        gos.finish()
        GZippedBytes(baos.toByteArray)
      }
      override def invert(gz: GZippedBytes) = {
        val baos = new ByteArrayOutputStream
        copy(new GZIPInputStream(new ByteArrayInputStream(gz.bytes)), baos)
        baos.toByteArray
      }
    }

  /**
   * Bijection between byte array and Base64 encoded string.
   *
   * The "trim" here is important, as encodeBase64String sometimes
   * tags a newline on the end of its encoding. DON'T REMOVE THIS
   * CALL TO TRIM.
   */
  implicit val bytes2Base64: Bijection[Array[Byte], Base64String] =
    new Bijection[Array[Byte], Base64String] {
      def apply(bytes: Array[Byte]) = Base64String(Base64.encodeBase64String(bytes).trim)
      override def invert(b64: Base64String) = Base64.decodeBase64(b64.str)
    }

  implicit val bytes2GZippedBase64: Bijection[Array[Byte], GZippedBase64String] =
    bytes2GzippedBytes
      .andThen(GZippedBytes.unwrap)
      .andThen(bytes2Base64)
      .andThen(Base64String.unwrap)
      .andThen(GZippedBase64String.unwrap.inverse)
}
