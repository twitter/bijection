package com.twitter.bijection

import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.Checkers

/**
  * @author
  *   Mansur Ashraf.
  */
trait CheckProperties extends AnyPropSpec with Checkers {
  def property(testName: scala.Predef.String, testTags: org.scalatest.Tag*)(
      testFun: org.scalacheck.Prop
  ): scala.Unit =
    super.property(testName, testTags: _*) { check { testFun } }
}
