package com.twitter.bijection.avro

import avro.FiscalRecord
import com.twitter.bijection.{BaseProperties, CheckProperties, Injection}
import org.apache.avro.Schema

/**
  * @author Muhammad Ashraf
  * @since 10/5/13
  */
class SpecificAvroCodecLaws extends CheckProperties with BaseProperties {
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

  def buildSpecificAvroRecord(i: (String, Int, Int)): FiscalRecord = {
    FiscalRecord.newBuilder().setCalendarDate(i._1).setFiscalWeek(i._2).setFiscalYear(i._3).build()
  }

  implicit val testSpecificRecord = arbitraryViaFn { is: (String, Int, Int) =>
    buildSpecificAvroRecord(is)
  }

  def roundTripsSpecificRecord(implicit injection: Injection[FiscalRecord, Array[Byte]]) = {
    isLooseInjection[FiscalRecord, Array[Byte]]
  }

  def roundTripsSpecificRecordToJson(implicit injection: Injection[FiscalRecord, String]) = {
    isLooseInjection[FiscalRecord, String]
  }

  property("round trips Specific Record -> Array[Byte]") {
    roundTripsSpecificRecord(SpecificAvroCodecs[FiscalRecord])
  }

  property("round trips Specific Record -> Array[Byte] using Binary Encoder/Decoder") {
    roundTripsSpecificRecord(SpecificAvroCodecs.toBinary[FiscalRecord])
  }

  property("round trips Specific Record -> String using Json Encoder/Decoder") {
    roundTripsSpecificRecordToJson(SpecificAvroCodecs.toJson[FiscalRecord](testSchema))
  }

}
