package dao.table

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}

import slick.jdbc.PostgresProfile.api._

object Converters {
  implicit val localDateTimeType: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    dataTime => new Timestamp(dataTime.toInstant(ZoneOffset.of("UTC")).toEpochMilli),
    timestamp => LocalDateTime.ofInstant(timestamp.toInstant, ZoneOffset.of("UTC"))
  )

  implicit val urlsType: BaseColumnType[Map[Int, String]] = MappedColumnType.base[Map[Int, String], String](
    urls => urls.map(e => s"${e._1}:${e._2}").mkString(";"),
    dbData =>
      (for {
        part <- dbData.split(";")
      } yield {
        val parts = part.split(":", 2)
        parts(0).toInt -> parts(1)
      }).toMap
  )
}
