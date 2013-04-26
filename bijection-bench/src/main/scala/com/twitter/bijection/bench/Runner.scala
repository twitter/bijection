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

import com.google.caliper.{ Benchmark => CaliperBenchmark, Runner => CaliperRunner }

/**
  * Objects extending Runner will inherit a Caliper-compatible main method.
  */
abstract class Runner(cls: java.lang.Class[_ <: Benchmark]) {
  def main(args:Array[String]): Unit = CaliperRunner.main(cls, args:_*)
}
