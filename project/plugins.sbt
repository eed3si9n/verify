val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.0.0")
val scalaNativeVersion =
  Option(System.getenv("SCALANATIVE_VERSION")).getOrElse("0.3.9")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("org.scala-native" % "sbt-scala-native" % scalaNativeVersion)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.4.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.1")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.4.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.0.0")
