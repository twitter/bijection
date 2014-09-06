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

import com.twitter.bijection.Inversion.attempt
import scala.reflect.ClassTag

/**
 *  Injection between Class objects and string.
 */
class ClassInjection[T] extends AbstractInjection[Class[T], String] {
  override def apply(k: Class[T]) = k.getName
  override def invert(s: String) = attempt(s)(Class.forName(_).asInstanceOf[Class[T]])
}

/**
 *  Injection to cast back and forth between two types.
 * WARNING: this uses java's Class.cast, which is subject to type erasure. If you have
 * a type parameterized type, like List[String] => List[Any], the cast will succeed, but
 * the inner items will not be correct. This is intended for experts.
 */
object CastInjection {
  def of[A, B >: A](implicit cmf: ClassTag[A]): Injection[A, B] = new AbstractInjection[A, B] {
    private val cls = cmf.runtimeClass.asInstanceOf[Class[A]]
    def apply(a: A) = a
    def invert(b: B) = attempt(b)(cls.cast(_))
  }
}
