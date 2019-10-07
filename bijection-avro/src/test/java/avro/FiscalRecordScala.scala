/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package avro

case class FiscalRecordScala(
    var calendarDate: String,
    var fiscalWeek: Option[Int],
    var fiscalYear: Option[Int]
) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", Some(1), Some(1))
  def get(field: Int): AnyRef = {
    field match {
      case pos if pos == 0 => {
        calendarDate
      }.asInstanceOf[AnyRef]
      case pos if pos == 1 => {
        fiscalWeek match {
          case Some(x) => x
          case None    => null
        }
      }.asInstanceOf[AnyRef]
      case pos if pos == 2 => {
        fiscalYear match {
          case Some(x) => x
          case None    => null
        }
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field: Int, value: Any): Unit = {
    field match {
      case pos if pos == 0 =>
        this.calendarDate = {
          value match {
            case (value: org.apache.avro.util.Utf8) => value.toString
            case _                                  => value
          }
        }.asInstanceOf[String]
      case pos if pos == 1 =>
        this.fiscalWeek = {
          Option(value)
        }.asInstanceOf[Option[Int]]
      case pos if pos == 2 =>
        this.fiscalYear = {
          Option(value)
        }.asInstanceOf[Option[Int]]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = FiscalRecordScala.SCHEMA$
}

object FiscalRecordScala {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse(
    "{\"type\":\"record\",\"name\":\"FiscalRecordScala\",\"namespace\":\"avro\",\"fields\":[{\"name\":\"calendarDate\",\"type\":\"string\"},{\"name\":\"fiscalWeek\",\"type\":[\"int\",\"null\"]},{\"name\":\"fiscalYear\",\"type\":[\"int\",\"null\"]}]}"
  )
}
