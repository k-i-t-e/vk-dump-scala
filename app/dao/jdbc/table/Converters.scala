package dao.jdbc.table

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}

import model.ImageType
import model.ImageType.ImageType
import slick.jdbc.PostgresProfile.api._

object Converters {
  implicit val localDateTimeType: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    dataTime => new Timestamp(dataTime.toInstant(ZoneOffset.UTC).toEpochMilli),
    timestamp => LocalDateTime.ofInstant(timestamp.toInstant, ZoneOffset.UTC)
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

  implicit val imageTypeType: BaseColumnType[ImageType] = MappedColumnType.base[ImageType, Int](
    t => t.id,
    code => ImageType(code)
  )
}
