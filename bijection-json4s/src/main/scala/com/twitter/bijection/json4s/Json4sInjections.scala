/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.twitter.bijection.json4s

import org.json4s._
import com.twitter.bijection.{ Injection, AbstractInjection }
import org.json4s.native.JsonMethods._
import scala.util.Try
import com.twitter.bijection.Inversion._
import org.json4s.native.Serialization._
import scala.reflect.runtime.universe.TypeTag
import scala.reflect.ClassTag

/**
 * @author Mansur Ashraf
 * @since 1/10/14
 */
object Json4sInjections {

  /**
   * JValue to Json Injection
   */
  implicit val jvalue2Json: Injection[JValue, String] = new AbstractInjection[JValue, String] {
    override def apply(a: JValue): String = compact(render(a))

    override def invert(b: String): Try[JValue] = attempt(b)(parse(_))
  }

  /**
   * Case Class to Json Injection
   * @tparam A Case Class
   * @return Json String
   */
  implicit def caseClass2Json[A <: AnyRef](implicit tt: TypeTag[A], ct: ClassTag[A], fmt: Formats): Injection[A, String] = new AbstractInjection[A, String] {
    override def apply(a: A): String = write(a)

    override def invert(b: String): Try[A] = attempt(b)(read[A])
  }

  /**
   * Case Class to JValue Injection
   * @tparam A Case Class
   * @return JValue
   */
  implicit def caseClass2JValue[A <: AnyRef](implicit tt: TypeTag[A], ct: ClassTag[A], fmt: Formats): Injection[A, JValue] = new AbstractInjection[A, JValue] {
    override def apply(a: A): JValue = Extraction.decompose(a)

    override def invert(b: JValue): Try[A] = attempt(b)(_.extract[A])
  }
}
