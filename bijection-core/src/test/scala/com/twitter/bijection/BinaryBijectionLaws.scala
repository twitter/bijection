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

import java.nio.ByteBuffer
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import org.scalacheck.Prop._

class BinaryBijectionLaws extends PropSpec with PropertyChecks with MustMatchers
  with BaseProperties {
  implicit val arbBB = arbitraryViaFn[Array[Byte], ByteBuffer] { ByteBuffer.wrap(_) }

  // TODO: These are all bijections,
  property("Array[Byte] <=> ByteBuffer") {
    isBijection[Array[Byte], ByteBuffer]
  }

  // These are trivially bijecitons because the right-side is only defined as the image of the left:
  property("rts Array[Byte] -> GZippedBytes") {
    isInjective[Array[Byte], GZippedBytes]
  }

  property("rts Array[Byte] -> Base64String") {
    isInjective[Array[Byte], Base64String]
  }

  property("rts Array[Byte] -> GZippedBase64String") {
    isInjective[Array[Byte], GZippedBase64String]
  }

  implicit val optSer = JavaSerializationInjection[Option[Int]]

  property("java serialize Option[Int]") {
    isInjection[Option[Int], Array[Byte]]
  }
}
