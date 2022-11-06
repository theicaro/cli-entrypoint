import com.google.gson.Gson
import java.io.File
import kotlin.system.exitProcess

class SemVerComparator : Comparator<String> {
    private fun getVersionAsList(release: String): List<String> {
        var (major, minor, patch, preReleaseType, preReleaseNumber) = "(\\d).(\\d).?(\\d)?-?(\\w*).?(\\d)?".toRegex()
            .find(release)!!.destructured.toList()

        if (patch == "") patch = "0"
        if (preReleaseType == "") preReleaseType = "zeta"
        if (preReleaseNumber == "") preReleaseNumber = "0"

        return listOf(major, minor, patch, preReleaseType, preReleaseNumber)
    }

    override fun compare(o1: String, o2: String): Int {
        for ((currentNumberO1, currentNumberO2) in getVersionAsList(o1).zip(getVersionAsList(o2))) {
            if (currentNumberO1 < currentNumberO2) return -1

            if (currentNumberO1 > currentNumberO2) return 1
        }

        return 0
    }
}

fun getLastVersionInstalled(): String =
    File("$ICARO_HOME/cli/core").listFiles().map { it.name }.maxWith(SemVerComparator()) 

fun getCliVersion(): String {
    if (!File("deps.json").isFile) return getLastVersionInstalled()

    val dependencies = Gson().fromJson(File("deps.json").readText(), Map::class.java)

    return dependencies["cliVersion"].toString()
}

fun main(args: Array<String>) {
    try {
        val cliPath = "$ICARO_HOME/cli/core/${getCliVersion()}.jar"

        val cliProcess = ProcessBuilder(listOf("java", "-jar", cliPath) + args).start()

        val cliOutput = String(cliProcess.inputStream.readAllBytes()) + String(cliProcess.errorStream.readAllBytes())

        println(cliOutput)
    } catch (e: Throwable) {
        exitProcess(1)
    }
}