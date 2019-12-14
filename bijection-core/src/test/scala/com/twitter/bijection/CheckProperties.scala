package com.twitter.bijection

import org.scalatestplus.scalacheck.Checkers
import org.scalatest.propspec.AnyPropSpec

/**
  * @author Mansur Ashraf.
  */
trait CheckProperties extends PropSpec with Checkers {
  def property(testName: scala.Predef.String, testTags: org.scalatest.Tag*)(
      testFun: org.scalacheck.Prop
  ): scala.Unit =
    super.property(testName, testTags: _*) { check { testFun } }
}
