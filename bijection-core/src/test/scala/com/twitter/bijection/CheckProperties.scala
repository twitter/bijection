package com.twitter.bijection

import org.scalatest.PropSpec
import org.scalatest.prop.Checkers

/**
  * @author Mansur Ashraf.
  */
trait CheckProperties extends PropSpec with Checkers {

  def property(testName: scala.Predef.String, testTags: org.scalatest.Tag*)(
      testFun: org.scalacheck.Prop): scala.Unit =
    super.property(testName, testTags: _*) { check { testFun } }
}
