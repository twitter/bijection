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
import com.twitter.util.{ Future => TwitterFuture, Try => TwitterTry, Await => TwitterAwait }
import java.lang.{ Integer => JInt, Long => JLong }
import org.scalacheck.{ Arbitrary, Properties }
import org.scalacheck.Prop.forAll
import scala.concurrent.{ Future => ScalaFuture, Await => ScalaAwait }
import scala.concurrent.duration.Duration
import scala.util.{ Try => ScalaTry }
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global

object UtilBijectionLaws extends Properties("UtilBijection") with BaseProperties {
  import UtilBijections._

  protected def toOption[T](f: TwitterFuture[T]): Option[T] = TwitterTry(TwitterAwait.result(f)).toOption

  protected def toOption[T](f: ScalaFuture[T]): Option[T] = TwitterTry(ScalaAwait.result(f, Duration.Inf)).toOption

  implicit def tryArb[T: Arbitrary] = arbitraryViaFn[T, TwitterTry[T]] { TwitterTry(_) }
  implicit def scalaTryArb[T: Arbitrary] = arbitraryViaFn[T, ScalaTry[T]] { ScalaTry(_) }

  implicit def futureArb[T: Arbitrary] =
    arbitraryViaFn[TwitterTry[T], TwitterFuture[T]] { TwitterFuture.const(_) }

  implicit def scalaFutureArb[T: Arbitrary] =
    arbitraryViaFn[ScalaTry[T], ScalaFuture[T]] { st => future(st.get) }

  implicit val jIntArb = arbitraryViaBijection[Int, JInt]
  implicit val jLongArb = arbitraryViaBijection[Long, JLong]

  implicit protected def futureEq[T:Equiv]: Equiv[TwitterFuture[T]] = Equiv.fromFunction { (f1, f2) =>
    Equiv[T].equiv(TwitterAwait.result(f1), TwitterAwait.result(f2))
  }

  implicit protected def scalaFutureEq[T:Equiv]: Equiv[ScalaFuture[T]] = Equiv.fromFunction { (f1, f2) =>
    Equiv[T].equiv(ScalaAwait.result(f1, Duration.Inf), ScalaAwait.result(f2, Duration.Inf))
  }

  type FromMap = Map[Int, Long]
  type ToMap = Map[JInt, JLong]

  property("round trips com.twitter.util.Future[Map[Int, String]] -> com.twitter.util.Future[JInt, JLong]") =
    isBijection[TwitterFuture[FromMap], TwitterFuture[ToMap]]

  property("round trips scala.concurrent.Future[Map[Int, String]] -> scala.concurrent.Future[JInt, JLong]") =
    isBijection[ScalaFuture[FromMap], ScalaFuture[ToMap]]

  property("round trips com.twitter.util.Try[Map[Int, String]] -> com.twitter.util.Try[Map[JInt, JLong]]") =
    isBijection[TwitterTry[FromMap], TwitterTry[ToMap]]

  property("round trips scala.util.Try[Map[Int, String]] -> scala.util.Try[Map[JInt, JLong]]") =
    isBijection[ScalaTry[FromMap], ScalaTry[ToMap]]

  property("round trips com.twitter.util.Try[Map[JInt, JLong]] -> scala.util.Try[Map[JInt, JLong]]") =
    isBijection[TwitterTry[ToMap], ScalaTry[ToMap]]

  property("round trips com.twitter.util.Future[Map[JInt, JLong]] -> scala.concurrent.Future[Map[JInt, JLong]]") =
    isBijection[TwitterFuture[ToMap], ScalaFuture[ToMap]]

}
