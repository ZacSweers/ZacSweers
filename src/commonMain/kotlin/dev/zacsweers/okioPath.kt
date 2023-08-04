package dev.zacsweers

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.ProcessedArgument
import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.NullableOption
import com.github.ajalt.clikt.parameters.options.RawOption
import com.github.ajalt.clikt.parameters.options.convert
import kotlin.io.path.isReadable
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.isWritable
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/*
 * okio.Path options for Clikt
 */

private fun pathType(context: Context, fileOkay: Boolean, folderOkay: Boolean): String =
  when {
    fileOkay && !folderOkay -> context.localization.pathTypeFile()
    !fileOkay && folderOkay -> context.localization.pathTypeDirectory()
    else -> context.localization.pathTypeOther()
  }

internal fun convertToPath(
  path: String,
  mustExist: Boolean,
  canBeFile: Boolean,
  canBeFolder: Boolean,
  mustBeWritable: Boolean,
  mustBeReadable: Boolean,
  canBeSymlink: Boolean,
  fileSystem: FileSystem,
  context: Context,
  fail: (String) -> Unit,
): Path {
  val name = pathType(context, canBeFile, canBeFolder)
  return with(context.localization) {
    path.toPath().also {
      if (mustExist && !fileSystem.exists(it)) fail(pathDoesNotExist(name, it.toString()))
      val metadata = fileSystem.metadata(it)
      if (!canBeFile && metadata.isRegularFile) fail(pathIsFile(name, it.toString()))
      if (!canBeFolder && metadata.isDirectory) fail(pathIsDirectory(name, it.toString()))
      val nioPath = it.toNioPath()
      if (mustBeWritable && !nioPath.isWritable()) fail(pathIsNotWritable(name, it.toString()))
      if (mustBeReadable && !nioPath.isReadable()) fail(pathIsNotReadable(name, it.toString()))
      if (!canBeSymlink && nioPath.isSymbolicLink()) fail(pathIsSymlink(name, it.toString()))
    }
  }
}

/**
 * Convert the argument to a [Path].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param fileSystem The [FileSystem] with which to resolve paths
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
fun RawArgument.path(
  mustExist: Boolean = false,
  canBeFile: Boolean = true,
  canBeDir: Boolean = true,
  mustBeWritable: Boolean = false,
  mustBeReadable: Boolean = false,
  canBeSymlink: Boolean = true,
  fileSystem: FileSystem = FileSystem.SYSTEM,
): ProcessedArgument<Path, Path> {
  return convert(
    completionCandidates = com.github.ajalt.clikt.completion.CompletionCandidates.Path
  ) { str ->
    convertToPath(
      str,
      mustExist,
      canBeFile,
      canBeDir,
      mustBeWritable,
      mustBeReadable,
      canBeSymlink,
      fileSystem,
      context
    ) {
      fail(it)
    }
  }
}

/**
 * Convert the option to a [Path].
 *
 * @param mustExist If true, fail if the given path does not exist
 * @param canBeFile If false, fail if the given path is a file
 * @param canBeDir If false, fail if the given path is a directory
 * @param mustBeWritable If true, fail if the given path is not writable
 * @param mustBeReadable If true, fail if the given path is not readable
 * @param fileSystem The [FileSystem] with which to resolve paths.
 * @param canBeSymlink If false, fail if the given path is a symlink
 */
fun RawOption.path(
  mustExist: Boolean = false,
  canBeFile: Boolean = true,
  canBeDir: Boolean = true,
  mustBeWritable: Boolean = false,
  mustBeReadable: Boolean = false,
  canBeSymlink: Boolean = true,
  fileSystem: FileSystem = FileSystem.SYSTEM,
): NullableOption<Path, Path> {
  return convert(
    { localization.pathMetavar() },
    com.github.ajalt.clikt.completion.CompletionCandidates.Path
  ) { str ->
    convertToPath(
      str,
      mustExist,
      canBeFile,
      canBeDir,
      mustBeWritable,
      mustBeReadable,
      canBeSymlink,
      fileSystem,
      context
    ) {
      fail(it)
    }
  }
}
