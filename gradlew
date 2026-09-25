#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    link=`expr "$PRG" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != maximum.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" -o "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`

    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 3 -type d -name gradle 2>/dev/null | head -n 1`
    [ -z "$ROOTDIRSRAW" ] && ROOTDIRSRAW=`find -L / -maxdepth 3 -type d -name gradle 2>/dev/null | head -n 1`
    [ -n "$ROOTDIRSRAW" ] && ROOTDIR=`echo "$ROOTDIRSRAW" | sed 's|/[^/]*$||'`
    [ -z "$ROOTDIR" ] && ROOTDIR=`dirname "$APP_HOME"`

    if [ -d "$ROOTDIR" ]
    then
        module_opts="-add-modules ALL-SYSTEM"
        GRADLE_OPTS="$GRADLE_OPTS $module_opts"
    fi
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`

    JAVACMD=`cygpath --unix "$JAVACMD"`

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 3 -type d -name gradle 2>/dev/null | head -n 1`
    [ -z "$ROOTDIRSRAW" ] && ROOTDIRSRAW=`find -L / -maxdepth 3 -type d -name gradle 2>/dev/null | head -n 1`
    [ -n "$ROOTDIRSRAW" ] && ROOTDIR=`echo "$ROOTDIRSRAW" | sed 's|/[^/]*$||'`
    [ -z "$ROOTDIR" ] && ROOTDIR=`dirname "$APP_HOME"`

    if [ -d "$ROOTDIR" ]
    then
        module_opts="-add-modules ALL-SYSTEM"
        GRADLE_OPTS="$GRADLE_OPTS $module_opts"
    fi

    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`

    JAVACMD=`cygpath --unix "$JAVACMD"`
fi

# Collect all arguments for the java command;
#   * $DEFAULT_JVM_OPTS, $JAVA_OPTS, and $GRADLE_OPTS can contain fragments of
#     shell commands we need for eval
#   * put everything else in "$@"
set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, useful for xargs.
# In practice, we have 3 types of quoting:
#   1) simple: unquoted words
#   2) quoted as a whole by the shell: 'words here'
#   3) quoted word-by-word: word="words here" (e.g. to circumvent autosplitting)
#
# (3) is a special case of (2), as we prepend the quote we stipped.
#
# But we must be careful of one case: within single quotes, backslashes
# do not escape anything. So if we have one arg containing a semicolon,
# this needs to be wrapped in double quotes, which means we have to escape
# all embedded double quotes to preserve the string.
#
# Since the input comes from "$@", we will always have 0 or 1 parent shell
# quote, which is preserved to the end.
if [ "$#" -eq 0 ] ; then
    set -- \
        ""
fi
# Discard cd standard output but keep stderr
eval "set -- $(
        printf 'argument=%s\n' "$@" |
        sed 's/argument=//' |
        tr '\n' ' ' |
        sed 's/ $//g'
    )" >&2

exec "$JAVACMD" "$@"
