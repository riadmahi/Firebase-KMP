package com.riadmahi.firebase.cli.ui

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.keymap.BindingReader
import org.jline.keymap.KeyMap
import org.jline.utils.InfoCmp
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

object KFireTerminal {
    val terminal = Terminal()

    // JLine terminal for raw input
    private val jlineTerminal by lazy {
        TerminalBuilder.builder()
            .system(true)
            .jansi(true)
            .build()
    }

    // ANSI escape codes
    private const val ESC = "\u001B"
    private const val HIDE_CURSOR = "$ESC[?25l"
    private const val SHOW_CURSOR = "$ESC[?25h"
    private const val CLEAR_LINE = "$ESC[2K"
    private const val MOVE_UP = "$ESC[1A"
    private const val SAVE_CURSOR = "$ESC[s"
    private const val RESTORE_CURSOR = "$ESC[u"

    // Brand colors
    private val fireColors = listOf(
        "\u001B[38;2;255;202;40m",   // Firebase yellow #FFCA28
        "\u001B[38;2;255;167;38m",   // Firebase orange #FFA726
        "\u001B[38;2;255;138;0m",    // Deeper orange #FF8A00
    )
    private val kotlinPurple = "\u001B[38;2;127;82;255m"  // Kotlin purple #7F52FF
    private val reset = "\u001B[0m"

    // Gradient text helper
    fun gradient(text: String, colors: List<String> = fireColors): String {
        if (text.isEmpty()) return text
        val result = StringBuilder()
        text.forEachIndexed { index, char ->
            val colorIndex = (index * colors.size / text.length).coerceIn(0, colors.lastIndex)
            result.append(colors[colorIndex]).append(char)
        }
        result.append(reset)
        return result.toString()
    }

    fun fire(text: String): String = "\u001B[38;2;255;202;40m$text$reset"
    fun kotlin(text: String): String = "$kotlinPurple$text$reset"
    fun success(text: String): String = green(text)
    fun error(text: String): String = red(text)
    fun warning(text: String): String = yellow(text)
    fun info(text: String): String = cyan(text)
    fun muted(text: String): String = gray(text)
    fun subtle(text: String): String = "\u001B[38;2;100;100;100m$text$reset"

    // ═══════════════════════════════════════════════════════════════════
    // LOGO & BRANDING
    // ═══════════════════════════════════════════════════════════════════

    fun logo() {
        terminal.println()
        val fireArt = """
            │      ${gradient("🔥")}       │
            │   ${bold(gradient("K F i r e"))}   │
        """.trimIndent()

        terminal.println(fire("    ┌─────────────────┐"))
        terminal.println(fire("    │") + "      🔥         " + fire("│"))
        terminal.println(fire("    │") + "   " + bold(gradient("K F i r e")) + "     " + fire("│"))
        terminal.println(fire("    │") + subtle("  Firebase + KMP ") + fire("│"))
        terminal.println(fire("    └─────────────────┘"))
        terminal.println()
    }

    fun welcomeBanner() {
        terminal.println()
        val width = 60
        val border = gradient("═".repeat(width))

        terminal.println("  $border")
        terminal.println()
        terminal.println("  " + bold("Welcome to ") + gradient("KFire") + bold(" — Firebase for Kotlin Multiplatform"))
        terminal.println()
        terminal.println("  " + muted("This wizard will guide you through setting up Firebase"))
        terminal.println("  " + muted("in your KMP project. Let's get started!"))
        terminal.println()
        terminal.println("  $border")
        terminal.println()
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP PROGRESS
    // ═══════════════════════════════════════════════════════════════════

    data class WizardStep(
        val number: Int,
        val title: String,
        val description: String = ""
    )

    fun stepIndicator(steps: List<WizardStep>, currentStep: Int) {
        terminal.println()

        steps.forEachIndexed { index, step ->
            val isCompleted = index < currentStep
            val isCurrent = index == currentStep
            val isPending = index > currentStep

            val (bullet, titleStyle) = when {
                isCompleted -> Pair(green("✓"), { s: String -> muted(s) })
                isCurrent -> Pair(gradient("●"), { s: String -> bold(s) })
                else -> Pair(subtle("○"), { s: String -> subtle(s) })
            }

            val stepNum = if (isCurrent) fire("${step.number}") else muted("${step.number}")

            terminal.println("  $bullet $stepNum  ${titleStyle(step.title)}")

            // Show description only for current step
            if (isCurrent && step.description.isNotEmpty()) {
                terminal.println("       " + subtle(step.description))
            }

            // Connector line
            if (index < steps.lastIndex) {
                val connector = if (isCompleted) green("│") else subtle("│")
                terminal.println("     $connector")
            }
        }
        terminal.println()
    }

    fun currentStep(number: Int, total: Int, title: String) {
        terminal.println()
        terminal.println(gradient("━".repeat(60)))
        terminal.println()
        terminal.println("  " + subtle("Step $number of $total"))
        terminal.println("  " + bold(title))
        terminal.println()
    }

    // ═══════════════════════════════════════════════════════════════════
    // ANIMATED SPINNER
    // ═══════════════════════════════════════════════════════════════════

    class Spinner(private val message: String) {
        private val frames = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
        private val running = AtomicBoolean(true)
        private val currentStatus = AtomicReference(message)
        private var thread: Thread? = null

        fun start() {
            print(HIDE_CURSOR)
            thread = Thread {
                var frameIndex = 0
                while (running.get()) {
                    val frame = gradient(frames[frameIndex])
                    print("\r  $frame ${currentStatus.get()}${" ".repeat(20)}")
                    System.out.flush()
                    frameIndex = (frameIndex + 1) % frames.size
                    Thread.sleep(80)
                }
            }
            thread?.start()
        }

        fun updateStatus(newStatus: String) {
            currentStatus.set(newStatus)
        }

        fun success(successMessage: String = currentStatus.get()) {
            stop()
            terminal.println("\r  ${green("✓")} $successMessage${" ".repeat(30)}")
        }

        fun error(errorMessage: String) {
            stop()
            terminal.println("\r  ${red("✗")} $errorMessage${" ".repeat(30)}")
        }

        fun stop() {
            running.set(false)
            thread?.join(200)
            print(SHOW_CURSOR)
            print("\r${" ".repeat(80)}\r")
        }
    }

    fun withSpinner(message: String, block: (Spinner) -> Unit): Boolean {
        val spinner = Spinner(message)
        spinner.start()
        return try {
            block(spinner)
            true
        } catch (e: Exception) {
            spinner.error("$message - ${e.message}")
            false
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERACTIVE PROMPTS
    // ═══════════════════════════════════════════════════════════════════

    fun header(text: String) {
        terminal.println()
        terminal.println("  " + gradient("◆") + " " + bold(text))
        terminal.println()
    }

    fun sectionDivider() {
        terminal.println()
        terminal.println("  " + subtle("─".repeat(56)))
        terminal.println()
    }

    fun prompt(message: String, default: String? = null): String {
        val defaultText = if (default != null) subtle(" ($default)") else ""
        terminal.print("  " + gradient("?") + " " + message + defaultText + subtle(": "))
        val input = readlnOrNull()?.trim() ?: ""
        return input.ifEmpty { default ?: "" }
    }

    fun confirm(message: String, default: Boolean = true): Boolean {
        val options = if (default) "${bold("Y")}/${muted("n")}" else "${muted("y")}/${bold("N")}"
        terminal.print("  " + gradient("?") + " " + message + " " + subtle("[$options]") + subtle(": "))
        val input = readlnOrNull()?.trim()?.lowercase() ?: ""
        return when {
            input.isEmpty() -> default
            input == "y" || input == "yes" -> true
            input == "n" || input == "no" -> false
            else -> default
        }
    }

    fun select(message: String, options: List<String>, default: Int = 0): Int {
        terminal.println()
        terminal.println("  " + gradient("?") + " " + bold(message))
        terminal.println("    " + subtle("(Use ↑↓ arrows or j/k, Enter to confirm)"))
        terminal.println()

        var selectedIndex = default

        // Use raw print for interactive rendering
        fun renderOptions() {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                val prefix = if (isSelected) gradient("❯") else " "
                val text = if (isSelected) bold(option) else muted(option)
                println("    $prefix $text")
            }
            System.out.flush()
        }

        fun clearOptions() {
            repeat(options.size) {
                print("$MOVE_UP$CLEAR_LINE")
            }
            System.out.flush()
        }

        print(HIDE_CURSOR)
        System.out.flush()
        renderOptions()

        try {
            jlineTerminal.enterRawMode()
            val reader = jlineTerminal.reader()

            while (true) {
                val c = reader.read()

                when (c) {
                    // Enter key
                    '\r'.code, '\n'.code -> break

                    // Up arrow (escape sequence)
                    27 -> { // ESC
                        val next1 = reader.read()
                        if (next1 == '['.code) {
                            when (reader.read()) {
                                'A'.code -> { // Up
                                    if (selectedIndex > 0) {
                                        selectedIndex--
                                        clearOptions()
                                        renderOptions()
                                    }
                                }
                                'B'.code -> { // Down
                                    if (selectedIndex < options.size - 1) {
                                        selectedIndex++
                                        clearOptions()
                                        renderOptions()
                                    }
                                }
                            }
                        }
                    }

                    // k = up, j = down (vim-style)
                    'k'.code, 'K'.code -> {
                        if (selectedIndex > 0) {
                            selectedIndex--
                            clearOptions()
                            renderOptions()
                        }
                    }
                    'j'.code, 'J'.code -> {
                        if (selectedIndex < options.size - 1) {
                            selectedIndex++
                            clearOptions()
                            renderOptions()
                        }
                    }

                    // Number keys for quick selection
                    in '1'.code..'9'.code -> {
                        val num = c - '1'.code
                        if (num < options.size) {
                            selectedIndex = num
                            clearOptions()
                            renderOptions()
                        }
                    }

                    // Ctrl+C
                    3 -> {
                        print(SHOW_CURSOR)
                        System.out.flush()
                        terminal.println()
                        throw InterruptedException("User cancelled")
                    }
                }
            }
        } finally {
            print(SHOW_CURSOR)
            System.out.flush()
        }

        // Clear and show final selection
        clearOptions()
        terminal.println("    " + gradient("❯") + " " + bold(options[selectedIndex]))
        terminal.println()

        return selectedIndex
    }

    fun multiSelect(message: String, options: List<String>, defaults: Set<Int> = emptySet()): Set<Int> {
        terminal.println()
        terminal.println("  " + gradient("?") + " " + bold(message))
        terminal.println("    " + subtle("(Use ↑↓ arrows, Space to toggle, Enter to confirm)"))
        terminal.println()

        var cursorIndex = 0
        val selected = defaults.toMutableSet()

        // Use raw print for interactive rendering
        fun renderOptions() {
            options.forEachIndexed { index, option ->
                val isCursor = index == cursorIndex
                val isSelected = index in selected
                val checkbox = if (isSelected) green("◉") else subtle("○")
                val prefix = if (isCursor) gradient("❯") else " "
                val text = if (isCursor) bold(option) else if (isSelected) option else muted(option)
                println("   $prefix $checkbox $text")
            }
            System.out.flush()
        }

        fun clearOptions() {
            repeat(options.size) {
                print("$MOVE_UP$CLEAR_LINE")
            }
            System.out.flush()
        }

        print(HIDE_CURSOR)
        System.out.flush()
        renderOptions()

        try {
            jlineTerminal.enterRawMode()
            val reader = jlineTerminal.reader()

            while (true) {
                val c = reader.read()

                when (c) {
                    // Enter key
                    '\r'.code, '\n'.code -> break

                    // Space - toggle selection
                    ' '.code -> {
                        if (cursorIndex in selected) {
                            selected.remove(cursorIndex)
                        } else {
                            selected.add(cursorIndex)
                        }
                        clearOptions()
                        renderOptions()
                    }

                    // Up arrow (escape sequence)
                    27 -> { // ESC
                        val next1 = reader.read()
                        if (next1 == '['.code) {
                            when (reader.read()) {
                                'A'.code -> { // Up
                                    if (cursorIndex > 0) {
                                        cursorIndex--
                                        clearOptions()
                                        renderOptions()
                                    }
                                }
                                'B'.code -> { // Down
                                    if (cursorIndex < options.size - 1) {
                                        cursorIndex++
                                        clearOptions()
                                        renderOptions()
                                    }
                                }
                            }
                        }
                    }

                    // k = up, j = down (vim-style)
                    'k'.code, 'K'.code -> {
                        if (cursorIndex > 0) {
                            cursorIndex--
                            clearOptions()
                            renderOptions()
                        }
                    }
                    'j'.code, 'J'.code -> {
                        if (cursorIndex < options.size - 1) {
                            cursorIndex++
                            clearOptions()
                            renderOptions()
                        }
                    }

                    // Number keys for quick toggle
                    in '1'.code..'9'.code -> {
                        val num = c - '1'.code
                        if (num < options.size) {
                            if (num in selected) {
                                selected.remove(num)
                            } else {
                                selected.add(num)
                            }
                            clearOptions()
                            renderOptions()
                        }
                    }

                    // Ctrl+C
                    3 -> {
                        print(SHOW_CURSOR)
                        System.out.flush()
                        terminal.println()
                        throw InterruptedException("User cancelled")
                    }
                }
            }
        } finally {
            print(SHOW_CURSOR)
            System.out.flush()
        }

        // Clear and show final selections
        clearOptions()
        options.forEachIndexed { index, option ->
            if (index in selected) {
                terminal.println("    " + green("◉") + " " + bold(option))
            }
        }
        if (selected.isEmpty()) {
            terminal.println("    " + subtle("(none selected)"))
        }
        terminal.println()

        return selected
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATUS MESSAGES
    // ═══════════════════════════════════════════════════════════════════

    fun step(text: String) {
        terminal.println("  " + subtle("→") + " " + text)
    }

    fun step(number: Int, text: String) {
        terminal.println("  " + muted("[$number]") + " " + text)
    }

    fun success(text: String, prefix: Boolean = true) {
        if (prefix) {
            terminal.println("  " + green("✓") + " " + text)
        } else {
            terminal.println(green(text))
        }
    }

    fun error(text: String, prefix: Boolean = true) {
        if (prefix) {
            terminal.println("  " + red("✗") + " " + text)
        } else {
            terminal.println(red(text))
        }
    }

    fun warning(text: String, prefix: Boolean = true) {
        if (prefix) {
            terminal.println("  " + yellow("⚠") + " " + text)
        } else {
            terminal.println(yellow(text))
        }
    }

    fun info(text: String, prefix: Boolean = true) {
        if (prefix) {
            terminal.println("  " + cyan("ℹ") + " " + text)
        } else {
            terminal.println(cyan(text))
        }
    }

    fun item(text: String) {
        terminal.println("    " + subtle("•") + " " + text)
    }

    fun blank() {
        terminal.println()
    }

    fun divider() {
        terminal.println("  " + subtle("─".repeat(50)))
    }

    // ═══════════════════════════════════════════════════════════════════
    // BOXES & TABLES
    // ═══════════════════════════════════════════════════════════════════

    fun box(title: String, content: List<String>, style: BoxStyle = BoxStyle.DEFAULT) {
        val titleLen = stripAnsi(title).length
        val maxLen = maxOf(titleLen, content.maxOfOrNull { stripAnsi(it).length } ?: 0)
        val width = maxLen + 4

        val borderColor: (String) -> String = when (style) {
            BoxStyle.DEFAULT -> ::fire
            BoxStyle.SUCCESS -> { s -> green(s) }
            BoxStyle.ERROR -> { s -> red(s) }
            BoxStyle.INFO -> { s -> cyan(s) }
        }

        terminal.println()
        terminal.println("  " + borderColor("╭") + borderColor("─".repeat(width)) + borderColor("╮"))
        val titlePadding = maxLen - titleLen + 3
        terminal.println("  " + borderColor("│") + " " + bold(title) + " ".repeat(titlePadding) + borderColor("│"))
        terminal.println("  " + borderColor("├") + borderColor("─".repeat(width)) + borderColor("┤"))
        content.forEach { line ->
            val stripped = stripAnsi(line)
            val padding = maxLen - stripped.length + 3
            terminal.println("  " + borderColor("│") + " " + line + " ".repeat(padding) + borderColor("│"))
        }
        terminal.println("  " + borderColor("╰") + borderColor("─".repeat(width)) + borderColor("╯"))
        terminal.println()
    }

    enum class BoxStyle { DEFAULT, SUCCESS, ERROR, INFO }

    fun table(headers: List<String>, rows: List<List<String>>) {
        val colWidths = headers.indices.map { col ->
            maxOf(
                stripAnsi(headers[col]).length,
                rows.maxOfOrNull { stripAnsi(it.getOrNull(col) ?: "").length } ?: 0
            )
        }

        val headerRow = headers.mapIndexed { i, h ->
            bold(h) + " ".repeat(colWidths[i] - stripAnsi(h).length)
        }.joinToString("  ")
        val separator = colWidths.joinToString("  ") { "─".repeat(it) }

        terminal.println()
        terminal.println("  $headerRow")
        terminal.println("  " + subtle(separator))
        rows.forEach { row ->
            val rowText = row.mapIndexed { i, cell ->
                cell + " ".repeat(colWidths.getOrElse(i) { 0 } - stripAnsi(cell).length)
            }.joinToString("  ")
            terminal.println("  $rowText")
        }
        terminal.println()
    }

    // ═══════════════════════════════════════════════════════════════════
    // COMPLETION SCREENS
    // ═══════════════════════════════════════════════════════════════════

    fun successScreen(title: String, subtitle: String = "") {
        terminal.println()
        terminal.println("  " + gradient("╔════════════════════════════════════════════════════════════╗"))
        terminal.println("  " + gradient("║") + " ".repeat(60) + gradient("║"))
        terminal.println("  " + gradient("║") + "   " + green("✓") + "  " + bold(green(title)) + " ".repeat(60 - 6 - stripAnsi(title).length) + gradient("║"))
        if (subtitle.isNotEmpty()) {
            terminal.println("  " + gradient("║") + "      " + muted(subtitle) + " ".repeat(60 - 6 - stripAnsi(subtitle).length) + gradient("║"))
        }
        terminal.println("  " + gradient("║") + " ".repeat(60) + gradient("║"))
        terminal.println("  " + gradient("╚════════════════════════════════════════════════════════════╝"))
        terminal.println()
    }

    fun errorScreen(title: String, details: String = "") {
        terminal.println()
        terminal.println("  " + red("╔════════════════════════════════════════════════════════════╗"))
        terminal.println("  " + red("║") + " ".repeat(60) + red("║"))
        terminal.println("  " + red("║") + "   " + red("✗") + "  " + bold(red(title)) + " ".repeat(60 - 6 - stripAnsi(title).length) + red("║"))
        if (details.isNotEmpty()) {
            terminal.println("  " + red("║") + "      " + details + " ".repeat(60 - 6 - stripAnsi(details).length) + red("║"))
        }
        terminal.println("  " + red("║") + " ".repeat(60) + red("║"))
        terminal.println("  " + red("╚════════════════════════════════════════════════════════════╝"))
        terminal.println()
    }

    fun nextSteps(steps: List<String>) {
        terminal.println()
        terminal.println("  " + bold(gradient("Next steps:")))
        terminal.println()
        steps.forEachIndexed { index, step ->
            terminal.println("  " + subtle("${index + 1}.") + " " + step)
        }
        terminal.println()
    }

    fun codeBlock(title: String, code: String) {
        terminal.println()
        terminal.println("  " + subtle("┌─") + " " + muted(title))
        terminal.println("  " + subtle("│"))
        code.lines().forEach { line ->
            terminal.println("  " + subtle("│") + "  " + kotlin(line))
        }
        terminal.println("  " + subtle("│"))
        terminal.println("  " + subtle("└─"))
        terminal.println()
    }

    fun done(message: String = "Done!") {
        terminal.println()
        terminal.println("  " + green("✓") + " " + bold(green(message)))
        terminal.println()
    }

    fun failed(message: String = "Failed!") {
        terminal.println()
        terminal.println("  " + red("✗") + " " + bold(red(message)))
        terminal.println()
    }

    // Helper to strip ANSI codes for length calculation
    private fun stripAnsi(text: String): String {
        return text.replace(Regex("\u001B\\[[;\\d]*m"), "")
    }
}
