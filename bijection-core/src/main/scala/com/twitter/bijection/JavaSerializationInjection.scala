/*
Copyright 2013 Twitter, Inc.

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

import java.io._
import scala.util.{ Failure, Try }
import scala.util.control.Exception.allCatch
import com.twitter.bijection.Inversion.attempt
import scala.reflect.ClassTag

object JavaSerializationInjection extends Serializable {
  /**
   * Construct a new JavaSerializationInjection instance
   */
  def apply[T <: Serializable](implicit ct: ClassTag[T]): JavaSerializationInjection[T] = {
    val cls = ct.runtimeClass.asInstanceOf[Class[T]]
    new JavaSerializationInjection[T](cls)
  }
}

/**
 * Use Java serialization to write/read bytes.
 * We avoid manifests here to make it easier from Java
 */
class JavaSerializationInjection[T <: Serializable](klass: Class[T]) extends Injection[T, Array[Byte]] {
  def apply(t: T) = {
    val bos = new ByteArrayOutputStream
    val out = new ObjectOutputStream(bos)
    try {
      out.writeObject(t)
      bos.toByteArray
    } finally {
      out.close
      bos.close
    }
  }
  def invert(bytes: Array[Byte]) = {
    val bis = new ByteArrayInputStream(bytes)
    val inOpt = Try(new ObjectInputStream(bis))
    try {
      inOpt.map { in => klass.cast(in.readObject) }.recoverWith(InversionFailure.partialFailure(bytes))
    } catch {
      case t: Throwable => Failure(InversionFailure(bytes, t))
    } finally {
      bis.close
      inOpt.map { _.close }
    }
  }
}
