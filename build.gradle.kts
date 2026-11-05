// Корневой build-файл. Здесь только объявляются плагины с версиями (apply false),
// чтобы все модули использовали одну и ту же версию плагина на classpath.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
