package com.twitter.bijection.avro

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.twitter.bijection.{Injection, BaseProperties}
import org.apache.avro.Schema
import avro.{FiscalRecordScala, FiscalRecord}

/**
  * @author
  *   Muhammad Ashraf
  * @since 10/5/13
  */
class SpecificAvroCodecsSpecification extends AnyWordSpec with Matchers with BaseProperties {
  val testSchema = new Schema.Parser().parse("""{
                                                   "type":"record",
                                                   "name":"FiscalRecord",
                                                   "namespace":"avro",
                                                   "fields":[
                                                      {
                                                         "name":"calendarDate",
                                                         "type":"string"
                                                      },
                                                      {
                                                         "name":"fiscalWeek",
                                                         "type":[
                                                            "int",
                                                            "null"
                                                         ]
                                                      },
                                                      {
                                                         "name":"fiscalYear",
                                                         "type":[
                                                            "int",
                                                            "null"
                                                         ]
                                                      }
                                                   ]
                                                }""")

  "Avro codec" should {
    "Round trip specific record using Specific Injection" in {
      implicit val specificInjection = SpecificAvroCodecs[FiscalRecord]
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[FiscalRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecord, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Specific Injection with Bzip2 compression" in {
      implicit val specificInjection = SpecificAvroCodecs.withBzip2Compression[FiscalRecord]
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[FiscalRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecord, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Specific Injection with Deflate compression (default compression level)" in {
      implicit val specificInjection = SpecificAvroCodecs.withDeflateCompression[FiscalRecord]
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[FiscalRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecord, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Specific Injection with Deflate compression (custom compression level)" in {
      implicit val specificInjection = SpecificAvroCodecs.withDeflateCompression[FiscalRecord](9)
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[FiscalRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecord, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Cannot create Specific Injection with Deflate compression if compression level is set too low" in {
      an[IllegalArgumentException] should be thrownBy SpecificAvroCodecs
        .withDeflateCompression[FiscalRecord](0)
    }

    "Cannot create Specific Injection with Deflate compression if compression level is set too high" in {
      an[IllegalArgumentException] should be thrownBy SpecificAvroCodecs
        .withDeflateCompression[FiscalRecord](10)
    }

    "Round trip specific record using Specific Injection with Snappy compression" in {
      implicit val specificInjection = SpecificAvroCodecs.withSnappyCompression[FiscalRecord]
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[FiscalRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecord, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Binary Injection" in {
      implicit val specificBinaryInjection = SpecificAvroCodecs.toBinary[FiscalRecord]
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[FiscalRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecord, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Binary Injection With Schema for Scala case classes" in {
      implicit val specificBinaryInjection = SpecificAvroCodecs.toBinary[FiscalRecordScala]
      val testRecord = FiscalRecordScala("2012-01-01", Some(1), Some(12))
      val bytes = Injection[FiscalRecordScala, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecordScala, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Binary Injection With Schema for Scala case classes with explicit schema" in {
      implicit val specificBinaryInjection =
        SpecificAvroCodecs.toBinary[FiscalRecordScala](FiscalRecordScala.SCHEMA$)
      val testRecord = FiscalRecordScala("2012-01-01", Some(1), Some(12))
      val bytes = Injection[FiscalRecordScala, Array[Byte]](testRecord)
      val attempt = Injection.invert[FiscalRecordScala, Array[Byte]](bytes)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Json Injection" in {
      implicit val specificJsonInjection = SpecificAvroCodecs.toJson[FiscalRecord]
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val jsonString = Injection[FiscalRecord, String](testRecord)
      val attempt = Injection.invert[FiscalRecord, String](jsonString)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Json Injection with explicit schema" in {
      implicit val specificJsonInjection =
        SpecificAvroCodecs.toJson[FiscalRecord](FiscalRecord.SCHEMA$)
      val testRecord = buildSpecificAvroRecord(("2012-01-01", 1, 12))
      val jsonString = Injection[FiscalRecord, String](testRecord)
      val attempt = Injection.invert[FiscalRecord, String](jsonString)
      assert(attempt.get == testRecord)
    }

    "Round trip specific record using Json Injection for Scala case classes" in {
      implicit val specificJsonInjection = SpecificAvroCodecs.toJson[FiscalRecordScala]
      val testRecord = FiscalRecordScala("2012-01-01", Some(1), Some(12))
      val jsonString = Injection[FiscalRecordScala, String](testRecord)
      val attempt = Injection.invert[FiscalRecordScala, String](jsonString)
      assert(attempt.get == testRecord)
    }
  }

  def buildSpecificAvroRecord(i: (String, Int, Int)): FiscalRecord = {
    FiscalRecord.newBuilder().setCalendarDate(i._1).setFiscalWeek(i._2).setFiscalYear(i._3).build()
  }
}
