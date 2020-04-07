package com.rcohen;

import com.beust.jcommander.Parameter;

class Flags {
    @Parameter(names = "-version", description = "Displays the current version")
    boolean version = false;

    @Parameter(names = "-help", description = "Displays usage and options")
    boolean help = false;

    @Parameter(names = "-gradletask", description = "The gradle task to run upon each dependency removal test")
    String gradleTask = ":app:assembleDebug";

    @Parameter(names = "-skip", description = "Comma separated list of dependencies to skip")
    String skipDependencies = "";
}
