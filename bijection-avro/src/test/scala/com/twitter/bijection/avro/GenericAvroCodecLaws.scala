/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.twitter.bijection.avro

import com.twitter.bijection.{BaseProperties, CheckProperties, Injection}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}

/**
  * @author Muhammad Ashraf
  * @since 7/5/13
  */
class GenericAvroCodecLaws extends CheckProperties with BaseProperties {
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

  def buildGenericAvroRecord(i: (String, Int, Int)): GenericRecord = {
    val fiscalRecord = new GenericData.Record(testSchema)
    fiscalRecord.put("calendarDate", i._1)
    fiscalRecord.put("fiscalWeek", i._2)
    fiscalRecord.put("fiscalYear", i._3)
    fiscalRecord
  }

  implicit val testGenericRecord = arbitraryViaFn { is: (String, Int, Int) =>
    buildGenericAvroRecord(is)
  }

  def roundTripsGenericRecord(implicit injection: Injection[GenericRecord, Array[Byte]]) = {
    isLooseInjection[GenericRecord, Array[Byte]]
  }

  def roundTripsGenericRecordToJson(implicit injection: Injection[GenericRecord, String]) = {
    isLooseInjection[GenericRecord, String]
  }

  property("round trips Generic Record -> Array[Byte]") {
    roundTripsGenericRecord(GenericAvroCodecs[GenericRecord](testSchema))
  }

  property("round trips Generic Record -> Array[Byte] using Binary Encoder/Decoder") {
    roundTripsGenericRecord(GenericAvroCodecs.toBinary[GenericRecord](testSchema))
  }

  property("round trips Generic Record -> String using Json Encoder/Decoder") {
    roundTripsGenericRecordToJson(GenericAvroCodecs.toJson[GenericRecord](testSchema))
  }
}
