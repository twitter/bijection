/*
 * Copyright 2010 Twitter Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.bijection.guava

import com.google.common.base.Optional
import com.twitter.bijection.{ @@, BaseProperties, Bijection, Rep }
import com.twitter.bijection.Rep._

import org.scalacheck.Properties
import org.scalacheck.Arbitrary

import java.lang.{ Long => JLong }

object GuavaBijectionLaws extends Properties("GuavaBijections") with BaseProperties {
  import GuavaBijections._

  property("round trips Option[Int] -> Optional[String @@ Rep[Int]]") =
    roundTrips[Option[Int], Optional[String @@ Rep[Int]]]()

  property("round trips Option[Long] -> Optional[JLong]") =
    roundTrips[Option[Long], Optional[JLong]]()
}
