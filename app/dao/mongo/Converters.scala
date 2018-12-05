package dao.mongo

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

import reactivemongo.bson.{BSONLong, BSONReader, BSONWriter}

object Converters {
  implicit object LocalDateTimeWriter extends BSONWriter[LocalDateTime, BSONLong] {
    override def write(t: LocalDateTime): BSONLong = BSONLong(t.toInstant(ZoneOffset.UTC).toEpochMilli)
  }

  implicit object LocalDateTimeReader extends BSONReader[BSONLong, LocalDateTime] {
    override def read(t: BSONLong): LocalDateTime =
      LocalDateTime.ofInstant(Instant.ofEpochMilli(t.value), ZoneId.of("UTC"))
  }
}
