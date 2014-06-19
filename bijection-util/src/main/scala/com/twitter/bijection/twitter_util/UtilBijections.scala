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

import com.twitter.bijection.{ AbstractBijection, Bijection, ImplicitBijection }
import com.twitter.util.{ Future => TwitterFuture, Try => TwitterTry, Promise => TwitterPromise, Return, Throw, FuturePool }

import scala.concurrent.{ Future => ScalaFuture, Promise => ScalaPromise, ExecutionContext }
import scala.util.{ Success, Failure, Try => ScalaTry }

/**
 * Bijection for mapping twitter-util's Future and Try onto
 * other types.
 *
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 *  @author Moses Nakamura
 */

trait UtilBijections {
  /**
   * Bijection on Twitter Future
   * if the bijection throws, the result will be a Throw.
   */
  implicit def futureBijection[A, B](implicit bij: ImplicitBijection[A, B]): Bijection[TwitterFuture[A], TwitterFuture[B]] =
    new AbstractBijection[TwitterFuture[A], TwitterFuture[B]] {
      override def apply(fa: TwitterFuture[A]) = fa.map(bij(_))
      override def invert(fb: TwitterFuture[B]) = fb.map(bij.invert(_))
    }

  /**
   * Bijection on Scala Future
   * if the bijection throws, the result will be a Throw.
   */
  implicit def futureScalaBijection[A, B](implicit bij: ImplicitBijection[A, B], executor: ExecutionContext): Bijection[ScalaFuture[A], ScalaFuture[B]] =
    new AbstractBijection[ScalaFuture[A], ScalaFuture[B]] {
      override def apply(fa: ScalaFuture[A]) = fa.map(bij(_))
      override def invert(fb: ScalaFuture[B]) = fb.map(bij.invert(_))
    }

  /**
   * Bijection between twitter and scala style Futures
   */
  implicit def twitter2ScalaFuture[A](implicit executor: ExecutionContext): Bijection[TwitterFuture[A], ScalaFuture[A]] = {
    new AbstractBijection[TwitterFuture[A], ScalaFuture[A]] {
      override def apply(f: TwitterFuture[A]): ScalaFuture[A] = {
        val p = ScalaPromise[A]()
        f.respond {
          case Return(value) => p success value
          case Throw(exception) => p failure exception
        }
        p.future
      }

      override def invert(f: ScalaFuture[A]): TwitterFuture[A] = {
        val p = new TwitterPromise[A]()
        f.onComplete {
          case Success(value) => p.setValue(value)
          case Failure(exception) => p.setException(exception)
        }
        p
      }
    }
  }

  /**
   * Bijection between twitter and scala style Trys
   */
  implicit def twitter2ScalaTry[A]: Bijection[TwitterTry[A], ScalaTry[A]] = {
    new AbstractBijection[TwitterTry[A], ScalaTry[A]] {
      override def apply(t: TwitterTry[A]): ScalaTry[A] = t match {
        case Return(value) => Success(value)
        case Throw(exception) => Failure(exception)
      }

      override def invert(t: ScalaTry[A]): TwitterTry[A] = t match {
        case Success(value) => Return(value)
        case Failure(exception) => Throw(exception)
      }
    }
  }

  /**
   * Bijection on Try.
   * If the the bijection throws, the result will be a throw
   */
  implicit def tryBijection[A, B](implicit bij: ImplicitBijection[A, B]): Bijection[TwitterTry[A], TwitterTry[B]] =
    new AbstractBijection[TwitterTry[A], TwitterTry[B]] {
      override def apply(fa: TwitterTry[A]) = fa.map(bij(_))
      override def invert(fb: TwitterTry[B]) = fb.map(bij.invert(_))
    }

  /**
   * Bijection on scala Try.
   * If the the bijection throws, the result will be a throw
   */
  implicit def tryScalaBijection[A, B](implicit bij: ImplicitBijection[A, B]): Bijection[ScalaTry[A], ScalaTry[B]] =
    new AbstractBijection[ScalaTry[A], ScalaTry[B]] {
      override def apply(fa: ScalaTry[A]) = fa.map(bij(_))
      override def invert(fb: ScalaTry[B]) = fb.map(bij.invert(_))
    }

  /**
   * Bijection from FuturePool to ExecutionContext
   */
  implicit def futurePoolExecutionContextBijection: Bijection[FuturePool, ExecutionContext] =
    new AbstractBijection[FuturePool, ExecutionContext] {
      override def apply(pool: FuturePool) = new TwitterExecutionContext(pool)
      override def invert(context: ExecutionContext) = new ScalaFuturePool(context)
    }
}

object UtilBijections extends UtilBijections
