package com.riadmahi.firebase.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.riadmahi.firebase.cli.firebase.FirebaseToolsBridge
import com.riadmahi.firebase.cli.ui.KFireTerminal
import com.riadmahi.firebase.cli.ui.KFireTerminal.terminal
import kotlinx.coroutines.runBlocking

class LoginCommand : CliktCommand(name = "login") {
    override fun help(context: Context): String = "Authenticate with Firebase"

    override fun run() = runBlocking {
        KFireTerminal.logo()
        KFireTerminal.header("Firebase Authentication")

        val bridge = FirebaseToolsBridge()

        // Check if already logged in
        KFireTerminal.info("Checking authentication status...")

        if (bridge.isLoggedIn()) {
            KFireTerminal.blank()
            KFireTerminal.success("You are already logged in to Firebase")
            KFireTerminal.blank()
            KFireTerminal.box("Account Info", listOf(
                "Status: ${KFireTerminal.success("Authenticated")}",
                "To log out: ${KFireTerminal.muted("firebase logout")}"
            ))
            return@runBlocking
        }

        KFireTerminal.blank()
        KFireTerminal.warning("Not logged in to Firebase")
        KFireTerminal.blank()

        if (!KFireTerminal.confirm("Open browser to authenticate?")) {
            KFireTerminal.info("Login cancelled")
            return@runBlocking
        }

        KFireTerminal.blank()
        KFireTerminal.info("Opening browser for authentication...")
        KFireTerminal.muted("  This requires firebase-tools to be installed")
        KFireTerminal.blank()

        try {
            bridge.login()
            KFireTerminal.done("Successfully authenticated with Firebase!")

            KFireTerminal.nextSteps(listOf(
                "Run ${KFireTerminal.fire("kfire configure")} to set up your project"
            ))
        } catch (e: Exception) {
            KFireTerminal.failed("Authentication failed")
            KFireTerminal.error(e.message ?: "Unknown error")
            KFireTerminal.blank()

            KFireTerminal.box("Troubleshooting", listOf(
                "Make sure firebase-tools is installed:",
                "  ${KFireTerminal.muted("npm install -g firebase-tools")}",
                "",
                "Then try logging in manually:",
                "  ${KFireTerminal.muted("firebase login")}"
            ))
        }
    }
}
