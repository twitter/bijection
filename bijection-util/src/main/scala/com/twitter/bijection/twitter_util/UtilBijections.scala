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

import java.util.concurrent.{Future => JavaFuture, CompletableFuture}
import java.util.function.BiConsumer

import com.twitter.bijection._
import com.twitter.io.Buf
import com.twitter.util.{
  Future => TwitterFuture,
  Try => TwitterTry,
  Promise => TwitterPromise,
  Return,
  Throw,
  FuturePool
}

import scala.concurrent.{Future => ScalaFuture, Promise => ScalaPromise, ExecutionContext}
import scala.util.{Success, Failure, Try => ScalaTry}

/**
  * Bijection for mapping twitter-util's Future and Try onto other types.
  *
  * @author
  *   Oscar Boykin
  * @author
  *   Sam Ritchie
  * @author
  *   Moses Nakamura
  */
trait UtilBijections {

  /**
    * Bijection on Twitter Future if the bijection throws, the result will be a Throw.
    */
  implicit def futureBijection[A, B](implicit
      bij: ImplicitBijection[A, B]
  ): Bijection[TwitterFuture[A], TwitterFuture[B]] =
    new AbstractBijection[TwitterFuture[A], TwitterFuture[B]] {
      override def apply(fa: TwitterFuture[A]) = fa.map(bij(_))
      override def invert(fb: TwitterFuture[B]) = fb.map(bij.invert)
    }

  /**
    * Bijection on Scala Future if the bijection throws, the result will be a Throw.
    */
  implicit def futureScalaBijection[A, B](implicit
      bij: ImplicitBijection[A, B],
      executor: ExecutionContext
  ): Bijection[ScalaFuture[A], ScalaFuture[B]] =
    new AbstractBijection[ScalaFuture[A], ScalaFuture[B]] {
      override def apply(fa: ScalaFuture[A]) = fa.map(bij(_))
      override def invert(fb: ScalaFuture[B]) = fb.map(bij.invert)
    }

  /**
    * Bijection between twitter and scala style Futures
    */
  implicit def twitter2ScalaFuture[A](implicit
      executor: ExecutionContext
  ): Bijection[TwitterFuture[A], ScalaFuture[A]] = {
    new AbstractBijection[TwitterFuture[A], ScalaFuture[A]] {
      override def apply(f: TwitterFuture[A]): ScalaFuture[A] = {
        val p = ScalaPromise[A]()
        f.respond {
          case Return(value)    => p success value
          case Throw(exception) => p failure exception
        }
        p.future
      }

      override def invert(f: ScalaFuture[A]): TwitterFuture[A] = {
        val p = new TwitterPromise[A]()
        f.onComplete {
          case Success(value)     => p.setValue(value)
          case Failure(exception) => p.setException(exception)
        }
        p
      }
    }
  }

  /**
    * Injection from twitter futures to java futures. Will throw when inverting back from java
    * future to twitter future if the java future is not done.
    */
  def twitter2JavaFutureInjection[A]: Injection[TwitterFuture[A], JavaFuture[A]] = {
    new AbstractInjection[TwitterFuture[A], JavaFuture[A]] {
      override def apply(f: TwitterFuture[A]): JavaFuture[A] =
        f.toJavaFuture.asInstanceOf[JavaFuture[A]]

      override def invert(f: JavaFuture[A]): ScalaTry[TwitterFuture[A]] =
        Inversion.attemptWhen(f)(_.isDone)(jf => TwitterFuture(jf.get()))
    }
  }

  /**
    * Bijection between java futures and twitter futures. An implicit [[JavaFutureConverter]] is
    * needed, two strategies are available out of the box:
    *   - [[FuturePoolJavaFutureConverter]] which is based on a [[FuturePool]] and which will create
    *     one thread per future. To favor if there aren't too many futures to convert and one cares
    *     about latency.
    *   - [[TimerJavaFutureConverter]] which is based on a [[com.twitter.util.Timer]] which will
    *     create a task which will check every <code>checkFrequency</code> if the java future is
    *     completed, one thread will be used for every conversion. To favor if there are a lot of
    *     futures to convert and one cares less about the latency induced by
    *     <code>checkFrequency</code>.
    */
  implicit def twitter2JavaFutureBijection[A](implicit
      converter: JavaFutureConverter
  ): Bijection[TwitterFuture[A], JavaFuture[A]] = {
    new AbstractBijection[TwitterFuture[A], JavaFuture[A]] {
      override def apply(f: TwitterFuture[A]): JavaFuture[A] =
        f.toJavaFuture.asInstanceOf[JavaFuture[A]]

      override def invert(f: JavaFuture[A]): TwitterFuture[A] =
        converter(f)
    }
  }

  /**
    * Bijection between java completable futures and twitter futures.
    */
  implicit def twitter2CompletableFutureBijection[A]
      : Bijection[TwitterFuture[A], CompletableFuture[A]] = {
    new AbstractBijection[TwitterFuture[A], CompletableFuture[A]] {
      override def apply(f: TwitterFuture[A]): CompletableFuture[A] = {
        val cf = new CompletableFuture[A]()
        f.respond {
          case Return(value)    => cf.complete(value)
          case Throw(exception) => cf.completeExceptionally(exception)
        }
        cf
      }

      override def invert(f: CompletableFuture[A]): TwitterFuture[A] = {
        val p = new TwitterPromise[A]()
        f.whenComplete(new BiConsumer[A, Throwable] {
          override def accept(arg: A, t: Throwable): Unit =
            if (!f.isCompletedExceptionally)
              p.setValue(arg)
            else
              p.setException(t)
        })
        p
      }
    }
  }

  /**
    * Bijection between twitter and scala style Trys
    */
  implicit def twitter2ScalaTry[A]: Bijection[TwitterTry[A], ScalaTry[A]] = {
    new AbstractBijection[TwitterTry[A], ScalaTry[A]] {
      override def apply(t: TwitterTry[A]): ScalaTry[A] =
        t match {
          case Return(value)    => Success(value)
          case Throw(exception) => Failure(exception)
        }

      override def invert(t: ScalaTry[A]): TwitterTry[A] =
        t match {
          case Success(value)     => Return(value)
          case Failure(exception) => Throw(exception)
        }
    }
  }

  /**
    * Bijection on Try. If the bijection throws, the result will be a throw
    */
  implicit def tryBijection[A, B](implicit
      bij: ImplicitBijection[A, B]
  ): Bijection[TwitterTry[A], TwitterTry[B]] =
    new AbstractBijection[TwitterTry[A], TwitterTry[B]] {
      override def apply(fa: TwitterTry[A]) = fa.map(bij(_))
      override def invert(fb: TwitterTry[B]) = fb.map(bij.invert)
    }

  /**
    * Bijection on scala Try. If the bijection throws, the result will be a throw
    */
  implicit def tryScalaBijection[A, B](implicit
      bij: ImplicitBijection[A, B]
  ): Bijection[ScalaTry[A], ScalaTry[B]] =
    new AbstractBijection[ScalaTry[A], ScalaTry[B]] {
      override def apply(fa: ScalaTry[A]) = fa.map(bij(_))
      override def invert(fb: ScalaTry[B]) = fb.map(bij.invert)
    }

  /**
    * Bijection from FuturePool to ExecutionContext
    */
  implicit def futurePoolExecutionContextBijection: Bijection[FuturePool, ExecutionContext] =
    new AbstractBijection[FuturePool, ExecutionContext] {
      override def apply(pool: FuturePool) = new TwitterExecutionContext(pool)
      override def invert(context: ExecutionContext) = new ScalaFuturePool(context)
    }

  object Owned {

    /**
      * A bijection using Buf's Owned api which minimizes copying of the underlying array data but
      * places the onus of immutability on the user.
      */
    implicit def byteArrayBufBijection: Bijection[Array[Byte], Buf] =
      new AbstractBijection[Array[Byte], Buf] {
        override def apply(bytes: Array[Byte]) = Buf.ByteArray.Owned(bytes)
        override def invert(buf: Buf) = Buf.ByteArray.Owned.extract(buf)
      }
  }

  object Shared {

    /**
      * A bijection using Buf's Shared api which avoids sharing state at the cost added allocations
      * for defensive copies.
      */
    implicit def byteArrayBufBijection: Bijection[Array[Byte], Buf] =
      new AbstractBijection[Array[Byte], Buf] {
        override def apply(bytes: Array[Byte]) = Buf.ByteArray.Shared(bytes)
        override def invert(buf: Buf) = Buf.ByteArray.Shared.extract(buf)
      }
  }
}

object UtilBijections extends UtilBijections
