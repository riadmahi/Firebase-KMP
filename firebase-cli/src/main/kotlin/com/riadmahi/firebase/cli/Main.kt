package com.riadmahi.firebase.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.riadmahi.firebase.cli.commands.ConfigureCommand
import com.riadmahi.firebase.cli.commands.InitCommand
import com.riadmahi.firebase.cli.commands.LoginCommand
import com.riadmahi.firebase.cli.ui.KFireTerminal

class KFireCli : CliktCommand(name = "kfire") {
    override fun help(context: com.github.ajalt.clikt.core.Context): String = """
        Firebase SDK for Kotlin Multiplatform - CLI Tool

        Configure Firebase for your KMP project with a single command.
        Similar to FlutterFire, but for Kotlin Multiplatform.
    """.trimIndent()

    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            KFireTerminal.logo()
            KFireTerminal.info("Run ${KFireTerminal.fire("kfire --help")} to see available commands")
            KFireTerminal.blank()
        }
    }
}

fun main(args: Array<String>) = KFireCli()
    .subcommands(
        InitCommand(),
        LoginCommand(),
        ConfigureCommand()
    )
    .main(args)
