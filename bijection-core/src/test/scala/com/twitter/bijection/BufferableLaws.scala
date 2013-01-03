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

import org.scalacheck.Properties
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

trait BaseBufferable {
  def defEq[T](l: T, r: T) = l == r
  def roundTrips[T: Bufferable : Arbitrary](eqfn: (T,T) => Boolean = (defEq _)) = forAll { (t: T) =>
    val buf = ByteBuffer.allocateDirect(100)
    val newBuf = Bufferable.put(buf, t)
    val newBuf2 = ByteBuffer.wrap(Bufferable.getBytes(newBuf))
    eqfn(t, Bufferable.get[T](newBuf2))
  }
}

object BufferableLaws extends Properties("Bufferable") with BaseBufferable {
  property("Reallocate works properly") = forAll { (bytes: Array[Byte]) =>
    val bb = ByteBuffer.wrap(bytes)
    bb.position(bytes.size)
    val newBb = Bufferable.reallocate(bb)
    (Bufferable.getBytes(bb).toList == Bufferable.getBytes(newBb).toList) &&
      (newBb.capacity > bb.capacity) &&
      (newBb.position == bb.position)
  }
  property("getBytes works") = forAll { (bytes: Array[Byte]) =>
    val bb = ByteBuffer.wrap(bytes)
    bb.position(bytes.size)
    Bufferable.getBytes(bb).toList == bytes.toList
  }

  property("Ints roundtrip") = roundTrips[Int]()
  property("Doubles roundtrip") = roundTrips[Double]()
  property("Floats roundtrip") = roundTrips[Float]()
  property("Shorts roundtrip") = roundTrips[Int]()
  property("Longs roundtrip") = roundTrips[Long]()
  property("(Int,Long) roundtrip") = roundTrips[(Int,Long)]()
  property("(Int,Long,String) roundtrip") = roundTrips[(Int,Long,String)]()
  property("(Int,Long,String,(Int,Long)) roundtrip") = roundTrips[(Int,Long,String,(Int,Long))]()
  property("Array[Byte] roundtrip") = roundTrips[Array[Byte]] { _.toList == _.toList }
  property("List[Double] roundtrip") = roundTrips[List[Double]]()
  property("Set[Double] roundtrip") = roundTrips[Set[Double]]()
  property("Map[String, Int] roundtrip") = roundTrips[Map[String, Int]]()
}
