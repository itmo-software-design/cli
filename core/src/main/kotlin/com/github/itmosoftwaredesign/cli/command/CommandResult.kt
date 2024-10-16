package com.github.itmosoftwaredesign.cli.command

/**
 * Common command result, used to describe result of command execution.
 *
 * @author sibmaks
 * @since 0.0.1
 */
sealed class CommandResult(val statusCode: Int)

/**
 * Success command result. Command executed without any errors.
 * Used `0` status code.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class SuccessResult : CommandResult(0)

/**
 * Error command result. Command execution failed. Contains status code.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class ErrorResult(statusCode: Int) : CommandResult(statusCode)

/**
 * Command result, which means that command execution interrupt current execution line.
 *
 * @author sibmaks
 * @since 0.0.1
 */
class CommandInterrupted(statusCode: Int) : CommandResult(statusCode)