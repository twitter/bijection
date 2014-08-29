/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.bijection.guava

import com.twitter.bijection.BaseProperties
import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import com.twitter.bijection.guava.GuavaBinaryBijections._
import com.twitter.bijection.guava.GuavaBinaryBijections.Base64String
import com.twitter.bijection.guava.GuavaBinaryBijections.Base64URLString

/**
 * @author Muhammad Ashraf
 * @since 7/7/13
 */
class GuavaBinaryBijectionsLaws extends PropSpec with PropertyChecks with MustMatchers
  with BaseProperties {

  property("rts Array[Byte] -> Base64String") {
    isInjective[Array[Byte], Base64String]
  }

  property("rts Array[Byte] -> Base64URLString") {
    isInjective[Array[Byte], Base64URLString]
  }

  property("rts Array[Byte] -> Base32String") {
    isInjective[Array[Byte], Base32String]
  }

  property("rts Array[Byte] -> Base32HEXString") {
    isInjective[Array[Byte], Base32HEXString]
  }

  property("rts Array[Byte] -> Base16String") {
    isInjective[Array[Byte], Base16String]
  }

}
