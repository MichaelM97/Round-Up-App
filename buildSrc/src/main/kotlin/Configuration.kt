object Configuration {
    val EXCLUDED_PACKAGING_OPTIONS = listOf(
        "META-INF/NOTICE",
        "META-INF/LICENSE",
        "META-INF/LICENSE.md",
        "META-INF/LICENSE-notice.md",
        "META-INF/*.kotlin_module",
        "META-INF/AL2.0",
        "META-INF/LGPL2.1",
        "META-INF/licenses/ASM",
        "io/mockk/settings.properties",
        "win32-x86/attach_hotspot_windows.dll",
        "win32-x86-64/attach_hotspot_windows.dll",
    )
}
