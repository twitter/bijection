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
import org.apache.avro.specific.{ SpecificDatumReader, SpecificDatumWriter, SpecificRecordBase }
import org.apache.avro.file.{ CodecFactory, DataFileStream, DataFileWriter }
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import com.twitter.bijection.Inversion.attempt
import com.twitter.bijection.Attempt
import org.apache.avro.generic.{ GenericDatumReader, GenericDatumWriter, GenericRecord }
import org.apache.avro.Schema
import org.apache.avro.io.{ DecoderFactory, DatumReader, EncoderFactory, DatumWriter }
import Injection.utf8
import scala.reflect._

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
  def apply[T <: SpecificRecordBase: ClassTag]: Injection[T, Array[Byte]] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    new SpecificAvroCodec[T](klass)
  }

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the provided codec.
   * @param codecFactory codec with which the data is being compressed
   * @tparam T compiled Avro record
   * @return Injection
   */
  def withCompression[T <: SpecificRecordBase: ClassTag](codecFactory: CodecFactory): Injection[T, Array[Byte]] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    new SpecificAvroCodec[T](klass, Some(codecFactory))
  }

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Bzip2 codec.
   * @tparam T compiled Avro record
   * @return Injection
   */
  def withBzip2Compression[T <: SpecificRecordBase: ClassTag]: Injection[T, Array[Byte]] =
    withCompression(CodecFactory.bzip2Codec())

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Deflate codec.
   * @param compressionLevel Compression level should be between 1 and 9, inclusive.  Higher values result in better
   *                         compression at the expense of encoding speed.
   * @tparam T compiled Avro record
   * @return Injection
   */
  def withDeflateCompression[T <: SpecificRecordBase: ClassTag](compressionLevel: Int): Injection[T, Array[Byte]] = {
    require(1 <= compressionLevel && compressionLevel <= 9, "Compression level should be between 1 and 9, inclusive")
    withCompression(CodecFactory.deflateCodec(compressionLevel))
  }

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Deflate codec and a default compression level of 5.
   * @tparam T compiled Avro record
   * @return Injection
   */
  // Allows to create deflate-compressing Injection's without requiring parentheses similar to `apply`,
  // `withSnappyCompression`, etc. to achieve API consistency.
  def withDeflateCompression[T <: SpecificRecordBase: ClassTag]: Injection[T, Array[Byte]] = withDeflateCompression(5)

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Snappy codec.
   * @tparam T compiled Avro record
   * @return Injection
   */
  def withSnappyCompression[T <: SpecificRecordBase: ClassTag]: Injection[T, Array[Byte]] =
    withCompression(CodecFactory.snappyCodec())

  /**
   * Returns Injection capable of serializing and deserializing a compiled avro record using org.apache.avro.io.BinaryEncoder
   * @tparam T compiled Avro record
   * @return Injection
   */
  def toBinary[T <: SpecificRecordBase: ClassTag]: Injection[T, Array[Byte]] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    val writer = new SpecificDatumWriter[T](klass)
    val reader = new SpecificDatumReader[T](klass)
    new BinaryAvroCodec[T](writer, reader)
  }

  /**
   * Returns Injection capable of serializing and deserializing a generic avro record using org.apache.avro.io.JsonEncoder to a
   * UTF-8 String
   * @tparam T compiled Avro record
   * @return Injection
   */
  def toJson[T <: SpecificRecordBase: ClassTag](schema: Schema): Injection[T, String] = {
    val klass = classTag[T].runtimeClass.asInstanceOf[Class[T]]
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
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the provided codec.
   * @param codecFactory codec with which the data is being compressed
   * @tparam T generic record
   * @return Injection
   */
  def withCompression[T <: GenericRecord: ClassTag](schema: Schema, codecFactory: CodecFactory): Injection[T, Array[Byte]] =
    new GenericAvroCodec[T](schema, Some(codecFactory))

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Bzip2 codec.
   * @tparam T generic record
   * @return Injection
   */
  def withBzip2Compression[T <: GenericRecord: ClassTag](schema: Schema): Injection[T, Array[Byte]] =
    withCompression(schema, CodecFactory.bzip2Codec())

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Deflate codec.
   * @param compressionLevel Compression level should be between 1 and 9, inclusive.  Higher values result in better
   *                         compression at the expense of encoding speed.  Default compression level is 5.
   * @tparam T generic record
   * @return Injection
   */
  def withDeflateCompression[T <: GenericRecord: ClassTag](schema: Schema, compressionLevel: Int = 5): Injection[T, Array[Byte]] = {
    require(1 <= compressionLevel && compressionLevel <= 9, "Compression level should be between 1 and 9, inclusive")
    withCompression(schema, CodecFactory.deflateCodec(compressionLevel))
  }

  /**
   * Returns Injection capable of serializing and deserializing a compiled Avro record using SpecificDatumWriter and
   * SpecificDatumReader.  Data is compressed with the Snappy codec.
   * @tparam T generic record
   * @return Injection
   */
  def withSnappyCompression[T <: GenericRecord: ClassTag](schema: Schema): Injection[T, Array[Byte]] =
    withCompression(schema, CodecFactory.snappyCodec())

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
   * Returns Injection capable of serializing and deserializing a generic avro record using org.apache.avro.io.JsonEncoder to a
   * UTF-8 String
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
class SpecificAvroCodec[T <: SpecificRecordBase](klass: Class[T], codecFactory: Option[CodecFactory] = None) extends Injection[T, Array[Byte]] {
  def apply(a: T): Array[Byte] = {
    val writer = new SpecificDatumWriter[T](a.getSchema)
    val fileWriter = new DataFileWriter[T](writer)
    codecFactory match {
      case Some(cf) => fileWriter.setCodec(cf)
      case None =>
    }
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
class GenericAvroCodec[T <: GenericRecord](schema: Schema, codecFactory: Option[CodecFactory] = None) extends Injection[T, Array[Byte]] {
  def apply(a: T): Array[Byte] = {
    val writer = new GenericDatumWriter[T](a.getSchema)
    val fileWriter = new DataFileWriter[T](writer)
    codecFactory match {
      case Some(cf) => fileWriter.setCodec(cf)
      case None =>
    }
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
 * to a UTF-8 String
 * @param writer Datum writer
 * @param reader Datum reader
 * @tparam T avro record
 * @throws RuntimeException if Avro Records cannot be converted to a UTF-8 String
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

