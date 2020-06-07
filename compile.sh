#!/usr/bin/env bash

echo "[-->] Detect artifactId from pom.xml"
ARTIFACT=$(mvn -q \
-Dexec.executable=echo \
-Dexec.args='${project.artifactId}' \
--non-recursive \
exec:exec);
echo "artifactId is '$ARTIFACT'"

echo "[-->] Detect artifact version from pom.xml"
VERSION=$(mvn -q \
  -Dexec.executable=echo \
  -Dexec.args='${project.version}' \
  --non-recursive \
  exec:exec);
echo "artifact version is '$VERSION'"

echo "[-->] Detect Spring Boot Main class ('start-class') from pom.xml"
MAINCLASS=$(mvn -q \
-Dexec.executable=echo \
-Dexec.args='${start-class}' \
--non-recursive \
exec:exec);
echo "Spring Boot Main class ('start-class') is '$MAINCLASS'"

echo "[-->] Cleaning target directory & creating new one"
rm -rf target
mkdir -p target/native-image

echo "[-->] Build Spring Boot App with mvn package"
mvn -DskipTests package

echo "[-->] Expanding the Spring Boot fat jar"
JAR="$ARTIFACT-$VERSION.jar"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

echo "[-->] Set the classpath to the contents of the fat jar (where the libs contain the Spring Graal AutomaticFeature)"
LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

GRAALVM_VERSION=`native-image --version`
echo "[-->] Compiling Spring Boot App '$ARTIFACT' with $GRAALVM_VERSION"
time native-image \
  --verbose \
  --initialize-at-build-time=javax.el.ListELResolver,javax.el.BeanELResolver,javax.el.MapELResolver,javax.el.CompositeELResolver \
  --no-server -J-Xmx4G \
  --no-fallback \
  -H:+TraceClassInitialization \
  -H:Name=$ARTIFACT \
  -H:+ReportExceptionStackTraces \
  -Dspring.native.remove-unused-autoconfig=true \
  -Dspring.native.remove-yaml-support=true \
  -Dspring.native.remove-xml-support=true \
  -Dspring.native.remove-spel-support=true \
  -Dspring.native.remove-jmx-support=true \
  -Dspring.graal.missing-selector-hints=warning \
  -Dspring.native.dump-config=/tmp/rc.json \
  -cp $CP $MAINCLASS;