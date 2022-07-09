package com.kfehlhauer.json2parquet
import zio.*
import zio.json.*
import zio.Console._
import java.io.File
import com.github.mjakubowski84.parquet4s.{ParquetReader, ParquetWriter, Path}
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.hadoop.conf.Configuration

object Main extends zio.ZIOAppDefault:
  val getData =
    ZIO.acquireReleaseWith(ZIO.attemptBlocking(io.Source.fromFile("src/main/resources/vehicles.json")))(file =>
      ZIO.attempt(file.close()).orDie
    )(file => ZIO.attemptBlocking(file.getLines().toList))

  def decodeJson(json: String) =
    ZIO.fromEither(json.fromJson[Vehicle])

  val hadoopConf = new Configuration()
  hadoopConf.set("fs.s3a.path.style.access", "true")

  val writerOptions = ParquetWriter.Options(
    compressionCodecName = CompressionCodecName.SNAPPY,
    hadoopConf = hadoopConf
  )

  def saveAsParquet(vehicles: List[Vehicle]) = ZIO.attemptBlocking(
    ParquetWriter
      .of[Vehicle]
      .options(writerOptions)
      .writeAndClose(Path("vehicles.parquet"), vehicles)
  )

  def readParquet(file: Path): Task[List[Vehicle]] =
    ZIO.acquireReleaseWith(ZIO.attemptBlocking(ParquetReader.as[Vehicle].read(file)))(file =>
      ZIO.attempt(file.close()).orDie
    )(file => ZIO.attempt(file.foldLeft(List[Vehicle]())((acc, vehicle) => acc :+ vehicle)))

  val cleanUp =
    for
      wd <- ZIO.attempt(java.lang.System.getProperty("user.dir"))
      _  <- ZIO.attemptBlocking(new File(s"${wd}/vehicles.parquet").delete)
    yield ()

  def program =
    for
      vehicleJson         <- getData
      _                   <- ZIO.attempt(vehicleJson.foreach(println)) // Display the raw JSON
      vehicles            <- ZIO.foreach(vehicleJson)(decodeJson)
      _                   <- saveAsParquet(vehicles) // Save to Parquet
      wd                  <- ZIO.attempt(java.lang.System.getProperty("user.dir"))
      vehiclesFromParquet <- readParquet(Path(s"${wd}/vehicles.parquet")) // Read back the data we just saved
      _                   <- ZIO.attempt(vehiclesFromParquet.foreach(println))              // Display the decoded Parquet
      _                   <- cleanUp
    yield ()

  def run = program
