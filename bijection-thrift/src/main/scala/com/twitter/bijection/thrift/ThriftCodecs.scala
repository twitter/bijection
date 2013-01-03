package com.twitter.bijection.thrift

import com.twitter.bijection.Bijection
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import org.apache.thrift.TBase
import org.apache.thrift.protocol.{
  TBinaryProtocol,
  TCompactProtocol,
  TProtocolFactory,
  TSimpleJSONProtocol
}
import org.apache.thrift.transport.TIOStreamTransport
import org.codehaus.jackson.map.MappingJsonFactory

/**
 * Codecs for use in serializing and deserializing Thrift structures.
 */
object ThriftCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  def apply[T <: TBase[_, _]: Manifest, P <: TProtocolFactory: Manifest]: Bijection[T, Array[Byte]] = {
    val klass = manifest[T].erasure.asInstanceOf[Class[T]]
    val factory = manifest[P].erasure.asInstanceOf[Class[P]].newInstance
    apply(klass, factory)
  }

  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def apply[T <: TBase[_, _], P <: TProtocolFactory](klass: Class[T], factory: P): Bijection[T, Array[Byte]] =
    new ThriftCodec[T,P](klass, factory)
}

class ThriftCodec[T <: TBase[_, _], P <: TProtocolFactory](klass: Class[T], factory: P)
extends Bijection[T, Array[Byte]] {
  protected lazy val prototype = klass.newInstance

  override def apply(item: T) = {
    val baos = new ByteArrayOutputStream
    item.write(factory.getProtocol(new TIOStreamTransport(baos)))
    baos.toByteArray
  }

  override val inverse = Bijection.build[Array[Byte], T] { bytes =>
    val obj = prototype.deepCopy
    val stream = new ByteArrayInputStream(bytes)
    obj.read(factory.getProtocol(new TIOStreamTransport(stream)))
    obj.asInstanceOf[T]
  } { this.apply(_) }
}

object BinaryThriftCodec {
  def apply[T <: TBase[_, _]: Manifest]: BinaryThriftCodec[T] = apply(manifest[T].erasure.asInstanceOf[Class[T]])
  def apply[T <: TBase[_, _]](klass: Class[T]): BinaryThriftCodec[T] = new BinaryThriftCodec(klass)
}

class BinaryThriftCodec[T <: TBase[_, _]](klass: Class[T])
extends ThriftCodec[T, TBinaryProtocol.Factory](klass, new TBinaryProtocol.Factory)

object CompactThriftCodec {
  def apply[T <: TBase[_, _]: Manifest]: CompactThriftCodec[T] = apply(manifest[T].erasure.asInstanceOf[Class[T]])
  def apply[T <: TBase[_, _]](klass: Class[T]): CompactThriftCodec[T] = new CompactThriftCodec(klass)
}

class CompactThriftCodec[T <: TBase[_, _]](klass: Class[T])
extends ThriftCodec[T, TCompactProtocol.Factory](klass, new TCompactProtocol.Factory)

object JsonThriftCodec {
  def apply[T <: TBase[_, _]: Manifest]: JsonThriftCodec[T] = apply(manifest[T].erasure.asInstanceOf[Class[T]])
  def apply[T <: TBase[_, _]](klass: Class[T]): JsonThriftCodec[T] = new JsonThriftCodec[T](klass)
}

class JsonThriftCodec[T <: TBase[_, _]](klass: Class[T])
extends ThriftCodec[T, TSimpleJSONProtocol.Factory](klass, new TSimpleJSONProtocol.Factory) {
  override val inverse = Bijection.build[Array[Byte], T] { bytes =>
    new MappingJsonFactory()
      .createJsonParser(bytes)
      .readValueAs(klass)
      .asInstanceOf[T]
  } { this.apply(_) }
}
