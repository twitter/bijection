package com.twitter.bijection

import java.util._
import com.github.nscala_time.time._
import com.github.nscala_time.time.Imports._

trait DateBijections extends GeneratedTupleBijections {

  
  implicit val date2joda: Bijection[java.util.Date, DateTime] =
    new AbstractBijection[java.util.Date, DateTime] {
      def apply(d: java.util.Date) = new DateTime(d)
      override def invert(joda: DateTime) = joda.toDate()
    }

   
}