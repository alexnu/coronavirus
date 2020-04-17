name := "Coronavirus Spark"
version := "1.0"
scalaVersion := "2.11.12"

// The dependencies are in Maven format, with % separating the parts.
// Notice the extra bit "test" on the end of JUnit and ScalaTest, which will
// mean it is only a test dependency.
//
// The %% means that it will automatically add the specific Scala version to the dependency name.

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5" % Provided
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5" % Provided
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test
libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "2.4.5_0.14.0" % Test
