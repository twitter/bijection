package com.twitter.bijection

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

import scala.util.{ Success, Try }
import com.twitter.bijection.InversionFailure.{ failedAttempt, partialFailure }
/**
 * Factory for applying inversion attempts
 */
object Inversion {
  /**
   *  The analog of Exception.allCatch either where exceptions
   *  are wrapped by the InversionFailure type
   */
  def attempt[A, B](b: B)(inv: B => A): Try[A] =
    Try(inv(b)).recoverWith(partialFailure(b))

  /**
   * Applies tests for known inversion failure before returning
   * a success or failure
   */
  def attemptWhen[A, B](b: B)(test: B => Boolean)(inv: B => A): Try[A] =
    if (test(b)) Success(inv(b)) else failedAttempt(b)
}
