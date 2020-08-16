package dev.zacsweers

fun createReadMe(
  githubActivity: List<ActivityItem>,
  blogActivity: List<ActivityItem>
): String {
  return """
    Currently working on [Slack](https://slack.com/). Read [my blog](https://zacsweers.dev/) or [follow @ZacSweers on Twitter](https://twitter.com/ZacSweers).

    <table><tr><td valign="top" width="50%">

    ## GitHub Activity
    <!-- githubActivity starts -->
${githubActivity.joinToString("\n\n") { "    $it" }}
    <!-- recent_releases ends -->
    </td><td valign="top" width="50%">

    ## On My Blog
    <!-- blog starts -->
${blogActivity.joinToString("\n\n") { "    $it" }}
    <!-- blog ends -->
    More on [zacsweers.dev](https://zacsweers.dev/)
    </td></tr></table>
    
    <sub><a href="https://simonwillison.net/2020/Jul/10/self-updating-profile-readme/">Inspired by Simon Willison's auto-updating profile README.</a></sub>
  """.trimIndent()
}