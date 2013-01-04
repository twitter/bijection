package com.twitter.bijection.json

import com.twitter.bijection.Bijection
import org.json.simple.JSONValue
import org.json.simple.parser.JSONParser

import scala.collection.JavaConverters._

/**
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 *
 * Bijection for converting between Scala basic collections and
 * types and JSON objects.
 */

trait JsonObject[V] {
  def toJava(v : V) : AnyRef
  def fromJava(v : AnyRef) : V
}

object JsonBijection {
  def toString[T: JsonObject](obj: T) = new JsonBijection[T].apply(obj)
  def fromString[T: JsonObject](s: String) = new JsonBijection[T].invert(s)
}

class JsonBijection[T: JsonObject] extends Bijection[T,String] {
  lazy val parser = new JSONParser
  override def apply(obj: T) = JSONValue.toJSONString(JsonConverter.toJava(obj))
  override def invert(s: String) = JsonConverter.fromJava(parser.parse(s))
}

object JsonConverter {
  def fromJava[V](v: AnyRef)(implicit json: JsonObject[V]) = json.fromJava(v)
  def toJava[V](v: V)(implicit json: JsonObject[V]) = json.toJava(v)
}

object JsonObject {
  implicit def mapToJsonObject[V:JsonObject]
  : JsonObject[Map[String,V]] = {
    new JsonObject[Map[String,V]] {
      def toJava(v : Map[String,V]) = {
        val innerKey = implicitly[JsonObject[String]]
        val innerVal = implicitly[JsonObject[V]]
        v.map { kv =>
          (innerKey.toJava(kv._1), innerVal.toJava(kv._2))
        }.toMap.asJava
      }
      def fromJava(v : AnyRef) = {
        val innerKey = implicitly[JsonObject[String]]
        val innerVal = implicitly[JsonObject[V]]
        v.asInstanceOf[java.util.Map[AnyRef,AnyRef]].asScala.map { kv =>
          (innerKey.fromJava(kv._1), innerVal.fromJava(kv._2))
        }.toMap
      }
    }
  }
  implicit def listToJsonObject[V:JsonObject] : JsonObject[List[V]] = new JsonObject[List[V]] {
    def toJava(v : List[V]) = {
      val inner = implicitly[JsonObject[V]]
      v.map { inner.toJava(_) }.asJava
    }
    def fromJava(v : AnyRef) = {
      val inner = implicitly[JsonObject[V]]
      v.asInstanceOf[java.util.List[AnyRef]].asScala.map { inner.fromJava(_) }.toList
    }
  }
  implicit val longJson : JsonObject[Long] = new JsonObject[Long] {
    def toJava(v : Long) = java.lang.Long.valueOf(v)
    def fromJava(v : AnyRef) = v.asInstanceOf[java.lang.Number].longValue
  }
  implicit val intJson : JsonObject[Int] = new JsonObject[Int] {
    def toJava(v : Int) = java.lang.Integer.valueOf(v)
    def fromJava(v : AnyRef) = v.asInstanceOf[java.lang.Number].intValue
  }
  implicit val doubleJson : JsonObject[Double] = new JsonObject[Double] {
    def toJava(v : Double) = java.lang.Double.valueOf(v)
    def fromJava(v : AnyRef) = v.asInstanceOf[java.lang.Number].doubleValue
  }
  implicit val boolJson : JsonObject[Boolean] = new JsonObject[Boolean] {
    def toJava(v : Boolean) = java.lang.Boolean.valueOf(v)
    def fromJava(v : AnyRef) = v.asInstanceOf[java.lang.Boolean].booleanValue
  }
  implicit val stringJson : JsonObject[String] = new JsonObject[String] {
    def toJava(v : String) = v.asInstanceOf[AnyRef]
    def fromJava(v : AnyRef) = v.asInstanceOf[String]
  }
}
