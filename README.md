
[![License](https://img.shields.io/github/license/eitco/html-maven-plugin.svg?style=for-the-badge)](https://opensource.org/license/mit)
[![Build status](https://img.shields.io/github/actions/workflow/status/eitco/html-maven-plugin/deploy.yaml?branch=main&style=for-the-badge&logo=github)](https://github.com/eitco/html-maven-plugin/actions/workflows/deploy.yaml)
[![Maven Central Version](https://img.shields.io/maven-central/v/de.eitco.cicd.html/html-maven-plugin?style=for-the-badge&logo=apachemaven)](https://central.sonatype.com/artifact/de.eitco.cicd.html/html-maven-plugin)

# html-maven-plugin

This maven plugin adds a build lifecycle to build and deploy html sites from asciidoc and markdown files. It 
deploys the generated html either as a directory - on file after another - or as zip file.

# usage

To add this plugin to your build add the following to your pom:

```xml
...
<build>
    <plugins>
        <plugin>
            <groupId>de.eitco.cicd</groupId>
            <artifactId>html-maven-plugin</artifactId>
            <version>4.0.1</version>
            <extensions>true</extensions>
        </plugin>
    </plugins>
</build>
```

Now you can activate one of the lifecycles this plugin provides. To generate a directory containing your 
generated html files and resources use the `html` lifecycle:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>your.group.id</groupId>
    <artifactId>your-artifact-id</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>html</packaging>
...
</project>
```

To generate a zip containing your generated html files and resources us the `html-zip` lifecycle 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>your.group.id</groupId>
    <artifactId>your-artifact-id</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>html-zip</packaging>
...
</project>
```

 * A complete reference about the goals and parameters of this plugin can be found [here](https://eitco.github.io/html-maven-plugin/plugin-info.html).
 * The [integration tests](tree/main/src/it/) provide some examples about how to use this plugin


