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

/**
 *  Bijection between Class objects and string.
 */
object ClassBijection extends Bijection[Class[_], String] {
  override def apply(k: Class[_]) = k.getName
  override def invert(s: String) = Class.forName(s)
}

/**
 *  Bijection to cast back and forth between two types.
 *  Note that this uses casting and can fail at runtime.
 */
object CastBijection {
  def of[A, B]: Bijection[A,B] = new Bijection[A, B] {
    def apply(a: A) = a.asInstanceOf[B]
    override def invert(b: B) = b.asInstanceOf[A]
  }
}
