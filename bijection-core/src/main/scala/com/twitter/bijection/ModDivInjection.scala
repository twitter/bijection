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

/** A common injection on numbers: N -> (m = N mod K, (N-m)/K)
 * The first element in result tuple is always [0, modulus)
 */
class IntModDivInjection(val modulus: Int) extends Injection[Int, (Int,Int)] {
  override def apply(n: Int) = {
    val cmod = n % modulus
    val mod = if (cmod < 0) cmod + modulus else cmod
    (mod, (n - mod)/modulus)
  }

  private val maxDiv = Int.MaxValue / modulus
  private val minDiv = Int.MinValue / modulus

  override def invert(moddiv: (Int,Int)) = {
    val (mod, div) = moddiv
    if (mod >= 0 && mod < modulus && div <= maxDiv && div >= minDiv) {
      Right(div * modulus + mod)
    }
    else InversionFailure.failedAttempt(moddiv)
  }
}


/** A common injection on numbers: N -> (m = N mod K, (N-m)/K)
 * The first element in result tuple is always [0, modulus)
 */
class LongModDivInjection(val modulus: Long) extends Injection[Long, (Long,Long)] {
  override def apply(n: Long) = {
    val cmod = n % modulus
    val mod = if (cmod < 0) cmod + modulus else cmod
    (mod, (n - mod)/modulus)
  }

  private val maxDiv = Long.MaxValue / modulus
  private val minDiv = Long.MinValue / modulus

  override def invert(moddiv: (Long,Long)) = {
    val (mod, div) = moddiv
    if (mod >= 0 && mod < modulus && div <= maxDiv && div >= minDiv) {
      Right(div * modulus + mod)
    }
    else InversionFailure.failedAttempt(moddiv)
  }
}
