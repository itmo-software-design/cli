package com.github.itmosoftwaredesign.cli

import java.nio.file.Path
import java.util.*

/**
 * Command line interface environment.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class Environment(
    private val variables: MutableMap<String, String> = HashMap(),
    private val systemVariables: MutableMap<String, () -> Any> = HashMap(),
    var workingDirectory: Path = Path.of(""),
    var lastExitCode: Int = 0
) {
    init {
        systemVariables["PWD"] = { workingDirectory.toString() }
        systemVariables["?"] = { lastExitCode }
        systemVariables["$"] = { getPid() }
    }

    /**
     * Set variable in environment.
     *
     * @param name  variable name
     * @param value variable value
     */
    fun setVariable(name: String, value: String?) {
        if (value == null) {
            variables.remove(name)
        } else {
            variables[name] = value
        }
    }

    /**
     * Get variable value from environment.
     *
     * @param name variable name
     * @return variable value
     */
    fun getVariable(name: String): String? {
        val systemVariable = systemVariables[name]
        if (systemVariable != null) {
            return "${systemVariable()}"
        }

        return variables[name]
    }

    private fun getPid(): Long {
        return ProcessHandle.current()
            .pid()
    }

    /**
     * Get variable names from environment.
     *
     * @return variable names
     */
    fun getVariableNames(): Set<String> {
        return Collections.unmodifiableSet(variables.keys)
    }

}
