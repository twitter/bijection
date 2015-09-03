package com.twitter.bijection.macros

import org.scalatest.Matchers

import _root_.java.io.{
  ByteArrayOutputStream,
  ByteArrayInputStream,
  Externalizable,
  ObjectInput,
  ObjectOutput,
  ObjectInputStream,
  ObjectOutputStream
}

import _root_.java.util.concurrent.atomic.{ AtomicBoolean, AtomicReference }

object MacroCaseClasses extends java.io.Serializable {
  type Atup = (Int, String)
  type Btup = (Atup, Atup, String)
  type Ctup = (Atup, Btup, Atup, Btup, Btup)

  // These are s single level unpacking into tuples
  // of the case classes below
  type Atupnr = (Int, String)
  type Btupnr = (SampleClassA, SampleClassA, String)
  type Ctupnr = (SampleClassA, SampleClassB, SampleClassA, SampleClassB, SampleClassB)

  case class SampleClassA(x: Int, y: String)
  case class SampleClassB(a1: SampleClassA, a2: SampleClassA, y: String)
  case class SampleClassC(a: SampleClassA, b: SampleClassB, c: SampleClassA, d: SampleClassB, e: SampleClassB)
  class SampleClassD // Non-case class
}

object Externalizer {
  def apply[T](t: T): Externalizer[T] = {
    val x = new Externalizer[T]
    x.set(t)
    x
  }
}

/**
 * This is a simplified version of com.twitter.chill.Externalizer
 * which only does Java serialization
 */
class Externalizer[T] extends Externalizable {
  // Either points to a result or a delegate Externalizer to fufil that result.
  private var item: Either[Externalizer[T], Option[T]] = Right(None)
  import Externalizer._

  @transient private val doesJavaWork = new AtomicReference[Option[Boolean]](None)
  @transient private val testing = new AtomicBoolean(false)

  // No vals or var's below this line!

  def getOption: Option[T] = item match {
    case Left(e) => e.getOption
    case Right(i) => i
  }

  def get: T = getOption.get // This should never be None when get is called

  /**
   * Unfortunately, Java serialization requires mutable objects if
   * you are going to control how the serialization is done.
   * Use the companion object to creat new instances of this
   */
  def set(it: T): Unit = {
    item match {
      case Left(e) => e.set(it)
      case Right(x) =>
        assert(x.isEmpty, "Tried to call .set on an already constructed Externalizer")
        item = Right(Some(it))
    }
  }

  // 1 here is 1 thread, since we will likely only serialize once
  // this should not be a val because we don't want to capture a reference

  def javaWorks: Boolean =
    doesJavaWork.get match {
      case Some(v) => v
      case None => probeJavaWorks
    }

  /**
   * Try to round-trip and see if it works without error
   */
  private def probeJavaWorks: Boolean = {
    if (!testing.compareAndSet(false, true)) return true
    try {
      val baos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(baos)
      oos.writeObject(getOption)
      val bytes = baos.toByteArray
      val testInput = new ByteArrayInputStream(bytes)
      val ois = new ObjectInputStream(testInput)
      ois.readObject // this may throw
      doesJavaWork.set(Some(true))
      true
    } catch {
      case t: Throwable =>
        t.printStackTrace
        doesJavaWork.set(Some(false))
        false
    } finally {
      testing.set(false)
    }
  }

  override def readExternal(in: ObjectInput) = readJava(in)

  private def readJava(in: ObjectInput) {
    item = Right(in.readObject.asInstanceOf[Option[T]])
  }

  protected def writeJava(out: ObjectOutput): Boolean =
    javaWorks && {
      out.writeObject(getOption)
      true
    }

  override def writeExternal(out: ObjectOutput) = writeJava(out)
}

trait MacroTestHelper extends Matchers {
  def canExternalize(t: AnyRef) { Externalizer(t).javaWorks shouldBe true }
}
