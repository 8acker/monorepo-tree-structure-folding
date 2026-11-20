project.tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
}

project.tasks.withType(Test::class.java).configureEach {
    systemProperty("file.encoding", "UTF-8")
}