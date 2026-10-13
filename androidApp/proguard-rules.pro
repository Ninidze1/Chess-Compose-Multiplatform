# R8 rules for the release build, on top of proguard-android-optimize.txt.
#
# No library-specific keep rules are needed here:
# - kotlinx-serialization ships its own consumer rules, and the app only looks serializers
#   up through the generated companion functions (ChessBoardDTO.serializer()), never by
#   reflection.
# - Koin definitions use constructor references and lambdas, not reflection.
# - Compose and AndroidX ship their own consumer rules.

# Keep file names and line numbers so release crash stack traces can be retraced.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
