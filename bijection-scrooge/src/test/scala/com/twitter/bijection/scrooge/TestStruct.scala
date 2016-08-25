/**
  * generated by Scrooge 3.1-SNAPSHOT
  */
package com.twitter.bijection.scrooge

import com.twitter.scrooge.{ThriftException, ThriftStruct, ThriftStructCodec3}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object TestStruct extends ThriftStructCodec3[TestStruct] {
  val Struct = new TStruct("TestStruct")
  val SomeIntField = new TField("someInt", TType.I32, 1)
  val SomeStringField = new TField("someString", TType.STRING, 2)

  /**
    * Checks that all required fields are non-null.
    */
  def validate(_item: TestStruct) {}

  override def encode(_item: TestStruct, _oproto: TProtocol) { _item.write(_oproto) }
  override def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(someInt: Int, someString: Option[String] = None): TestStruct =
    new Immutable(someInt, someString)

  def unapply(_item: TestStruct): Option[Product2[Int, Option[String]]] = Some(_item)

  object Immutable extends ThriftStructCodec3[TestStruct] {
    override def encode(_item: TestStruct, _oproto: TProtocol) { _item.write(_oproto) }
    override def decode(_iprot: TProtocol) = {
      var someInt: Int = 0
      var _got_someInt = false
      var someString: String = null
      var _got_someString = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => {
              /* someInt */
              _field.`type` match {
                case TType.I32 => {
                  someInt = {
                    _iprot.readI32()
                  }
                  _got_someInt = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 2 => {
              /* someString */
              _field.`type` match {
                case TType.STRING => {
                  someString = {
                    _iprot.readString()
                  }
                  _got_someString = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case _ => TProtocolUtil.skip(_iprot, _field.`type`)
          }
          _iprot.readFieldEnd()
        }
      }
      _iprot.readStructEnd()
      if (!_got_someInt)
        throw new TProtocolException(
          "Required field 'TestStruct' was not found in serialized data for struct TestStruct")
      new Immutable(someInt, if (_got_someString) Some(someString) else None)
    }
  }

  /**
    * The default read-only implementation of TestStruct.  You typically should not need to
    * directly reference this class; instead, use the TestStruct.apply method to construct
    * new instances.
    */
  class Immutable(val someInt: Int, val someString: Option[String] = None) extends TestStruct

  /**
    * This Proxy trait allows you to extend the TestStruct trait with additional state or
    * behavior and implement the read-only methods from TestStruct using an underlying
    * instance.
    */
  trait Proxy extends TestStruct {
    protected def _underlying_TestStruct: TestStruct
    def someInt: Int = _underlying_TestStruct.someInt
    def someString: Option[String] = _underlying_TestStruct.someString
  }
}

trait TestStruct
    extends ThriftStruct
    with Product2[Int, Option[String]]
    with java.io.Serializable {
  import TestStruct._

  def someInt: Int
  def someString: Option[String]

  def _1 = someInt
  def _2 = someString

  override def write(_oprot: TProtocol) {
    TestStruct.validate(this)
    _oprot.writeStructBegin(Struct)
    if (true) {
      val someInt_item = someInt
      _oprot.writeFieldBegin(SomeIntField)
      _oprot.writeI32(someInt_item)
      _oprot.writeFieldEnd()
    }
    if (someString.isDefined) {
      val someString_item = someString.get
      _oprot.writeFieldBegin(SomeStringField)
      _oprot.writeString(someString_item)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(someInt: Int = this.someInt, someString: Option[String] = this.someString): TestStruct =
    new Immutable(someInt, someString)

  override def canEqual(other: Any): Boolean = other.isInstanceOf[TestStruct]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity: Int = 2

  override def productElement(n: Int): Any = n match {
    case 0 => someInt
    case 1 => someString
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix: String = "TestStruct"
}
