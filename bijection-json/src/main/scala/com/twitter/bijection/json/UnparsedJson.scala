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

package com.twitter.bijection.json

import com.twitter.bijection.Bijection

/** Value class representing unparsed Json text
 * TODO in scala 2.10 this should be a value class
 */
case class UnparsedJson(str: String)

object UnparsedJson {

  implicit def bijection[T](implicit json: JsonNodeBijection[T]): Bijection[T, UnparsedJson] =
    json andThen (JsonNodeBijection.unparsed.inverse)

  implicit val unwrap: Bijection[UnparsedJson, String] =
    new Bijection[UnparsedJson, String] {
      def apply(upj: UnparsedJson) = upj.str
      override def invert(str: String) = UnparsedJson(str)
    }
}
