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

import com.twitter.bijection.{Bijection, Injection}

import org.codehaus.jackson.{JsonParser, JsonNode, JsonFactory}
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.{
  BooleanNode,
  IntNode,
  JsonNodeFactory,
  LongNode
}

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scala.collection.JavaConverters._

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 *
 * Bijection for converting between Scala basic collections and
 * types and JSON objects.
 */

import scala.util.control.Exception.allCatch

trait JsonNodeInjection[T] extends Injection[T, JsonNode]

abstract class AbstractJsonNodeInjection[T] extends JsonNodeInjection[T]

trait LowPriorityJson {
  def viaInjection[A,B](implicit inj: Injection[A,B], json: JsonNodeInjection[B]):
    JsonNodeInjection[A] = new AbstractJsonNodeInjection[A] {
      def apply(a: A) = json(inj(a))
      def invert(j: JsonNode) = json.invert(j).flatMap { inj.invert(_) }
    }
  def viaBijection[A,B](implicit bij: Bijection[A,B], json: JsonNodeInjection[B]):
    JsonNodeInjection[A] = new AbstractJsonNodeInjection[A] {
      def apply(a: A) = json(bij(a))
      def invert(j: JsonNode) = json.invert(j).map { bij.invert(_) }
    }
}

object JsonNodeInjection extends LowPriorityJson with java.io.Serializable {

  def toJsonNode[T](t: T)(implicit json: JsonNodeInjection[T]): JsonNode =
    json.apply(t)

  def fromJsonNode[T](node: JsonNode)(implicit json: JsonNodeInjection[T]): Option[T] =
    json.invert(node)

  implicit val identity = new AbstractJsonNodeInjection[JsonNode] {
    def apply(n: JsonNode) = n
    override def invert(n: JsonNode) = Some(n)
  }
  implicit val booleanJson = new AbstractJsonNodeInjection[Boolean] {
    def apply(b: Boolean) = JsonNodeFactory.instance.booleanNode(b)
    override def invert(n: JsonNode) = if(n.isBoolean) Some(n.getValueAsBoolean) else None
  }
  implicit val shortJson = new AbstractJsonNodeInjection[Short] {
    def apply(i: Short) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = if(n.isInt) Some(n.getValueAsInt.toShort) else None
  }
  implicit val intJson = new AbstractJsonNodeInjection[Int] {
    def apply(i: Int) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = if (n.isInt) Some(n.getValueAsInt) else None
  }
  implicit val longJson = new AbstractJsonNodeInjection[Long] {
    def apply(i: Long) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = if(n.isLong || n.isInt) Some(n.getValueAsLong) else None
  }
  implicit val floatJson = new AbstractJsonNodeInjection[Float] {
    def apply(i: Float) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = allCatch.opt(n.getValueAsDouble.toFloat)
  }
  implicit val doubleJson = new AbstractJsonNodeInjection[Double] {
    def apply(i: Double) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = if (n.isDouble) Some(n.getValueAsDouble) else None
  }
  implicit val stringJson = new AbstractJsonNodeInjection[String] {
    def apply(s: String) = JsonNodeFactory.instance.textNode(s)
    override def invert(n: JsonNode) = if(n.isTextual) Some(n.getValueAsText) else None
  }
  implicit val byteArray = new AbstractJsonNodeInjection[Array[Byte]] {
    def apply(b: Array[Byte]) = JsonNodeFactory.instance.binaryNode(b)
    override def invert(n: JsonNode) = allCatch.opt(n.getBinaryValue)
  }
  implicit def either[L:JsonNodeInjection, R:JsonNodeInjection] = new AbstractJsonNodeInjection[Either[L,R]] {
    def apply(e: Either[L,R]) = e match {
      case Left(l) => toJsonNode(l)
      case Right(r) => toJsonNode(r)
    }
    override def invert(n: JsonNode) =
      fromJsonNode[R](n)
        .map { Right(_) }
        .orElse(fromJsonNode[L](n).map { Left(_) })
  }

  // This causes diverging implicits
  def collectionJson[T,C <: Traversable[T]](implicit cbf: CanBuildFrom[Nothing, T, C],
    jbij: JsonNodeInjection[T]): JsonNodeInjection[C] = fromBuilder(cbf())

  def fromBuilder[T,C <: Traversable[T]](builder: Builder[T,C])
    (implicit jbij: JsonNodeInjection[T]): JsonNodeInjection[C] =
    new AbstractJsonNodeInjection[C] {
      def apply(l: C) = {
        val ary = JsonNodeFactory.instance.arrayNode
        l foreach { t => ary.add(jbij(t)) }
        ary
      }
      override def invert(n: JsonNode): Option[C] = {
        builder.clear
        var inCount = 0
        n.getElements.asScala.foreach { jn =>
          inCount += 1
          val thisC = jbij.invert(jn)
          if(thisC.isEmpty) {
            return None
          }
          builder += thisC.get
        }
        Some(builder.result).filter { _.size == inCount }
      }
    }

  implicit def listJson[T:JsonNodeInjection]: JsonNodeInjection[List[T]] =
    fromBuilder(List.newBuilder[T])

  implicit def vectorJson[T:JsonNodeInjection]: JsonNodeInjection[Vector[T]] =
    fromBuilder(Vector.newBuilder[T])

  implicit def indexedSeqJson[T:JsonNodeInjection]: JsonNodeInjection[IndexedSeq[T]] =
    fromBuilder(IndexedSeq.newBuilder[T])

  implicit def seqJson[T:JsonNodeInjection]: JsonNodeInjection[Seq[T]] =
    fromBuilder(Seq.newBuilder[T])

  implicit def setJson[T:JsonNodeInjection]: JsonNodeInjection[Set[T]] =
    fromBuilder(Set.newBuilder[T])

  implicit def mapJson[V:JsonNodeInjection]: JsonNodeInjection[Map[String,V]] =
    new AbstractJsonNodeInjection[Map[String,V]] {
      def apply(m: Map[String,V]) = {
        val obj = JsonNodeFactory.instance.objectNode
        m.foreach { case (k,v) =>
          obj.put(k, toJsonNode(v))
        }
        obj
      }
      override def invert(n: JsonNode): Option[Map[String,V]] = {
        val builder = Map.newBuilder[String, V]
        builder.clear
        var cnt = 0
        n.getFields.asScala.foreach { kv =>
          val value = fromJsonNode[V](kv.getValue)
          if (value.isDefined) {
            cnt += 1
            builder += (kv.getKey -> value.get)
          }
          else {
            return None
          }
        }
        Some(builder.result).filter { _.size == cnt }
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
        Some(UnparsedJson(writer.toString))
      }
    }
}

object JsonInjection {
  def toString[T](implicit json: JsonNodeInjection[T]): Injection[T, String] =
    UnparsedJson.injection[T] andThen (UnparsedJson.unwrap)

  def fromString[T](s: String)(implicit json: JsonNodeInjection[T]): Option[T] =
    toString.invert(s)
}
