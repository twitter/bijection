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

  implicit def toBinary[T <: TBase[_,_]: Manifest]: Bijection[T, Array[Byte]] = BinaryThriftCodec[T]
  def toCompact[T <: TBase[_,_]: Manifest]: Bijection[T, Array[Byte]] = CompactThriftCodec[T]
  def toJson[T <: TBase[_,_]: Manifest]: Bijection[T, String] = JsonThriftCodec[T]
}

class ThriftCodec[T <: TBase[_, _], P <: TProtocolFactory](klass: Class[T], factory: P)
extends Bijection[T, Array[Byte]] {
  protected lazy val prototype = klass.newInstance
  override def apply(item: T) = {
    val baos = new ByteArrayOutputStream
    item.write(factory.getProtocol(new TIOStreamTransport(baos)))
    baos.toByteArray
  }
  override def invert(bytes: Array[Byte]) = {
    val obj = prototype.deepCopy
    val stream = new ByteArrayInputStream(bytes)
    obj.read(factory.getProtocol(new TIOStreamTransport(stream)))
    obj.asInstanceOf[T]
  }
}

object BinaryThriftCodec {
  def apply[T <: TBase[_, _]: Manifest]: Bijection[T, Array[Byte]] = fromClass(manifest[T].erasure.asInstanceOf[Class[T]])
  def fromClass[T <: TBase[_, _]](klass: Class[T]): Bijection[T, Array[Byte]] = new BinaryThriftCodec(klass)
}

class BinaryThriftCodec[T <: TBase[_, _]](klass: Class[T])
extends ThriftCodec[T, TBinaryProtocol.Factory](klass, new TBinaryProtocol.Factory)

object CompactThriftCodec {
  def apply[T <: TBase[_, _]: Manifest]: Bijection[T, Array[Byte]] = fromClass(manifest[T].erasure.asInstanceOf[Class[T]])
  def fromClass[T <: TBase[_, _]](klass: Class[T]): Bijection[T, Array[Byte]] = new CompactThriftCodec(klass)
}

class CompactThriftCodec[T <: TBase[_, _]](klass: Class[T])
extends ThriftCodec[T, TCompactProtocol.Factory](klass, new TCompactProtocol.Factory)

object JsonThriftCodec {
  def apply[T <: TBase[_, _]: Manifest]: Bijection[T, String] = fromClass(manifest[T].erasure.asInstanceOf[Class[T]])
  def fromClass[T <: TBase[_, _]](klass: Class[T]): Bijection[T, String] = new JsonThriftCodec[T](klass) andThen Bijection.utf8.inverse
}

class JsonThriftCodec[T <: TBase[_, _]](klass: Class[T])
extends ThriftCodec[T, TSimpleJSONProtocol.Factory](klass, new TSimpleJSONProtocol.Factory) {
  override def invert(bytes: Array[Byte]) =
    new MappingJsonFactory()
      .createJsonParser(bytes)
      .readValueAs(klass)
      .asInstanceOf[T]
}
