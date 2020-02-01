package com.kroegerama.kgen.cli

import com.kroegerama.kgen.Constants
import com.kroegerama.kgen.OptionSet
import com.kroegerama.kgen.generator.FileHelper
import com.kroegerama.kgen.generator.Generator
import com.kroegerama.kgen.openapi.OpenAPIAnalyzer
import com.kroegerama.kgen.openapi.parseSpecFile
import com.kroegerama.kgen.poet.PoetGenerator
import io.airlift.airline.Arguments
import io.airlift.airline.Command
import io.airlift.airline.Option

@Command(name = "generate", description = "Generate code from the specified OpenAPI Spec.")
class Generate : Runnable {

    @Option(name = ["-p", "--package-name"], title = "package name")
    private val packageName: String = Constants.DEFAULT_PACKAGE_NAME

    @Option(
        name = ["-o", "--output"],
        title = "output directory",
        required = true
    )
    private val output = ""

    @Arguments(
        title = "spec file",
        description = "Spec file (yaml/json). Can be a file or url.",
        required = true
    )
    private val specFile = ""

    @Option(
        name = ["-l", "--limit-apis"],
        title = "limit apis",
        description = "If set, generate only these APIs (set via tag) and their models. Comma separated list. Example: \"auth,app\""
    )
    private val limitApis = ""

    @Option(
        name = ["-v", "--verbose"],
        title = "detailed output"
    )
    private val verbose = true

    @Option(
        name = ["-d", "--dry-run"],
        title = "Dry run",
        description = "Do not create any files. Just parse and analyze."
    )
    private val dryRun = false

    @Option(
        name = ["--use-inline-class"],
        title = "Use inline class",
        description = "Use inline classes for named primitive types. Else use typealias."
    )
    private val useInlineClass = false

    override fun run() {
        val options = OptionSet(
            specFile = specFile,
            packageName = packageName,
            outputDir = output,
            limitApis = limitApis.split(",").filter { it.isNotBlank() }.toSet(),
            verbose = verbose,
            dryRun = dryRun,
            useInlineClass = useInlineClass
        )
        println("Selected options: $options")
        println()

        if (options.verbose) println("Parsing spec file...")
        val openAPI = parseSpecFile(options.specFile)

        if (options.verbose) println("Generating...")
        val analyzer = OpenAPIAnalyzer(openAPI, options)
        val poetGenerator = PoetGenerator(openAPI, options, analyzer)
        val fileHelper = FileHelper(options)
        val generator = Generator(options, poetGenerator, fileHelper, analyzer)

        generator.generate()
    }
}