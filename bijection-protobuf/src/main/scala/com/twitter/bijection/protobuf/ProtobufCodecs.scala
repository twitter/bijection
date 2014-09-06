package com.twitter.bijection.protobuf

import com.twitter.bijection.{ Bijection, Conversion, Injection, InversionFailure }
import com.twitter.bijection.Inversion.attempt
import com.google.protobuf.Message
import com.google.protobuf.ProtocolMessageEnum
import java.lang.{ Integer => JInt }
import scala.collection.mutable.{ Map => MMap }
import scala.util.{ Failure, Success }
import scala.reflect._

/**
 * Bijections for use in serializing and deserializing Protobufs.
 */
object ProtobufCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  implicit def apply[T <: Message: ClassTag]: Injection[T, Array[Byte]] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    fromClass(klass)
  }

  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def fromClass[T <: Message](klass: Class[T]): Injection[T, Array[Byte]] = new ProtobufCodec[T](klass)
}

class ProtobufCodec[T <: Message](klass: Class[T]) extends Injection[T, Array[Byte]] {
  lazy val parseFrom = klass.getMethod("parseFrom", classOf[Array[Byte]])
  override def apply(item: T) = item.toByteArray
  override def invert(bytes: Array[Byte]) = attempt(bytes) { bytes =>
    parseFrom.invoke(null, bytes).asInstanceOf[T]
  }
}

object ProtobufEnumCodec {
  /**
   * For scala instantiation. Uses reflection.
   */
  implicit def apply[T <: ProtocolMessageEnum: ClassTag]: Injection[T, Int] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    fromClass(klass)
  }
  /**
   * For java instantiation. No reflection, supplied classes only.
   */
  def fromClass[T <: ProtocolMessageEnum](klass: Class[T]): Injection[T, Int] = new ProtobufEnumCodec[T](klass)

  /**
   * Implicit conversions between ProtocolMessageEnum and common types.
   */
  implicit def toBinary[T <: ProtocolMessageEnum: ClassTag]: Injection[T, Array[Byte]] =
    Injection.connect[T, Int, Array[Byte]]
}

class ProtobufEnumCodec[T <: ProtocolMessageEnum](klass: Class[T]) extends Injection[T, Int] {
  import Conversion.asMethod // adds "as" for conversions

  lazy val valueOf = klass.getMethod("valueOf", classOf[Int])
  val cache = MMap[Int, T]()
  override def apply(enum: T) = enum.getNumber
  override def invert(i: Int) = Option {
    cache.getOrElseUpdate(i, valueOf.invoke(null, i.as[JInt]).asInstanceOf[T])
  }.toRight(InversionFailure(i)).fold(Failure(_), Success(_))
}
