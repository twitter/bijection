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

package com.twitter.bijection

import java.lang.{
  Short => JShort,
  Integer => JInt,
  Long => JLong,
  Float => JFloat,
  Double => JDouble,
  Byte => JByte
}

import java.nio.ByteBuffer

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

trait BaseBufferable {
  def roundTrips[T: Bufferable: Arbitrary: Equiv] = forAll { (t: T) =>
    val buf = ByteBuffer.allocateDirect(100)
    val newBuf = Bufferable.put(buf, t)
    val newBuf2 = ByteBuffer.wrap(Bufferable.getBytes(newBuf))
    val pos = newBuf2.position
    val (newBuf3, rtt) = Bufferable.get[T](newBuf2).get
    val copyT = Bufferable.deepCopy(t)
    Equiv[T].equiv(t, rtt) && Equiv[T].equiv(t, copyT) && (newBuf2.position == pos)
  }
  implicit protected def aeq[T: Equiv]: Equiv[Array[T]] = Equiv.fromFunction { (a1, a2) =>
    a1.zip(a2).forall { tup => Equiv[T].equiv(tup._1, tup._2) }
  }
  def itereq[C <: Iterable[T], T: Equiv]: Equiv[C] = Equiv.fromFunction { (a1, a2) =>
    a1.zip(a2).forall { tup => Equiv[T].equiv(tup._1, tup._2) }
  }
}

class BufferableLaws extends PropSpec with PropertyChecks with MustMatchers with BaseBufferable {
  property("Reallocate works properly") {
    forAll { (bytes: Array[Byte]) =>
      val bb = ByteBuffer.wrap(bytes)
      bb.position(bytes.size)
      val newBb = Bufferable.reallocate(bb)
      assert(Bufferable.getBytes(bb).toList == Bufferable.getBytes(newBb).toList)
      assert(newBb.capacity > bb.capacity)
      assert(newBb.position == bb.position)
    }
  }

  property("getBytes works") {
    forAll { (bytes: Array[Byte]) =>
      val bb = ByteBuffer.wrap(bytes)
      bb.position(bytes.size)
      assert(Bufferable.getBytes(bb).toList == bytes.toList)
    }
  }

  implicit val laeq = itereq[List[Array[Byte]], Array[Byte]]

  property("Bools roundtrip") {
    roundTrips[Boolean]
  }

  property("Ints roundtrip") {
    roundTrips[Int]
  }

  property("Doubles roundtrip") {
    roundTrips[Double]
  }

  property("Floats roundtrip") {
    roundTrips[Float]
  }
  property("Shorts roundtrip") {
    roundTrips[Int]
  }
  property("Longs roundtrip") {
    roundTrips[Long]
  }
  property("(Int,Long) roundtrip") {
    roundTrips[(Int, Long)]
  }

  property("(Int,Long,String) roundtrip") {
    roundTrips[(Int, Long, String)]
  }

  property("(Int,Long,String,(Int,Long)) roundtrip") {
    roundTrips[(Int, Long, String, (Int, Long))]
  }

  property("Array[Byte] roundtrip") {
    roundTrips[Array[Byte]]
  }

  property("Array[Int] roundtrip") {
    roundTrips[Array[Int]]
  }

  property("List[Double] roundtrip") {
    roundTrips[List[Double]]
  }

  property("List[Array[Byte]] roundtrip") {
    roundTrips[List[Array[Byte]]]
  }

  property("Set[Double] roundtrip") {
    roundTrips[Set[Double]]
  }

  property("Map[String, Int] roundtrip") {
    roundTrips[Map[String, Int]]
  }

  property("Option[(Long,Long)] roundtrip") {
    roundTrips[Option[(Long, Long)]]
  }

  property("Either[Long,String] roundtrip") {
    roundTrips[Either[Long, String]]
  }

  implicit val symbolArb = Arbitrary {
    implicitly[Arbitrary[String]]
      .arbitrary.map { Symbol(_) }
  }

  property("Symbol roundtrip") {
    roundTrips[Symbol]
  }
}
