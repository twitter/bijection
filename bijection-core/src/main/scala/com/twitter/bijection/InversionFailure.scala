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

import scala.util.{ Failure, Success, Try }
import scala.util.control.NonFatal

/**
 *  Factory for producing InversionFailures
 */
object InversionFailure {

  /**
   *  Produces a new InversionFailure of a given type
   */
  def apply[B](b: B): InversionFailure =
    new InversionFailure(b, new UnsupportedOperationException)

  /**
   *  Produces a failed Try
   */
  def failedAttempt[A, B](b: B): Try[A] =
    Failure(apply(b))

  /**
   * Produces a failed attempt statisfying a partial function defined
   * for any non-fatal Throwable
   */
  def partialFailure[A, B](b: B): PartialFunction[Throwable, Try[A]] = {
    case NonFatal(t) => Failure(InversionFailure(b, t))
  }
}

/**
 *  When Injection inversion attempts are known to fail,
 *  the attempt will encode an InversionFailure to represent
 *  that failure
 */
case class InversionFailure(val failed: Any, ex: Throwable)
  extends UnsupportedOperationException("Failed to invert: %s" format (failed), ex)
