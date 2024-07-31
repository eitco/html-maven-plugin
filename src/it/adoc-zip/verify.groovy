
File baseDirectory = new File("$basedir");


assert new File(baseDirectory, "target/html/index.html").isFile()
assert new File(baseDirectory, "target/cicd-html-simple-test-1.0.0-SNAPSHOT.zip").isFile()

