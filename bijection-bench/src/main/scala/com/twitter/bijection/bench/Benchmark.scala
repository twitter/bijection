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

package com.twitter.bijection.bench

import com.google.caliper.{ Param, SimpleBenchmark }

/**
 * Extend Benchmark to gain some nice convenience methods.
 */
trait Benchmark extends SimpleBenchmark {
  // helper method to keep the actual benchmarking methods a bit
  // cleaner your code snippet should always return a value that
  // cannot be "optimized away"
  def repeat[@specialized A](reps: Int)(snippet: => A) = {
    // looks weird but does what it should: init w/ default value in a fully generic way
    val zero = 0.asInstanceOf[A]
    var i = 0
    var result = zero
    while (i < reps) {
      val res = snippet
      if (res != zero) result = res // make result depend on the benchmarking snippet result
      i = i + 1
    }
    result
  }
}

object BasicBenchmark extends Runner(classOf[BasicBenchmark])

class BasicBenchmark extends Benchmark {
 // to make your benchmark depend on one or more parameterized values,
  // create fields with the name you want the parameter to be known
  // by, and add this annotation (see @Param javadocs for more
  // details) caliper will inject the respective value at runtime and
  // make sure to run all combinations
  @Param(Array("10", "100", "1000", "10000"))
  val length: Int = 0

  var array: Array[Int] = _

  override def setUp() {
    // set up all your benchmark data here
    array = new Array(length)
  }

  // the actual code you'd like to test needs to live in one or more
  // methods whose names begin with 'time' and which accept a single
  // 'reps: Int' parameter the body of the method simply executes the
  // code we wish to measure, 'reps' times you can use the 'repeat'
  // method from the SimpleScalaBenchmark trait to repeat with
  // relatively low overhead however, if your code snippet is very
  // fast you might want to implement the reps loop directly with
  // 'while'
  def timeForeach(reps: Int) = repeat(reps) {
    var result = 0
    array.foreach(result += _)
    result // always have your snippet return a value that cannot easily be "optimized away"
  }
}
