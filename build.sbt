name         := "json2parquet"
version      := "1.0"
scalaVersion := "3.1.1"

libraryDependencies ++= Seq(
  "dev.zio"                  %% "zio"            % "2.0.0-RC6",
  "dev.zio"                  %% "zio-json"       % "0.3.0-RC8",
  "com.github.mjakubowski84" %% "parquet4s-core" % "2.5.1",
  "org.apache.hadoop"         % "hadoop-client"  % "3.2.1",
  "org.xerial.snappy" % "snappy-java" % "1.1.8.4"
)
