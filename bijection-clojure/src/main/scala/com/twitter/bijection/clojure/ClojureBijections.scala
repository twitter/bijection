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

package com.twitter.bijection.clojure

import com.twitter.bijection.{ AbstractBijection, Bijection }

import Bijection.asMethod // "as" syntax

/**
 * Bijections between Clojure and Scala's types.
 *
 *  @author Oscar Boykin
 *  @author Sam Ritchie
 */

trait ClojureBijections {
}

object ClojureBijections extends AlgebirdBijections
