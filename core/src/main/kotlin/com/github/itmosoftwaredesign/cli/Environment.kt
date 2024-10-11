package com.github.itmosoftwaredesign.cli

import java.nio.file.Path

/**
 * Command line interface environment.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class Environment(
    private val variables: MutableMap<String, String> = HashMap(),
    var workingDirectory: Path = Path.of("")
) {
    /**
     * Set variable in environment.
     *
     * @param name  variable name
     * @param value variable value
     */
    fun setVariable(name: String, value: String) {
        variables[name] = value
    }

    /**
     * Get variable value from environment.
     *
     * @param name variable name
     * @return variable value
     */
    fun getVariable(name: String): String? {
        return variables[name]
    }

}
