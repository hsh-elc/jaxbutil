# jaxbutil
Utility methods to read and write XML files form Java code via JAXB


## Install jar in a local repository

In order to install the released jar file in a local maven repository, you should type something like this, where M.m.p denotes a version number:

```
curl --follow \
    -o /tmp/jaxbutil-M.m.p.jar \
    https://github.com/hsh-elc/jaxbutil/releases/download/vM.m.p/jaxbutil-M.m.p.jar

mvn deploy:deploy-file \
    -Dfile=/tmp/jaxbutil-M.m.p.jar \
    -Durl=file://path/to/projectbasedir/maven-repository
```


## Usage

When using this library from a client app, the java runtime of the client needs an implementation of JAXB-API on the classpath. Usually this is accomplished by including something like the following in the pom file of the client app:

```
    ...
    <repositories>
        <repository>
            <id>maven-repository</id>
            <url>file:///${project.basedir}/maven-repository</url>
        </repository>
    </repositories>
    ...
    <dependency>
        <groupId>de.hs-hannover</groupId>
        <artifactId>jaxbutil</artifactId>
        <version>M.m.p</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jaxb</groupId>
        <artifactId>jaxb-runtime</artifactId>
        <version>2.3.2</version>
        <scope>runtime</scope>
    </dependency>
```
