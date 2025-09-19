package dev.zacsweers

import dev.zacsweers.ReadMeUpdater.ActivityItem

fun createReadMe(githubActivity: List<ActivityItem>, blogActivity: List<ActivityItem>): String {
  return """
    Currently funemployed. Read [my blog](https://zacsweers.dev/) or follow me on places `@ZacSweers`.

    <table><tr><td valign="top" width="60%">

    ## GitHub Activity
    <!-- githubActivity starts -->
${githubActivity.joinToString("\n\n") { "    $it" }}
    <!-- githubActivity ends -->
    </td><td valign="top" width="40%">

    ## On My Blog
    <!-- blog starts -->
${blogActivity.joinToString("\n\n") { "    $it" }}
    <!-- blog ends -->
    _More on [zacsweers.dev](https://zacsweers.dev/)_
    </td></tr></table>
    
    <sub><a href="https://simonwillison.net/2020/Jul/10/self-updating-profile-readme/">Inspired by Simon Willison's auto-updating profile README.</a></sub>
  """
    .trimIndent()
}
