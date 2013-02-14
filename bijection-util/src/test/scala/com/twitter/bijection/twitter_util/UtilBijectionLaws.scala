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

package com.twitter.bijection.twitter_util

import com.twitter.bijection.{ BaseProperties, Bijection }
import com.twitter.util.{ Future, Try }
import java.lang.{ Integer => JInt, Long => JLong }
import org.scalacheck.{ Arbitrary, Properties }
import org.scalacheck.Prop.forAll

object UtilBijectionLaws extends Properties("UtilBijection") with BaseProperties {
  import UtilBijections._

  protected def toOption[T](f: Future[T]): Option[T] = if (f.isReturn) Some(f.get) else None

  implicit def futureArb[T: Arbitrary] = arbitraryViaFn[T, Future[T]] { Future.value(_) }
  implicit def tryArb[T: Arbitrary] = arbitraryViaFn[T, Try[T]] { Try(_) }
  implicit val jIntArb = arbitraryViaBijection[Int, JInt]
  implicit val jLongArb = arbitraryViaBijection[Long, JLong]

  implicit protected def futureEq[T:Equiv]: Equiv[Future[T]] = Equiv.fromFunction { (f1, f2) =>
    Equiv[Option[T]].equiv(toOption(f1), toOption(f2))
  }

  type FromMap = Map[Int, Long]
  type ToMap = Map[JInt, JLong]

  property("round trips Future[Map[Int, String]] -> Future[JInt, JLong]") =
    isBijection[Future[FromMap], Future[ToMap]]

  property("round trips Try[Map[Int, String]] -> Try[Map[JInt, JLong]]") =
    isBijection[Try[FromMap], Try[ToMap]]
}
