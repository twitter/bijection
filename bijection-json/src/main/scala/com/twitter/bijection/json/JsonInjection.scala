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

package com.twitter.bijection.json

import com.twitter.bijection.{Bijection, Injection, InversionFailure}
import com.twitter.bijection.Inversion.{attempt, attemptWhen}
import org.codehaus.jackson.{JsonNode, JsonFactory}
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.JsonNodeFactory

import scala.collection.JavaConverters._
import scala.util.{Success, Try}
import scala.util.control.NonFatal

/**
  *  @author Oscar Boykin
  *  @author Sam Ritchie
  *
  * Bijection for converting between Scala basic collections and
  * types and JSON objects.
  */
trait JsonNodeInjection[T] extends Injection[T, JsonNode]

abstract class AbstractJsonNodeInjection[T] extends JsonNodeInjection[T]

trait LowPriorityJson {
  def viaInjection[A, B](
      implicit inj: Injection[A, B],
      json: JsonNodeInjection[B]
  ): JsonNodeInjection[A] =
    new AbstractJsonNodeInjection[A] {
      def apply(a: A) = json(inj(a))
      def invert(j: JsonNode) = json.invert(j).flatMap { inj.invert(_) }
    }
  def viaBijection[A, B](
      implicit bij: Bijection[A, B],
      json: JsonNodeInjection[B]
  ): JsonNodeInjection[A] =
    new AbstractJsonNodeInjection[A] {
      def apply(a: A) = json(bij(a))
      def invert(j: JsonNode) = json.invert(j).map { bij.invert(_) }
    }
  // To get the tuple conversions, low priority because List[T] <: Product
  implicit def tuple[T <: Product](
      implicit inj: Injection[T, List[JsonNode]],
      ltoJ: Injection[List[JsonNode], JsonNode]
  ): JsonNodeInjection[T] =
    new AbstractJsonNodeInjection[T] {
      def apply(t: T) = ltoJ.apply(inj(t))
      def invert(j: JsonNode) = ltoJ.invert(j).flatMap { l =>
        inj.invert(l)
      }
    }
}

/**
  * You need to import all the methods of this object to get general
  * Injection[T,JsonNode] to work
  */
object JsonNodeInjection extends CollectionJson with LowPriorityJson with java.io.Serializable {

  def toJsonNode[T](t: T)(implicit json: JsonNodeInjection[T]): JsonNode =
    json.apply(t)

  def fromJsonNode[T](node: JsonNode)(implicit json: JsonNodeInjection[T]): Try[T] =
    json.invert(node)

  implicit val identity = new AbstractJsonNodeInjection[JsonNode] {
    def apply(n: JsonNode) = n
    override def invert(n: JsonNode) = Success(n)
  }
  implicit val booleanJson = new AbstractJsonNodeInjection[Boolean] {
    def apply(b: Boolean) = JsonNodeFactory.instance.booleanNode(b)
    override def invert(n: JsonNode) = attemptWhen(n)(_.isBoolean)(_.asBoolean)
  }
  implicit val shortJson = new AbstractJsonNodeInjection[Short] {
    def apply(i: Short) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = attemptWhen(n)(_.isInt)(_.asInt.toShort)
  }
  implicit val intJson = new AbstractJsonNodeInjection[Int] {
    def apply(i: Int) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) =
      if (n.isInt) Success(n.asInt) else InversionFailure.failedAttempt(n)
  }
  implicit val longJson = new AbstractJsonNodeInjection[Long] {
    def apply(i: Long) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) =
      if (n.isLong || n.isInt) Success(n.asLong) else InversionFailure.failedAttempt(n)
  }
  implicit val floatJson = new AbstractJsonNodeInjection[Float] {
    def apply(i: Float) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = attempt(n)(_.asDouble.toFloat)
  }
  implicit val doubleJson = new AbstractJsonNodeInjection[Double] {
    def apply(i: Double) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) =
      if (n.isDouble) Success(n.asDouble) else InversionFailure.failedAttempt(n)
  }
  implicit val stringJson = new AbstractJsonNodeInjection[String] {
    def apply(s: String) = JsonNodeFactory.instance.textNode(s)
    override def invert(n: JsonNode) =
      if (n.isTextual) Success(n.asText) else InversionFailure.failedAttempt(n)
  }
  implicit val byteArray = new AbstractJsonNodeInjection[Array[Byte]] {
    def apply(b: Array[Byte]) = JsonNodeFactory.instance.binaryNode(b)
    override def invert(n: JsonNode) = attempt(n)(_.getBinaryValue)
  }
  implicit def either[L: JsonNodeInjection, R: JsonNodeInjection] =
    new AbstractJsonNodeInjection[Either[L, R]] {
      def apply(e: Either[L, R]) = e match {
        case Left(l)  => toJsonNode(l)
        case Right(r) => toJsonNode(r)
      }
      override def invert(n: JsonNode) =
        fromJsonNode[R](n).map { Right(_) }.recoverWith {
          case NonFatal(_) =>
            fromJsonNode[L](n).map { Left(_) }.recoverWith(InversionFailure.partialFailure(n))
        }
    }

  implicit def listJson[T: JsonNodeInjection]: JsonNodeInjection[List[T]] =
    collectionJson[T, List[T]]

  implicit def vectorJson[T: JsonNodeInjection]: JsonNodeInjection[Vector[T]] =
    collectionJson[T, Vector[T]]

  implicit def indexedSeqJson[T: JsonNodeInjection]: JsonNodeInjection[IndexedSeq[T]] =
    collectionJson[T, IndexedSeq[T]]

  implicit def seqJson[T: JsonNodeInjection]: JsonNodeInjection[Seq[T]] =
    collectionJson[T, Seq[T]]

  implicit def setJson[T: JsonNodeInjection]: JsonNodeInjection[Set[T]] =
    collectionJson[T, Set[T]]

  implicit def mapJson[V: JsonNodeInjection]: JsonNodeInjection[Map[String, V]] =
    new AbstractJsonNodeInjection[Map[String, V]] {
      def apply(m: Map[String, V]) = {
        val obj = JsonNodeFactory.instance.objectNode
        m.foreach {
          case (k, v) =>
            obj.put(k, toJsonNode(v))
        }
        obj
      }
      override def invert(n: JsonNode): Try[Map[String, V]] = {
        val builder = Map.newBuilder[String, V]
        builder.clear
        var cnt = 0
        n.getFields.asScala.foreach { kv =>
          val value = fromJsonNode[V](kv.getValue)
          if (value.isSuccess) {
            cnt += 1
            builder += (kv.getKey -> value.get)
          } else {
            return InversionFailure.failedAttempt(n)
          }
        }
        val res = builder.result
        if (res.size == cnt) Success(res) else InversionFailure.failedAttempt(n)
      }
    }

  // Here is where the actual work is being done
  implicit val unparsed: JsonNodeInjection[UnparsedJson] =
    new AbstractJsonNodeInjection[UnparsedJson] {
      val factory = new JsonFactory
      val mapper = new ObjectMapper
      def apply(upjson: UnparsedJson) = {
        mapper.readTree(upjson.str)
      }
      override def invert(n: JsonNode) = {
        val writer = new java.io.StringWriter()
        val gen = factory.createJsonGenerator(writer)
        mapper.writeTree(gen, n)
        Success(UnparsedJson(writer.toString))
      }
    }
}

object JsonInjection {
  def toString[T](implicit json: JsonNodeInjection[T]): Injection[T, String] =
    UnparsedJson.injection[T] andThen (UnparsedJson.unwrap)

  def fromString[T](s: String)(implicit json: JsonNodeInjection[T]): Try[T] =
    toString.invert(s)
}
