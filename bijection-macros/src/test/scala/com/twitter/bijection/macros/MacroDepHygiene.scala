package com.twitter.bijection.macros

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.exceptions.TestFailedException

/**
  * This test is intended to ensure that the macros do not require any imported code in scope. This is why all
  * references are via absolute paths.
  */
class MacroDepHygiene extends WordSpec with Matchers with MacroTestHelper {
  import com.twitter.bijection.macros.MacroImplicits.isCaseClass
  import MacroCaseClasses._

  "IsCaseClass macro" should {
    val dummy = new com.twitter.bijection.macros.IsCaseClass[Nothing] {}
    def isCaseClassAvailable[T](implicit
        proof: com.twitter.bijection.macros.IsCaseClass[T] =
          dummy.asInstanceOf[com.twitter.bijection.macros.IsCaseClass[T]]
    ) {
      proof shouldBe a[MacroGenerated]
      canExternalize(proof)
    }

    "work fine without any imports" in {
      isCaseClassAvailable[SampleClassA]
      isCaseClassAvailable[SampleClassB]
    }

    "fail if not available" in {
      a[TestFailedException] should be thrownBy isCaseClassAvailable[SampleClassD]
    }
  }
}
