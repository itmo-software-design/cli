package com.github.itmosoftwaredesign.cli.command

import jakarta.annotation.Nonnull
import jakarta.annotation.Nullable
import java.util.*

/**
 *  Command registry. Store all command's alias and command itself.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class CommandRegistry {
    private val commands: MutableMap<String, Command>

    /**
     * Construct empty command registry
     */
    constructor() {
        this.commands = HashMap()
    }

    /**
     * Construct command registry with initial commands
     *
     * @param commands initial commands
     */
    constructor(commands: Map<String, Command>) {
        this.commands = Optional.ofNullable(commands)
            .map { m: Map<String, Command>? -> HashMap(m) }
            .orElseGet { HashMap() }
    }

    /**
     * Get command instance by alias
     *
     * @param name command alias
     * @return command instance
     */
    @Nullable
    operator fun get(@Nonnull name: String): Command? {
        return commands[name]
    }

    /**
     * Add command in registry
     *
     * @param name    command alias
     * @param command command instance
     */
    fun register(@Nonnull name: String, @Nonnull command: Command) {
        commands[name] = command
    }


    /**
     * Remove command from registry
     *
     * @param name command alias
     */
    fun unregister(name: String) {
        commands.remove(name)
    }
}
