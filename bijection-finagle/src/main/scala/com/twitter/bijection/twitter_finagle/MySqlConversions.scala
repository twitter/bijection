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

package com.twitter.bijection.twitter_finagle

import com.twitter.finagle.exp.mysql._
import com.twitter.bijection._
import scala.util.Try
import java.sql.Timestamp

/**
 * Bijections and injections for mapping twitter-finagle's MySql datatypes to Scala datatypes
 * other types.
 *
 *  @author George Leontiev
 */

trait MySqlBijections {
  implicit val byte: Bijection[ByteValue, Byte] = new AbstractBijection[ByteValue, Byte] {
    def apply(v: ByteValue) = v.b
    override def invert(b: Byte) = ByteValue(b)
  }

  implicit val short: Bijection[ShortValue, Short] = new AbstractBijection[ShortValue, Short] {
    def apply(s: ShortValue) = s.s
    override def invert(s: Short) = ShortValue(s)
  }

  implicit val int: Bijection[IntValue, Int] = new AbstractBijection[IntValue, Int] {
    def apply(i: IntValue) = i.i
    override def invert(i: Int) = IntValue(i)
  }

  implicit val long: Bijection[LongValue, Long] = new AbstractBijection[LongValue, Long] {
    def apply(l: LongValue) = l.l
    override def invert(l: Long) = LongValue(l)
  }

  implicit val float: Bijection[FloatValue, Float] = new AbstractBijection[FloatValue, Float] {
    def apply(f: FloatValue) = f.f
    override def invert(f: Float) = FloatValue(f)
  }

  implicit val double: Bijection[DoubleValue, Double] = new AbstractBijection[DoubleValue, Double] {
    def apply(d: DoubleValue) = d.d
    override def invert(d: Double) = DoubleValue(d)
  }

  implicit val string: Bijection[StringValue, String] = new AbstractBijection[StringValue, String] {
    def apply(s: StringValue) = s.s
    override def invert(s: String) = StringValue(s)
  }

  implicit val boolean: Bijection[ByteValue, Boolean] = new AbstractBijection[ByteValue, Boolean] {
    def apply(t: ByteValue) = t.b == 1
    override def invert(b: Boolean) = ByteValue(if (b) 1 else 0)
  }
}

trait MySqlInjections {
  implicit val timestamp: Injection[Timestamp, Value] =
    new AbstractInjection[Timestamp, Value] {
      private val UTC = java.util.TimeZone.getTimeZone("UTC")
      private val timestampValue = new TimestampValue(UTC, UTC)
      def apply(t: Timestamp) = timestampValue(t)
      override def invert(v: Value) = Try(timestampValue.unapply(v).get)
    }

  implicit def nullValue[A]: Injection[NullValue.type, Option[A]] =
    new AbstractInjection[NullValue.type, Option[A]] {
      def apply(n: NullValue.type) = None
      override def invert(n: Option[A]) = Try(n.map(_ => NullValue).get)
    }

  implicit def emptyValue[A]: Injection[EmptyValue.type, Option[A]] =
    new AbstractInjection[EmptyValue.type, Option[A]] {
      def apply(n: EmptyValue.type) = None
      override def invert(n: Option[A]) = Try(n.map(_ => EmptyValue).get)
    }
}

object MySqlConversions extends MySqlBijections with MySqlInjections
