package com.twitter.bijection.protobuf

import com.twitter.bijection.Bijection
import com.google.protobuf.Message
import com.google.protobuf.ProtocolMessageEnum
import java.lang.{ Integer => JInt }
import scala.collection.mutable.{ Map =>MMap }

/**
 * Bijections for use in serializing and deserializing Protobufs.
 */
object ProtobufCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  implicit def apply[T <: Message: Manifest]: Bijection[T, Array[Byte]] = {
    val klass = manifest[T].erasure.asInstanceOf[Class[T]]
    fromClass(klass)
  }

  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def fromClass[T <: Message](klass: Class[T]): Bijection[T, Array[Byte]] = new ProtobufCodec[T](klass)
}

class ProtobufCodec[T <: Message](klass: Class[T]) extends Bijection[T, Array[Byte]] {
  lazy val parseFrom = klass.getMethod("parseFrom", classOf[Array[Byte]])
  override def apply(item: T) = item.toByteArray
  override def invert(bytes: Array[Byte]) = parseFrom.invoke(null, bytes).asInstanceOf[T]
}

object ProtobufEnumCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  implicit def apply[T <: ProtocolMessageEnum: Manifest]: Bijection[T, Int] = {
    val klass = manifest[T].erasure.asInstanceOf[Class[T]]
    fromClass(klass)
  }
  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def fromClass[T <: ProtocolMessageEnum](klass: Class[T]): Bijection[T, Int] = new ProtobufEnumCodec[T](klass)

  /**
   * Implicit conversions between ProtocolMessageEnum and common types.
   */
  implicit def toBinary[T <: ProtocolMessageEnum: Manifest]: Bijection[T, Array[Byte]] = Bijection.connect[T, Int, Array[Byte]]
}

class ProtobufEnumCodec[T <: ProtocolMessageEnum](klass: Class[T]) extends Bijection[T, Int] {
  import Bijection.asMethod // adds "as" for conversions

  lazy val valueOf = klass.getMethod("valueOf", classOf[Int])
  val cache = MMap[Int,T]()
  override def apply(enum: T) = enum.getNumber
  override def invert(i: Int) =
    cache.getOrElseUpdate(i, valueOf.invoke(null, i.as[JInt]).asInstanceOf[T])
}
