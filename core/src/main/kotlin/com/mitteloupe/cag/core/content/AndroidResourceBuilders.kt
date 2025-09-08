package com.mitteloupe.cag.core.content

fun buildAndroidManifest(appName: String): String =
    """
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:tools="http://schemas.android.com/tools">

        <application
            android:allowBackup="true"
            android:dataExtractionRules="@xml/data_extraction_rules"
            android:fullBackupContent="@xml/backup_rules"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/Theme.$appName"
            tools:targetApi="31">
            <activity
                android:name=".MainActivity"
                android:exported="true"
                android:theme="@style/Theme.$appName">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
        </application>

    </manifest>
    """.trimIndent()

fun buildStringsXml(packageName: String): String =
    """
    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="app_name">${packageName.split('.').last().capitalized}</string>
    </resources>
    """.trimIndent()

fun buildThemesXml(appName: String): String =
    """
    <?xml version="1.0" encoding="utf-8"?>
    <resources xmlns:tools="http://schemas.android.com/tools">
        <style name="Theme.$appName" parent="Theme.Material3.DayNight.NoActionBar" />
    </resources>
    """.trimIndent()

fun buildThemeKt(
    appName: String,
    packageName: String
): String =
    """
    package $packageName.ui.theme

    import androidx.compose.foundation.isSystemInDarkTheme
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.darkColorScheme
    import androidx.compose.material3.lightColorScheme
    import androidx.compose.runtime.Composable

    private val DarkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
    )

    private val LightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40

        /* Other default colors to override
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        */
    )

    @Composable
    fun ${appName}Theme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val colorScheme = when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
    """.trimIndent()

fun buildColorsKt(packageName: String): String =
    """
    package $packageName.ui.theme

    import androidx.compose.ui.graphics.Color

    val Purple80 = Color(0xFFD0BCFF)
    val PurpleGrey80 = Color(0xFFCCC2DC)
    val Pink80 = Color(0xFFEFB8C8)

    val Purple40 = Color(0xFF6650a4)
    val PurpleGrey40 = Color(0xFF625b71)
    val Pink40 = Color(0xFF7D5260)
    """.trimIndent()

fun buildTypographyKt(packageName: String): String =
    """
    package $packageName.ui.theme

    import androidx.compose.material3.Typography
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontFamily
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.sp

    // Set of Material typography styles to start with
    val Typography = Typography(
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        )
        /* Other default text styles to override
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
        */
    )
    """.trimIndent()

fun buildBackupRulesXml(): String =
    """
    <?xml version="1.0" encoding="utf-8"?><!--
       Sample backup rules file; uncomment and customize as necessary.
       See https://developer.android.com/guide/topics/data/autobackup
       for details.
       Note: This file is ignored for devices older that API 23 even if they have auto
       backup available.
    -->
    <full-backup-content>
        <!--
       <include domain="sharedpref" path="."/>
       <exclude domain="sharedpref" path="device.xml"/>
    -->
    </full-backup-content>
    """.trimIndent()

fun buildDataExtractionRulesXml(): String =
    """
    <?xml version="1.0" encoding="utf-8"?><!--
       Sample data extraction rules file; uncomment and customize as necessary.
       See https://developer.android.com/about/versions/12/backup-restore#xml-changes
       for details.
    -->
    <data-extraction-rules>
        <!--
        <cloud-backup />
        <device-transfer />
        -->
    </data-extraction-rules>
    """.trimIndent()
