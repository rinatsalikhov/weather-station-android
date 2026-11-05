# Правила для R8/ProGuard (release-сборка).
# По умолчанию правил не нужно — kotlinx.serialization и Retrofit аннотированы,
# а R8 сам сохраняет нужные классы по аннотациям.
#
# Пример: если включишь minify и увидишь ошибки десериализации,
# добавь keep-правила для своих @Serializable моделей:
#
# -keep class com.example.weather.data.remote.dto.** { *; }
