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

import com.twitter.bijection.{ AbstractBijection, Bijection }
import com.twitter.util.{ Future, Try }

/**
 * Bijection for mapping twitter-util's Future and Try onto
 * other types.
 *
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

trait UtilBijections {
  /**
   * Bijection on Future
   * if the bijection throws, the result will be a Throw.
   */
  implicit def futureBijection[A, B](implicit bij: Bijection[A, B]): Bijection[Future[A], Future[B]] =
    new AbstractBijection[Future[A], Future[B]] {
      override def apply(fa: Future[A]) = fa.flatMap { a => Future(bij(a)) }
      override def invert(fb: Future[B]) = fb.flatMap { b => Future(bij.invert(b)) }
    }

  /**
   * Bijection on Try.
   * If the the bijection throws, the result will be a throw
   */
  implicit def tryBijection[A, B](implicit bij: Bijection[A, B]): Bijection[Try[A], Try[B]] =
    new AbstractBijection[Try[A], Try[B]] {
      override def apply(fa: Try[A]) = fa.flatMap { a => Try(bij(a)) }
      override def invert(fb: Try[B]) = fb.flatMap { b => Try(bij.invert(b)) }
    }
}

object UtilBijections extends UtilBijections
