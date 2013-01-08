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

import com.twitter.bijection.Bijection

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

trait JsonNodeBijection[T] extends Bijection[T, JsonNode] {
  def accepts(n: JsonNode): Boolean
}

trait LowPriorityJson {
  def viaBijection[A,B](implicit bij: Bijection[A,B], json: JsonNodeBijection[B]):
    JsonNodeBijection[A] = new JsonNodeBijection[A] {
      def apply(a: A) = json(bij(a))
      override def invert(j: JsonNode) = bij.invert(json.invert(j))
      def accepts(n: JsonNode) = json.accepts(n)
    }
}

object JsonNodeBijection extends LowPriorityJson with java.io.Serializable {
  protected val factory = new JsonFactory

  def toJsonNode[T](t: T)(implicit json: JsonNodeBijection[T]): JsonNode =
    json.apply(t)

  def fromJsonNode[T](node: JsonNode)(implicit json: JsonNodeBijection[T]): T =
    json.invert(node)

  implicit val identity = new JsonNodeBijection[JsonNode] {
    def apply(n: JsonNode) = n
    override def invert(n: JsonNode) = n
    def accepts(n: JsonNode) = true
  }
  implicit val booleanJson = new JsonNodeBijection[Boolean] {
    def apply(b: Boolean) = JsonNodeFactory.instance.booleanNode(b)
    override def invert(n: JsonNode) = n.getValueAsBoolean
    def accepts(n: JsonNode) = n.isBoolean
  }
  implicit val shortJson = new JsonNodeBijection[Short] {
    def apply(i: Short) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = n.getValueAsInt.toShort
    def accepts(n: JsonNode) = n.isInt
  }
  implicit val intJson = new JsonNodeBijection[Int] {
    def apply(i: Int) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = n.getValueAsInt
    def accepts(n: JsonNode) = n.isInt
  }
  implicit val longJson = new JsonNodeBijection[Long] {
    def apply(i: Long) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = n.getValueAsLong
    def accepts(n: JsonNode) = n.isLong
  }
  implicit val floatJson = new JsonNodeBijection[Float] {
    def apply(i: Float) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = n.getValueAsDouble.toFloat
    def accepts(n: JsonNode) = n.isDouble
  }
  implicit val doubleJson = new JsonNodeBijection[Double] {
    def apply(i: Double) = JsonNodeFactory.instance.numberNode(i)
    override def invert(n: JsonNode) = n.getValueAsDouble
    def accepts(n: JsonNode) = n.isDouble
  }
  implicit val stringJson = new JsonNodeBijection[String] {
    def apply(s: String) = JsonNodeFactory.instance.textNode(s)
    override def invert(n: JsonNode) = n.getValueAsText
    def accepts(n: JsonNode) = n.isTextual
  }
  implicit val byteArray = new JsonNodeBijection[Array[Byte]] {
    def apply(b: Array[Byte]) = JsonNodeFactory.instance.binaryNode(b)
    override def invert(n: JsonNode) = n.getBinaryValue
    def accepts(n: JsonNode) = n.isBinary
  }
  implicit def either[L:JsonNodeBijection, R:JsonNodeBijection] = new JsonNodeBijection[Either[L,R]] {
    def apply(e: Either[L,R]) = e match {
      case Left(l) => toJsonNode(l)
      case Right(r) => toJsonNode(r)
    }
    override def invert(n: JsonNode) = {
      if (isR(n))
        Right(fromJsonNode[R](n))
      else
        Left(fromJsonNode[L](n))
    }
    private def isL(n: JsonNode) = implicitly[JsonNodeBijection[L]].accepts(n)
    private def isR(n: JsonNode) = implicitly[JsonNodeBijection[R]].accepts(n)

    def accepts(n: JsonNode) = isR(n) || isL(n)
  }

  // This causes diverging implicits
  def collectionJson[T,C <: Traversable[T]](implicit cbf: CanBuildFrom[Nothing, T, C],
    jbij: JsonNodeBijection[T]): JsonNodeBijection[C] = fromBuilder(cbf())

  def fromBuilder[T,C <: Traversable[T]](builder: Builder[T,C])
    (implicit jbij: JsonNodeBijection[T]): JsonNodeBijection[C] =
    new JsonNodeBijection[C] {
      def apply(l: C) = {
        val ary = JsonNodeFactory.instance.arrayNode
        l foreach { t => ary.add(jbij(t)) }
        ary
      }
      override def invert(n: JsonNode) = {
        builder.clear
        n.getElements.asScala.foreach { jn => builder += jbij.invert(jn) }
        builder.result
      }
      def accepts(n: JsonNode) = n.isArray && {
        fromJsonNode[List[JsonNode]](n).forall { it => jbij.accepts(it) }
      }
    }

  implicit def listJson[T:JsonNodeBijection]: JsonNodeBijection[List[T]] =
    fromBuilder(List.newBuilder[T])

  implicit def vectorJson[T:JsonNodeBijection]: JsonNodeBijection[Vector[T]] =
    fromBuilder(Vector.newBuilder[T])

  implicit def indexedSeqJson[T:JsonNodeBijection]: JsonNodeBijection[IndexedSeq[T]] =
    fromBuilder(IndexedSeq.newBuilder[T])

  implicit def seqJson[T:JsonNodeBijection]: JsonNodeBijection[Seq[T]] =
    fromBuilder(Seq.newBuilder[T])

  implicit def setJson[T:JsonNodeBijection]: JsonNodeBijection[Set[T]] =
    fromBuilder(Set.newBuilder[T])

  implicit def mapJson[V:JsonNodeBijection]: JsonNodeBijection[Map[String,V]] =
    new JsonNodeBijection[Map[String,V]] {
      def apply(m: Map[String,V]) = {
        val obj = JsonNodeFactory.instance.objectNode
        m.foreach { case (k,v) =>
          obj.put(k, toJsonNode(v))
        }
        obj
      }
      override def invert(n: JsonNode) = {
        val builder = Map.newBuilder[String, V]
        builder.clear
        n.getFields.asScala.foreach { kv =>
          builder += ((kv.getKey, fromJsonNode[V](kv.getValue)))
        }
        builder.result
      }
      def accepts(n: JsonNode) = n.isObject && {
        val vbij = implicitly[JsonNodeBijection[V]]
        fromJsonNode[Map[String, JsonNode]](n).forall { kv => vbij.accepts(kv._2) }
      }
    }

  // Here is where the actual work is being done
  implicit val unparsed: JsonNodeBijection[UnparsedJson] =
    new JsonNodeBijection[UnparsedJson] {
      val mapper = new ObjectMapper
      def apply(upjson: UnparsedJson) = {
        mapper.readTree(upjson.str)
      }
      override def invert(n: JsonNode) = {
        val writer = new java.io.StringWriter()
        val gen = factory.createJsonGenerator(writer)
        mapper.writeTree(gen, n)
        UnparsedJson(writer.toString)
      }
      def accepts(n: JsonNode) = true
    }
}

object JsonBijection {
  def toString[T](implicit json: JsonNodeBijection[T]): Bijection[T, String] =
    UnparsedJson.bijection[T] andThen (UnparsedJson.unwrap)

  def fromString[T](s: String)(implicit json: JsonNodeBijection[T]) =
    toString.invert(s)
}
