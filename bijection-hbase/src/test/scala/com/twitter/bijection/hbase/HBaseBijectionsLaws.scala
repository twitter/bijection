package com.twitter.bijection.hbase

import org.scalatest.{ PropSpec, MustMatchers }
import org.scalatest.prop.PropertyChecks

import com.twitter.bijection.BaseProperties
import HBaseBijections._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.util.Bytes

/**
 * @author Muhammad Ashraf
 * @since 7/10/13
 */
class HBaseBijectionsLaws extends PropSpec with PropertyChecks with MustMatchers
  with BaseProperties {

  property("String <=> StringBytes") {
    isInjective[String, StringBytes]
  }

  property("Int <=> IntBytes") {
    isInjective[Int, IntBytes]
  }

  property("Long <=> LongBytes") {
    isInjective[Long, LongBytes]
  }

  property("Double <=> DoubleBytes") {
    isInjective[Double, DoubleBytes]
  }

  property("Float <=> FloatBytes") {
    isInjective[Float, FloatBytes]
  }

  property("Boolean <=> BooleanBytes") {
    isInjective[Boolean, BooleanBytes]
  }

  property("Short <=> ShortBytes") {
    isInjective[Short, ShortBytes]
  }

  property("BigDecimal <=> BigDecimalBytes") {
    isInjective[BigDecimal, BigDecimalBytes]
  }

  property("BigDecimal <=> ImmutableBytesWritable") {
    implicit val arbBD = arbitraryViaFn {
      bd: BigDecimal => new ImmutableBytesWritable(Bytes.toBytes(bd.underlying()))
    }
    isBijection[BigDecimal, ImmutableBytesWritable]
  }

  property("String <=> ImmutableBytesWritable") {
    implicit val arbString = arbitraryViaFn {
      input: String => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[String, ImmutableBytesWritable]
  }
  property("Long <=> ImmutableBytesWritable") {
    implicit val arbLong = arbitraryViaFn {
      input: Long => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[Long, ImmutableBytesWritable]
  }
  property("Int <=> ImmutableBytesWritable") {
    implicit val arbInt = arbitraryViaFn {
      input: Int => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[Int, ImmutableBytesWritable]
  }
  property("Double <=> ImmutableBytesWritable") {
    implicit val arbDouble = arbitraryViaFn {
      input: Double => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[Double, ImmutableBytesWritable]
  }
  property("Float <=> ImmutableBytesWritable") {
    implicit val arbFloat = arbitraryViaFn {
      input: Float => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[Float, ImmutableBytesWritable]
  }
  property("Short <=> ImmutableBytesWritable") {
    implicit val arbShort = arbitraryViaFn {
      input: Short => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[Short, ImmutableBytesWritable]
  }
  property("Boolean <=> ImmutableBytesWritable") {
    implicit val arbBoolean = arbitraryViaFn {
      input: Boolean => new ImmutableBytesWritable(Bytes.toBytes(input))
    }
    isBijection[Boolean, ImmutableBytesWritable]
  }

}
