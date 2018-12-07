package dao.mongo

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

import model.{Group, VkUser}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONReader, BSONWriter, Macros}

object  Converters {
  implicit object LocalDateTimeWriter extends BSONWriter[LocalDateTime, BSONLong] {
    override def write(t: LocalDateTime): BSONLong = BSONLong(t.toInstant(ZoneOffset.UTC).toEpochMilli)
  }

  implicit object LocalDateTimeReader extends BSONReader[BSONLong, LocalDateTime] {
    override def read(t: BSONLong): LocalDateTime =
      LocalDateTime.ofInstant(Instant.ofEpochMilli(t.value), ZoneId.of("UTC"))
  }

  implicit def userWriter: BSONDocumentWriter[VkUser] = Macros.writer[VkUser]
  implicit def userReader: BSONDocumentReader[VkUser] = Macros.reader[VkUser]

  implicit object GroupWriter extends BSONDocumentWriter[Group] {
    override def write(group: Group): BSONDocument = BSONDocument(
      "id" -> group.id,
      "domain" -> group.domain,
      "name" -> group.name,
      "alias" -> group.alias,
      "fetched" -> group.fetched,
      "offset" -> group.offset,
      "users" -> group.users.map(_.map(_.id)))
  }
  implicit def groupReader: BSONDocumentReader[Group] = Macros.reader[Group]
}
