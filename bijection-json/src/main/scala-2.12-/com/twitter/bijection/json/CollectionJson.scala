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

import com.twitter.bijection.InversionFailure
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.node.JsonNodeFactory

import scala.collection.generic.CanBuildFrom
import scala.collection.JavaConverters._
import scala.util.{Success, Try}

trait CollectionJson {
  // This causes diverging implicits
  def collectionJson[T, C <: Traversable[T]](
      implicit cbf: CanBuildFrom[Nothing, T, C],
      jbij: JsonNodeInjection[T]
  ): JsonNodeInjection[C] =
    new AbstractJsonNodeInjection[C] {
      def apply(l: C) = {
        val ary = JsonNodeFactory.instance.arrayNode
        l foreach { t =>
          ary.add(jbij(t))
        }
        ary
      }
      override def invert(n: JsonNode): Try[C] = {
        val builder = cbf()
        var inCount = 0
        n.getElements.asScala.foreach { jn =>
          inCount += 1
          val thisC = jbij.invert(jn)
          if (thisC.isFailure) {
            return InversionFailure.failedAttempt(n)
          }
          builder += thisC.get
        }
        val res = builder.result
        if (res.size == inCount) Success(res) else InversionFailure.failedAttempt(n)
      }
    }
}
