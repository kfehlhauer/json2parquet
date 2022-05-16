package com.kfehlhauer.json2parquet
import zio.json.*

final case class Vehicle(
  VIN: String,
  make: String,
  model: String,
  year: Int,
  owner: String,
  isRegistered: Option[Boolean]
)

object Vehicle {
    implicit val decoder: JsonDecoder[Vehicle] = DeriveJsonDecoder.gen[Vehicle]
}
