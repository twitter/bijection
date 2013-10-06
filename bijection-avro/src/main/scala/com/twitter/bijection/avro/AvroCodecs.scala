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

import com.twitter.bijection.Injection
import org.apache.avro.specific.{SpecificDatumReader, SpecificDatumWriter, SpecificRecordBase}
import org.apache.avro.file.{DataFileStream, DataFileWriter}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import com.twitter.bijection.Inversion.attempt
import com.twitter.bijection.Attempt
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.Schema
import org.apache.avro.io.{DecoderFactory, DatumReader, EncoderFactory, DatumWriter}
import Injection.utf8

/**
 * Factory providing various avro injections.
 * @author Muhammad Ashraf
 * @since 7/4/13
 */
object SpecificAvroCodecs {
  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader
   * @tparam T compiled Avro record
   * @return Injection
   */
  def apply[T <: SpecificRecordBase : Manifest]: Injection[T, Array[Byte]] = {
    val klass = manifest[T].erasure.asInstanceOf[Class[T]]
    new SpecificAvroCodec[T](klass)
  }

  /**
   * Returns Injection capable of serializing and deserializing a compiled avro record using org.apache.avro.io.BinaryEncoder
   * @tparam T compiled Avro record
   * @return Injection
   */
  def toBinary[T <: SpecificRecordBase : Manifest]: Injection[T, Array[Byte]] = {
    val klass = manifest[T].erasure.asInstanceOf[Class[T]]
    val writer = new SpecificDatumWriter[T](klass)
    val reader = new SpecificDatumReader[T](klass)
    new BinaryAvroCodec[T](writer, reader)
  }

  /**
   * Returns Injection capable of serializing and deserializing a generic avro record using org.apache.avro.io.JsonEncoder
   * @tparam T compiled Avro record
   * @return Injection
   */
  def toJson[T <: SpecificRecordBase : Manifest](schema: Schema): Injection[T, String] = {
    val klass = manifest[T].erasure.asInstanceOf[Class[T]]
    val writer = new SpecificDatumWriter[T](klass)
    val reader = new SpecificDatumReader[T](klass)
    new JsonAvroCodec[T](schema, writer, reader)
  }
}

object GenericAvroCodecs {
  /**
   * Returns Injection capable of serializing and deserializing a generic record using GenericDatumReader and
   * GenericDatumReader
   * @tparam T generic record
   * @return Injection
   */
  def apply[T <: GenericRecord](schema: Schema): Injection[T, Array[Byte]] = {
    new GenericAvroCodec[T](schema)
  }

  /**
   * Returns Injection capable of serializing and deserializing a generic avro record using org.apache.avro.io.BinaryEncoder
   * @tparam T GenericRecord
   * @return Injection
   */
  def toBinary[T <: GenericRecord](schema: Schema): Injection[T, Array[Byte]] = {
    val writer = new GenericDatumWriter[T](schema)
    val reader = new GenericDatumReader[T](schema)
    new BinaryAvroCodec[T](writer, reader)
  }

  /**
   * Returns Injection capable of serializing and deserializing a compiled avro record using org.apache.avro.io.JsonEncoder
   * @tparam T compiled Avro record
   * @return Injection
   */
  def toJson[T <: GenericRecord](schema: Schema): Injection[T, String] = {
    val writer = new GenericDatumWriter[T](schema)
    val reader = new GenericDatumReader[T](schema)
    new JsonAvroCodec[T](schema, writer, reader)
  }
}

/**
 * Provides methods to serialize and deserialize complied avro record.
 * @param klass class of complied record
 * @tparam T compiled record
 */
class SpecificAvroCodec[T <: SpecificRecordBase](klass: Class[T]) extends Injection[T, Array[Byte]] {
  def apply(a: T): Array[Byte] = {
    val writer = new SpecificDatumWriter[T](a.getSchema)
    val fileWriter = new DataFileWriter[T](writer)
    val stream = new ByteArrayOutputStream()
    fileWriter.create(a.getSchema, stream)
    fileWriter.append(a)
    fileWriter.flush()
    stream.toByteArray
  }

  def invert(bytes: Array[Byte]): Attempt[T] = attempt(bytes) {
    bytes =>
      val reader = new SpecificDatumReader[T](klass)
      val stream = new DataFileStream[T](new ByteArrayInputStream(bytes), reader)
      val result = stream.next()
      stream.close()
      result
  }
}

/**
 * Provides methods to serialize and deserialize generic avro record.
 * @param schema avro schema
 * @tparam T generic record
 */
class GenericAvroCodec[T <: GenericRecord](schema: Schema) extends Injection[T, Array[Byte]] {
  def apply(a: T): Array[Byte] = {
    val writer = new GenericDatumWriter[T](a.getSchema)
    val fileWriter = new DataFileWriter[T](writer)
    val stream = new ByteArrayOutputStream()
    fileWriter.create(a.getSchema, stream)
    fileWriter.append(a)
    fileWriter.flush()
    stream.toByteArray
  }

  def invert(bytes: Array[Byte]): Attempt[T] = attempt(bytes) {
    bytes =>
      val reader = new GenericDatumReader[T](schema)
      val stream = new DataFileStream[T](new ByteArrayInputStream(bytes), reader)
      val result = stream.next()
      stream.close()
      result
  }
}

/**
 * Provides methods to serializing and deserializing a generic and compiled avro record using org.apache.avro.io.BinaryEncoder
 * @param writer Datum writer
 * @param reader Datum reader
 * @tparam T avro record
 */
class BinaryAvroCodec[T](writer: DatumWriter[T], reader: DatumReader[T]) extends Injection[T, Array[Byte]] {
  def apply(a: T): Array[Byte] = {
    val stream = new ByteArrayOutputStream()
    val binaryEncoder = EncoderFactory.get().binaryEncoder(stream, null)
    writer.write(a, binaryEncoder)
    binaryEncoder.flush()
    stream.toByteArray
  }

  def invert(bytes: Array[Byte]): Attempt[T] = attempt(bytes) {
    bytes =>
      val binaryDecoder = DecoderFactory.get().binaryDecoder(bytes, null)
      reader.read(null.asInstanceOf[T], binaryDecoder)
  }
}

/**
 * Provides methods to serializing and deserializing a generic and compiled avro record using org.apache.avro.io.JsonEncoder
 * @param writer Datum writer
 * @param reader Datum reader
 * @tparam T avro record
 */
class JsonAvroCodec[T](schema: Schema, writer: DatumWriter[T], reader: DatumReader[T]) extends Injection[T, String] {
  def apply(a: T): String = {
    val stream = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().jsonEncoder(schema, stream)
    writer.write(a, encoder)
    encoder.flush()
    Injection.invert[String, Array[Byte]](stream.toByteArray).get
  }

  def invert(str: String): Attempt[T] = attempt(str) {
    str =>
      val decoder = DecoderFactory.get().jsonDecoder(schema, new ByteArrayInputStream(Injection[String, Array[Byte]](str)))
      reader.read(null.asInstanceOf[T], decoder)
  }
}

