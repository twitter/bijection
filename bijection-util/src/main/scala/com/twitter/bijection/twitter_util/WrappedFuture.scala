/*
Copyright 2013 Twitter, Inc.

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

import scala.concurrent.{Future => SFuture, Promise => SPromise, ExecutionContext, CanAwait}
import scala.concurrent.duration.{Duration => SDuration}
import scala.util.{Try => STry, Success, Failure}
import com.twitter.util.{
  Future => TFuture,
  Try => TTry,
  Promise => TPromise,
  Await => TAwait,
  Duration => TDuration
}

import java.util.NoSuchElementException

import com.twitter.bijection.Conversion.asMethod

import UtilBijections._

object WrappedTFuture {
  def apply[T](t: TFuture[T]): WrappedTFuture[T] = new WrappedTFuture(t)

  def toTwitter[T](f: SFuture[T])(implicit executor: ExecutionContext): TFuture[T] = {
    f match {
      case w: WrappedTFuture[_] => w.asTwitter
      case _ =>
        val p = new TPromise[T]()
        f.onComplete { t => p.update(t.as[TTry[T]]) }
        p
    }
  }
}

class WrappedTFuture[+T](val asTwitter: TFuture[T]) extends SFuture[T] {
  override def isCompleted = asTwitter.isDefined
  override def value: Option[STry[T]] = asTwitter.poll.map(_.as[STry[T]])
  override def onComplete[U](func: (STry[T]) => U)(implicit executor: ExecutionContext): Unit =
    asTwitter.respond { ttry =>
      executor.prepare().execute {
        new java.lang.Runnable {
          override def run { func(ttry.as[STry[T]]) }
        }
      }
    }

  override def ready(atMost: SDuration)(implicit permit: CanAwait) = {
    TAwait.ready(asTwitter, atMost.as[TDuration])
    this
  }

  override def result(atMost: SDuration)(implicit permit: CanAwait): T =
    TAwait.result(asTwitter, atMost.as[TDuration])

  ///////////
  // These are not needed strictly, but we prefer to delegate to the internal
  // future here for maximum efficienncy
  //////////
  override def collect[S](pf: PartialFunction[T, S])(implicit executor: ExecutionContext): SFuture[S] = {
    val lifted = pf.lift
    new WrappedTFuture(asTwitter.flatMap { t =>
      lifted(t) match {
        case Some(s) => TFuture.value(s)
        case None => TFuture.exception(new NoSuchElementException(t.toString))
      }
    })
  }

  override def filter(f: (T) => Boolean)(implicit executor: ExecutionContext): SFuture[T] =
    new WrappedTFuture(asTwitter.filter(f))

  override def foreach[S](f: (T) => S)(implicit executor: ExecutionContext): Unit =
    asTwitter.foreach { t => f(t) } // twitter requires a unit return

  override def map[S](f: (T) => S)(implicit executor: ExecutionContext): SFuture[S] =
    new WrappedTFuture(asTwitter.map(f))

  override def flatMap[S](f: (T) => SFuture[S])(implicit executor: ExecutionContext): SFuture[S] =
    new WrappedTFuture(asTwitter.flatMap { t => WrappedTFuture.toTwitter(f(t)) })

  override def onSuccess[U](pf: PartialFunction[T, U])(implicit executor: ExecutionContext): Unit =
    asTwitter.onSuccess { t => pf.lift(t) }

  override def onFailure[U](pf: PartialFunction[Throwable, U])(implicit executor: ExecutionContext): Unit =
    asTwitter.onFailure { t => pf.lift(t) }

  override def recover[U >: T](pf: PartialFunction[Throwable, U])(implicit executor: ExecutionContext): SFuture[U] =
    new WrappedTFuture(asTwitter.handle(pf))

  override def recoverWith[U >: T](pf: PartialFunction[Throwable, SFuture[U]])(implicit executor: ExecutionContext): SFuture[U] =
    new WrappedTFuture(asTwitter.rescue(pf.andThen(_.as[TFuture[U]])))
}

class WrappedTPromise[T](val asTwitter: TPromise[T]) extends SPromise[T] {
  override def future = new WrappedTFuture(asTwitter)
  override def isCompleted = asTwitter.isDefined
  override def tryComplete(t: STry[T]): Boolean = asTwitter.updateIfEmpty(t.as[TTry[T]])
}
