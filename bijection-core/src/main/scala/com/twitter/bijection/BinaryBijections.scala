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

import com.twitter.bijection.Inversion.attemptWhen
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.nio.ByteBuffer
// copied java code from Apache commons 1.7
import com.twitter.bijection.codec.Base64
import java.io.{OutputStream, InputStream, ByteArrayInputStream, ByteArrayOutputStream}
import annotation.tailrec

case class GZippedBytes(bytes: Array[Byte]) extends AnyVal {
  override def toString = bytes.mkString("GZippedBytes(", ", ", ")")
}

case class GZippedBase64String(str: String) extends AnyVal

object GZippedBase64String {
  implicit val unwrap: Injection[GZippedBase64String, String] =
    new AbstractInjection[GZippedBase64String, String] {
      override def apply(gzbs: GZippedBase64String) = gzbs.str
      override def invert(str: String) = attemptWhen(str)(Base64.isBase64)(GZippedBase64String(_))
    }
}

case class Base64String(str: String) extends AnyVal

object Base64String {
  implicit val unwrap: Injection[Base64String, String] =
    new AbstractInjection[Base64String, String] {
      override def apply(bs: Base64String) = bs.str
      override def invert(str: String) = attemptWhen(str)(Base64.isBase64)(Base64String(_))
    }
}

/**
  * A collection of utilities for encoding strings and byte arrays to
  * and decoding from strings compressed from with gzip.
  *
  * This object is thread-safe because there are no streams shared
  * outside of method scope, and therefore no contention for shared byte arrays.
  */
trait BinaryBijections extends StringBijections {

  /**
    * Bijection between byte array and java.nio.ByteBuffer.
    */
  implicit val bytes2Buffer: Bijection[Array[Byte], ByteBuffer] =
    new AbstractBijection[Array[Byte], ByteBuffer] {
      def apply(b: Array[Byte]) = ByteBuffer.wrap(b)
      override def invert(byteBuffer: ByteBuffer) = {
        val buf = byteBuffer.duplicate()
        val ret = Array.ofDim[Byte](buf.remaining())
        buf.get(ret)
        ret
      }
    }

  @tailrec
  private def copy(
      inputStream: InputStream,
      outputStream: OutputStream,
      bufferSize: Int = 1024
  ): Unit = {
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
  implicit lazy val bytes2GzippedBytes: Bijection[Array[Byte], GZippedBytes] =
    new AbstractBijection[Array[Byte], GZippedBytes] {
      def apply(bytes: Array[Byte]) = {
        val baos = new ByteArrayOutputStream
        val gos = new GZIPOutputStream(baos)
        gos.write(bytes)
        gos.close()
        GZippedBytes(baos.toByteArray)
      }
      override def invert(gz: GZippedBytes) = {
        val baos = new ByteArrayOutputStream
        val is = new GZIPInputStream(new ByteArrayInputStream(gz.bytes))
        copy(is, baos)
        is.close()
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
  implicit lazy val bytes2Base64: Bijection[Array[Byte], Base64String] =
    new AbstractBijection[Array[Byte], Base64String] {
      def apply(bytes: Array[Byte]) = Base64String(Base64.encodeBase64String(bytes).trim)
      override def invert(b64: Base64String) = Base64.decodeBase64(b64.str)
    }

  implicit val bytes2GZippedBase64: Bijection[Array[Byte], GZippedBase64String] =
    new AbstractBijection[Array[Byte], GZippedBase64String] {
      override def apply(bytes: Array[Byte]) = {
        val gzipped = bytes2GzippedBytes(bytes).bytes
        val b64z = bytes2Base64(gzipped).str
        GZippedBase64String(b64z)
      }
      override def invert(b64z: GZippedBase64String) = {
        val gzipped = bytes2Base64.invert(Base64String(b64z.str))
        bytes2GzippedBytes.invert(GZippedBytes(gzipped))
      }
    }
}
