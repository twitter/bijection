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

import com.twitter.bijection.{CheckProperties, BaseProperties}
import com.twitter.io.Buf
import com.twitter.util.{
  Future => TwitterFuture,
  Try => TwitterTry,
  Await => TwitterAwait,
  FuturePool,
  JavaTimer
}
import java.lang.{Integer => JInt, Long => JLong}
import java.util.concurrent.{Future => JavaFuture, Callable, FutureTask}
import org.scalacheck.Arbitrary
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.{Future => ScalaFuture, Await => ScalaAwait}
import scala.concurrent.duration.Duration
import scala.util.{Try => ScalaTry}
import scala.concurrent.ExecutionContext.Implicits.global

class UtilBijectionLaws extends CheckProperties with BaseProperties with BeforeAndAfterAll {
  import UtilBijections._

  protected def toOption[T](f: TwitterFuture[T]): Option[T] =
    TwitterTry(TwitterAwait.result(f)).toOption

  protected def toOption[T](f: ScalaFuture[T]): Option[T] =
    TwitterTry(ScalaAwait.result(f, Duration.Inf)).toOption

  protected def toOption[T](f: JavaFuture[T]): Option[T] =
    TwitterTry(f.get()).toOption

  implicit def futureArb[T: Arbitrary] = arbitraryViaFn[T, TwitterFuture[T]] {
    TwitterFuture.value
  }
  implicit def scalaFutureArb[T: Arbitrary] = arbitraryViaFn[T, ScalaFuture[T]] {
    ScalaFuture.apply(_)
  }
  implicit def javaFutureArb[T: Arbitrary] = arbitraryViaFn[T, JavaFuture[T]] { t =>
    val f = new FutureTask[T](new Callable[T] {
      override def call(): T = t
    })
    f.run()
    f
  }
  implicit def tryArb[T: Arbitrary] = arbitraryViaFn[T, TwitterTry[T]] { TwitterTry(_) }
  implicit def scalaTryArb[T: Arbitrary] = arbitraryViaFn[T, ScalaTry[T]] { ScalaTry(_) }

  implicit val jIntArb = arbitraryViaBijection[Int, JInt]
  implicit val jLongArb = arbitraryViaBijection[Long, JLong]
  implicit val bufArb: Arbitrary[Buf] = arbitraryViaFn[Array[Byte], Buf](Buf.ByteArray.Owned.apply)

  implicit protected def futureEq[T: Equiv]: Equiv[TwitterFuture[T]] =
    Equiv.fromFunction { (f1, f2) =>
      Equiv[Option[T]].equiv(toOption(f1), toOption(f2))
    }

  implicit protected def scalaFutureEq[T: Equiv]: Equiv[ScalaFuture[T]] =
    Equiv.fromFunction { (f1, f2) =>
      Equiv[Option[T]].equiv(toOption(f1), toOption(f2))
    }

  implicit protected def javaFutureEq[T: Equiv]: Equiv[JavaFuture[T]] =
    Equiv.fromFunction { (f1, f2) =>
      Equiv[Option[T]].equiv(toOption(f1), toOption(f2))
    }

  type FromMap = Map[Int, Long]
  type ToMap = Map[JInt, JLong]

  property("round trips TwitterFuture[Map[Int, String]] <-> Twitter.Future[JInt, JLong]") {
    isBijection[TwitterFuture[FromMap], TwitterFuture[ToMap]]
  }

  property("round trips ScalaFuture[Map[Int, String]] <-> ScalaFuture[JInt, JLong]") {
    isBijection[ScalaFuture[FromMap], ScalaFuture[ToMap]]
  }

  property("round trips TwitterTry[Map[Int, String]] <-> TwitterTry[Map[JInt, JLong]]") {
    isBijection[TwitterTry[FromMap], TwitterTry[ToMap]]
  }

  property("round trips ScalaTry[Map[Int, String]] <-> ScalaTry[Map[JInt, JLong]]") {
    isBijection[ScalaTry[FromMap], ScalaTry[ToMap]]
  }

  property("round trips TwitterTry[Map[JInt, JLong]] <-> ScalaTry[Map[JInt, JLong]]") {
    isBijection[TwitterTry[ToMap], ScalaTry[ToMap]]
  }

  property("round trips TwitterFuture[Map[JInt, JLong]] <-> ScalaFuture[Map[JInt, JLong]]") {
    isBijection[TwitterFuture[ToMap], ScalaFuture[ToMap]]
  }

  property(
    "round trips TwitterFuture[Map[JInt, JLong]] <-> JavaFuture[Map[JInt, JLong]] " +
      "using FuturePool"
  ) {
    implicit val converter = new FuturePoolJavaFutureConverter(FuturePool.unboundedPool, true)
    isBijection[TwitterFuture[ToMap], JavaFuture[ToMap]]
  }

  property(
    "round trips TwitterFuture[Map[JInt, JLong]] <-> JavaFuture[Map[JInt, JLong]] " +
      "using Timer"
  ) {
    implicit val converter =
      new TimerJavaFutureConverter(new JavaTimer, com.twitter.util.Duration.fromSeconds(1), true)
    isBijection[TwitterFuture[ToMap], JavaFuture[ToMap]]
  }

  property("TwitterFuture[Map[JInt, JLong]] -> JavaFuture[Map[JInt, JLong]]") {
    isInjection[TwitterFuture[ToMap], JavaFuture[ToMap]](
      futureArb[ToMap],
      twitter2JavaFutureInjection,
      javaFutureArb[ToMap],
      futureEq,
      javaFutureEq
    )
  }

  property("round trips shared com.twitter.io.Buf <-> Array[Byte]") {
    import Shared.byteArrayBufBijection

    isBijection[Array[Byte], Buf]
  }

  property("round trips owned com.twitter.io.Buf <-> Array[Byte]") {
    import Owned.byteArrayBufBijection

    isBijection[Array[Byte], Buf]
  }
}
