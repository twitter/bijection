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

import org.specs.Specification
import com.twitter.bijection.{ Injection, BaseProperties }
import org.apache.avro.Schema
import avro.FiscalRecord
import org.apache.avro.generic.{ GenericData, GenericRecord }

/**
 * @author Muhammad Ashraf
 * @since 7/6/13
 */
object GenericAvroCodecsSpecification extends Specification with BaseProperties {
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

  "Generic Avro codec" should {

    "Round trip generic record using Generic Injection" in {
      implicit val genericInjection = GenericAvroCodecs[GenericRecord](testSchema)
      val testRecord = buildGenericAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[GenericRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[GenericRecord, Array[Byte]](bytes)
      attempt.get must_== testRecord
    }

    "Round trip generic record using Binary Injection" in {
      implicit val genericBinaryInjection = GenericAvroCodecs.toBinary[GenericRecord](testSchema)
      val testRecord = buildGenericAvroRecord(("2012-01-01", 1, 12))
      val bytes = Injection[GenericRecord, Array[Byte]](testRecord)
      val attempt = Injection.invert[GenericRecord, Array[Byte]](bytes)
      attempt.get must_== testRecord
    }

    "Round trip generic record using Json Injection" in {
      implicit val genericJsonInjection = GenericAvroCodecs.toJson[GenericRecord](testSchema)
      val testRecord = buildGenericAvroRecord(("2012-01-01", 1, 12))
      val jsonString = Injection[GenericRecord, String](testRecord)
      val attempt = Injection.invert[GenericRecord, String](jsonString)
      attempt.get must_== testRecord
    }
  }

  def buildGenericAvroRecord(i: (String, Int, Int)): GenericRecord = {

    val fiscalRecord = new GenericData.Record(testSchema)
    fiscalRecord.put("calendarDate", i._1)
    fiscalRecord.put("fiscalWeek", i._2)
    fiscalRecord.put("fiscalYear", i._3)
    fiscalRecord
  }
}
