package com.twitter.bijection.thrift

import com.twitter.bijection.{ Bijection, Conversion, Injection, InversionFailure, StringCodec }
import com.twitter.bijection.Inversion.attempt
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import org.apache.thrift.{ TBase, TEnum }
import org.apache.thrift.protocol.{
  TBinaryProtocol,
  TCompactProtocol,
  TProtocolFactory,
  TSimpleJSONProtocol
}
import org.apache.thrift.transport.TIOStreamTransport
import org.codehaus.jackson.map.MappingJsonFactory
import java.lang.{ Integer => JInt }
import scala.collection.mutable.{ Map => MMap }
import scala.util.{ Failure, Success }
import scala.reflect._

/**
 * Codecs for use in serializing and deserializing Thrift structures.
 */
object ThriftCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  def apply[T <: TBase[_, _]: ClassTag, P <: TProtocolFactory: ClassTag]: Injection[T, Array[Byte]] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    val factory = classTag[P].runtimeClass.asInstanceOf[Class[P]].newInstance
    apply(klass, factory)
  }

  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def apply[T <: TBase[_, _], P <: TProtocolFactory](klass: Class[T], factory: P): Injection[T, Array[Byte]] =
    new ThriftCodec[T, P](klass, factory)

  implicit def toBinary[T <: TBase[_, _]: ClassTag]: Injection[T, Array[Byte]] = BinaryThriftCodec[T]
  def toCompact[T <: TBase[_, _]: ClassTag]: Injection[T, Array[Byte]] = CompactThriftCodec[T]
  def toJson[T <: TBase[_, _]: ClassTag]: Injection[T, String] = JsonThriftCodec[T]
}

class ThriftCodec[T <: TBase[_, _], P <: TProtocolFactory](klass: Class[T], factory: P)
  extends Injection[T, Array[Byte]] {
  protected lazy val prototype = klass.newInstance
  override def apply(item: T) = {
    val baos = new ByteArrayOutputStream
    item.write(factory.getProtocol(new TIOStreamTransport(baos)))
    baos.toByteArray
  }
  override def invert(bytes: Array[Byte]) = attempt(bytes) { bytes =>
    val obj = prototype.deepCopy
    val stream = new ByteArrayInputStream(bytes)
    obj.read(factory.getProtocol(new TIOStreamTransport(stream)))
    obj.asInstanceOf[T]
  }
}

object BinaryThriftCodec {
  def apply[T <: TBase[_, _]: ClassTag]: Injection[T, Array[Byte]] = fromClass(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  def fromClass[T <: TBase[_, _]](klass: Class[T]): Injection[T, Array[Byte]] = new BinaryThriftCodec(klass)
}

class BinaryThriftCodec[T <: TBase[_, _]](klass: Class[T])
  extends ThriftCodec[T, TBinaryProtocol.Factory](klass, new TBinaryProtocol.Factory)

object CompactThriftCodec {
  def apply[T <: TBase[_, _]: ClassTag]: Injection[T, Array[Byte]] = fromClass(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  def fromClass[T <: TBase[_, _]](klass: Class[T]): Injection[T, Array[Byte]] = new CompactThriftCodec(klass)
}

class CompactThriftCodec[T <: TBase[_, _]](klass: Class[T])
  extends ThriftCodec[T, TCompactProtocol.Factory](klass, new TCompactProtocol.Factory)

object JsonThriftCodec {
  def apply[T <: TBase[_, _]: ClassTag]: Injection[T, String] = fromClass(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  def fromClass[T <: TBase[_, _]](klass: Class[T]): Injection[T, String] =
    // This is not really unsafe because we know JsonThriftCodec gives utf8 bytes as output
    (new JsonThriftCodec[T](klass))
      .andThen(Injection.unsafeToBijection(StringCodec.utf8).inverse: Bijection[Array[Byte], String])
}

class JsonThriftCodec[T <: TBase[_, _]](klass: Class[T])
  extends ThriftCodec[T, TSimpleJSONProtocol.Factory](klass, new TSimpleJSONProtocol.Factory) {
  override def invert(bytes: Array[Byte]) = attempt(bytes) { bytes =>
    new MappingJsonFactory()
      .createJsonParser(bytes)
      .readValueAs(klass)
      .asInstanceOf[T]
  }
}

object TEnumCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  implicit def apply[T <: TEnum: ClassTag]: Injection[T, Int] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    fromClass(klass)
  }
  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def fromClass[T <: TEnum](klass: Class[T]): Injection[T, Int] =
    new TEnumCodec[T](klass)

  /**
   * Implicit conversions between TEnum and common types.
   */
  implicit def toBinary[T <: TEnum: ClassTag]: Injection[T, Array[Byte]] =
    Injection.connect[T, Int, Array[Byte]]
}

class TEnumCodec[T <: TEnum](klass: Class[T]) extends Injection[T, Int] {
  import Conversion.asMethod // adds "as" for conversions

  lazy val findByValue = klass.getMethod("findByValue", classOf[Int])
  val cache = MMap[Int, T]()
  override def apply(enum: T) = enum.getValue
  override def invert(i: Int) = Option {
    cache.getOrElseUpdate(i, findByValue.invoke(null, i.as[JInt]).asInstanceOf[T])
  }.toRight(InversionFailure(i)).fold(Failure(_), Success(_))
}
