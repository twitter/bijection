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

package com.twitter.bijection.scrooge

import org.apache.thrift.TException
import org.apache.thrift.transport.TTransport
import org.apache.thrift.protocol.{
  TBinaryProtocol => ThriftBinary,
  TList,
  TMap,
  TSet,
  TType,
  TProtocolFactory,
  TProtocol
}

/**
  * Binary protocol implementation for thrift. Copied from Raghu's fix
  * in Elephant-Bird.
  */
object TBinaryProtocol {
  /**
    * Factory implementation that returns the updated TBinaryProtocol.
    */
  class Factory(strictRead: Boolean, strictWrite: Boolean, readLength: Int)
      extends TProtocolFactory {
    def this(strictRead: Boolean, strictWrite: Boolean) =
      this(strictRead, strictWrite, 0)

    def this() = this(false, true, 0)

    override def getProtocol(trans: TTransport): TProtocol =
      new TBinaryProtocol(trans, strictRead, strictWrite)
  }
}

class TBinaryProtocol(trans: TTransport, strictRead: Boolean, strictWrite: Boolean)
    extends ThriftBinary(trans, strictRead, strictWrite) {
  // overwrite a few methods so that some malformed messages don't end
  // up taking prohibitively large amounts of cpu in side
  // TProtocolUtil.skip()
  private def checkElemType(byte: Byte) {
    byte match {
      // only valid types for an element in a container (List, Map, Set)
      // are the ones that are considered in TProtocolUtil.skip()
      case (TType.BOOL | TType.BYTE | TType.I16 | TType.I32 | TType.I64 | TType.DOUBLE |
          TType.STRING | TType.STRUCT | TType.MAP | TType.SET | TType.LIST) =>
        ()

      // list other known types, but not expected
      case (TType.STOP | TType.VOID | TType.ENUM // would be I32 on the wire
          ) =>
        ()
      case _ => throw new TException("Unexpected type " + byte + " in a container");
    }
  }

  override def readMapBegin: TMap = {
    val map = super.readMapBegin
    checkElemType(map.keyType)
    checkElemType(map.valueType)
    map
  }

  override def readListBegin: TList = {
    val list = super.readListBegin
    checkElemType(list.elemType)
    list
  }

  override def readSetBegin: TSet = {
    val set = super.readSetBegin
    checkElemType(set.elemType)
    set
  }
}
