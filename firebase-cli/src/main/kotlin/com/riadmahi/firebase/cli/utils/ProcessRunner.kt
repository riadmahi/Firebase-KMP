package com.riadmahi.firebase.cli.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class ProcessRunner {

    data class ProcessResult(
        val exitCode: Int,
        val output: String,
        val error: String
    )

    fun run(vararg command: String, timeoutSeconds: Long = 30): ProcessResult {
        val processBuilder = ProcessBuilder(*command)
            .redirectErrorStream(false)

        val process = processBuilder.start()

        val output = StringBuilder()
        val error = StringBuilder()

        // Read output in separate threads to avoid blocking
        val outputThread = Thread {
            try {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        output.appendLine(line)
                    }
                }
            } catch (e: Exception) {
                // Stream closed, ignore
            }
        }

        val errorThread = Thread {
            try {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        error.appendLine(line)
                    }
                }
            } catch (e: Exception) {
                // Stream closed, ignore
            }
        }

        outputThread.start()
        errorThread.start()

        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        if (!completed) {
            process.destroyForcibly()
            outputThread.join(500)
            errorThread.join(500)
            return ProcessResult(-1, output.toString().trim(), "Process timed out")
        }

        outputThread.join(2000)
        errorThread.join(2000)

        return ProcessResult(
            exitCode = process.exitValue(),
            output = output.toString().trim(),
            error = error.toString().trim()
        )
    }

    fun runInteractive(vararg command: String) {
        val processBuilder = ProcessBuilder(*command)
            .inheritIO()

        val process = processBuilder.start()
        process.waitFor()
    }
}
