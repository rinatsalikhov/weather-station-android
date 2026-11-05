# Weather — простое Android-приложение на Kotlin + Jetpack Compose

Приложение обращается к вебсервису **ESP32** в локальной сети
(`http://192.168.1.133/data`) и показывает на экране **температуру, влажность
и атмосферное давление**. Интерфейс повторяет веб-страницу устройства
`http://192.168.1.133/`, данные обновляются каждые 5 секунд.

> **Статус:** `:app:assembleDebug` и `:app:testDebugUnitTest` проходят успешно.

---

## 1. Стек и версии

| Слой | Технология | Версия |
|---|---|---|
| Сборка | Android Gradle Plugin | 9.4.1 |
| Сборка | Gradle | 9.6.0 |
| Сборка | JDK | 17+ |
| Сборка | Kotlin | 2.4.21 |
| UI | Jetpack Compose (BOM) | 2026.09.00 |
| UI | Material 3 | из BOM |
| Архитектура | MVVM + `StateFlow` | — |
| DI | ручной контейнер (без Hilt) | — |
| Сеть | Retrofit + OkHttp + kotlinx.serialization | 3.0.0 / 4.12.0 / 1.12.0 |
| Асинхронность | Coroutines | 1.11.0 |

> **Важно про AGP 9:** в Android Gradle Plugin 9.x появилась *встроенная* поддержка
> Kotlin. Плагин `org.jetbrains.kotlin.android` применять **не нужно** — только
> `com.android.application` + `org.jetbrains.kotlin.plugin.compose`
> (+ `org.jetbrains.kotlin.plugin.serialization` для `@Serializable`).

---

## 2. Структура проекта

```
weather/
├── gradle/
│   ├── libs.versions.toml            # Version Catalog: все версии в одном месте
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew / gradlew.bat             # Gradle Wrapper
├── settings.gradle.kts               # Подключение модулей + репозитории
├── build.gradle.kts                  # Плагины верхнего уровня (apply false)
├── gradle.properties                 # JVM-аргументы, кэш, configuration cache
├── .gitignore
│
└── app/                              # единственный модуль приложения
    ├── build.gradle.kts              # Конфигурация модуля + зависимости
    ├── proguard-rules.pro            # Правила R8 для release
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml   # Разрешения, точка входа, иконка
        │   ├── java/com/example/weather/
        │   │   ├── MainActivity.kt              # Activity + setContent
        │   │   ├── WeatherApplication.kt        # Application, создаёт контейнер
        │   │   ├── di/
        │   │   │   └── AppContainer.kt           # Retrofit → API → Repository
        │   │   ├── data/
        │   │   │   ├── WeatherRepository.kt      # Единственная точка доступа к данным
        │   │   │   └── remote/
        │   │   │       ├── WeatherApi.kt         # Интерфейс вебсервиса (Retrofit)
        │   │   │       └── dto/
        │   │   │           ├── WeatherResponseDto.kt  # JSON-зеркало ответа
        │   │   │           └── WeatherDtoMapper.kt    # DTO → доменная модель
        │   │   ├── domain/
        │   │   │   └── model/Weather.kt          # Чистая доменная модель
        │   │   └── ui/
        │   │       ├── WeatherScreen.kt          # Stateless-экран (Compose)
        │   │       ├── WeatherViewModel.kt       # Состояние + загрузка данных
        │   │       ├── WifiSignalIndicator.kt    # Полоски уровня Wi-Fi
        │   │       ├── state/WeatherUiState.kt   # Loading / Success(isStale) / Error
        │   │       └── theme/                    # Color, Theme, Type
        │   └── res/
        │       ├── values/strings.xml            # Строки (ru)
        │       ├── values/themes.xml             # Тема окна
        │       ├── values-night/themes.xml
        │       ├── drawable/                     # Иконка (векторная)
        │       └── mipmap-anydpi*/               # Launcher icon
        └── test/
            └── java/com/example/weather/
                └── WeatherDtoMapperTest.kt       # Юнит-тесты (без сети)
```

### Как устроены слои

```
UI (Compose)  →  ViewModel  →  Repository  →  Retrofit API  →  вебсервис
     ↑              ↑              ↑
  WeatherScreen  WeatherViewModel  AppContainer
```

* **UI** знает только про `WeatherUiState` — ни про сеть, ни про Retrofit.
* **ViewModel** знает только про `WeatherRepository`; раз в 5 секунд опрашивает
  устройство, пока экран на переднем плане, и хранит последние удачные данные.
* **Repository** знает про `WeatherApi` и DTO.
* **DTO** — «как есть» JSON. **Доменная модель** `Weather` — то, с чем работает UI.

---

## 3. Окружение

### Текущая машина (проверено, сборка проходит)

| Компонент | Версия / путь |
|---|---|
| JDK для запуска Gradle | **17.0.12** — `C:\Program Files\Java\jdk-17`, доступен в `PATH` через `C:\Program Files\Common Files\Oracle\Java\javapath` |
| JVM Gradle-демона | **25.0.3** — Gradle скачал сам в `~\.gradle\jdks` (см. `gradle/gradle-daemon-jvm.properties`) |
| Android Studio | установлена, встроенный JBR 25.0.3 |
| Android SDK | `C:\Users\rinat\AppData\Local\Android\Sdk` |
| SDK Platform | `android-37.0` и `android-37.2` (используется 37.2) |
| Build Tools | 36.0.0 |
| Platform Tools | `adb` |
| Эмулятор | AVD `Medium_Phone` — android-37.0, google_apis_playstore, x86_64 |

Проверка одной командой:

```powershell
java -version                     # 17.0.12
.\gradlew -v                      # Gradle 9.6.0
.\gradlew :app:assembleDebug      # APK: app/build/outputs/apk/debug/app-debug.apk
```

`local.properties` (путь к SDK) Android Studio создаёт сам, в git он не попадает:

```properties
sdk.dir=C\:\\Users\\rinat\\AppData\\Local\\Android\\Sdk
```

### Нужен ли `JAVA_HOME`

**Не обязателен.** `gradlew.bat` выбирает JVM так: `JAVA_HOME`, если задан, иначе `java`
из `PATH`. В `PATH` уже стоит JDK 17, поэтому из нового терминала сборка запускается
и без `JAVA_HOME`.

`JAVA_HOME` стоит задать, только если нужно зафиксировать конкретный JDK независимо от
порядка `PATH` (пригодится и для `sdkmanager`):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"     # только для текущего терминала
```

### Почему демон на Java 25

Файл `gradle/gradle-daemon-jvm.properties` (создан Android Studio при открытии проекта)
задаёт `toolchainVersion=25`, поэтому Gradle запускает демон на JDK 25 и при
необходимости скачивает его сам в `~\.gradle\jdks`. JDK 17 при этом отвечает только за
запуск Gradle и за `jvmTarget` скомпилированного кода — на работу приложения это никак
не влияет. Если лишний JDK не нужен, файл можно удалить: демон тогда будет работать на
JVM лаунчера (JDK 17).

---

## 4. Сборка и запуск

```powershell
# Первая сборка (скачает Gradle и все зависимости)
.\gradlew assembleDebug

# Запустить юнит-тесты
.\gradlew test

# Запустить эмулятор (AVD Medium_Phone)
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Medium_Phone

# Установить на подключённый девайс/эмулятор
.\gradlew installDebug
```

Готовый APK лежит в `app/build/outputs/apk/debug/app-debug.apk`.

Либо просто открой папку в Android Studio и жми Run.

---

## 5. Вебсервис ESP32

Приложение берёт данные с ESP32 в локальной сети:

```
GET http://192.168.1.133/data
```

Пример ответа:

```json
{"device":"esp32-meteo","ip":"192.168.1.133","uptime_s":40820,"rssi_dbm":-61,
 "temperature_c":8.80,"humidity_pct":100.0,"pressure_mmhg":766.04}
```

Используются все поля ответа: `device`, `temperature_c`, `humidity_pct`,
`pressure_mmhg` — в карточках, `ip`, `uptime_s`, `rssi_dbm` — в нижней строке.
Новые/лишние поля не ломают разбор благодаря `ignoreUnknownKeys = true`
в `di/AppContainer.kt`.

Адрес сервиса задан в одном месте — `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.1.133/\"")
```

Путь метода — `data/remote/WeatherApi.kt` (`@GET("data")`).

> Телефон (или эмулятор) должен быть в той же сети, что и ESP32. Эмулятор ходит
> в локальную сеть через NAT, так что адрес `192.168.1.133` доступен и из него.

### Если изменятся IP или формат ответа

1. **Адрес** — `BASE_URL` в `app/build.gradle.kts` (слэш в конце обязателен).
2. **Путь и параметры запроса** — `data/remote/WeatherApi.kt`:

   ```kotlin
   @GET("data")
   suspend fun getCurrentWeather(): WeatherResponseDto
   ```

3. **Поля JSON** — `data/remote/dto/WeatherResponseDto.kt` (для snake_case
   используется `@SerialName`) и маппер `WeatherDtoMapper.kt`.

### HTTP вместо HTTPS

Сервис отдаёт данные по `http://`, поэтому в `AndroidManifest.xml` включён
`android:usesCleartextTraffic="true"`. Когда появится HTTPS — флаг можно убрать.

> Если понадобится API-ключ, его удобнее вынести в `gradle.properties` и читать
> через `providers.gradleProperty("API_KEY").get()`, а в запрос добавлять
> interceptor в `di/AppContainer.kt` — тогда ключ не попадёт в git.

---

## 6. Экран и поведение

Вёрстка повторяет веб-страницу `http://192.168.1.133/`:

* фон `#EEF1F6`, по центру заголовок «🌤️ Метеостанция»;
* три белые карточки (скругление 18 dp, мягкая тень): иконка и подпись сдвинуты
  к левому краю (отступ 10 dp), справа — крупное значение с отклонением от нормы
  760;
* значения как на странице — с одним знаком после запятой: `9.2 °C`,
  `100.0 %`, `766.0 мм`;
* у давления после «мм» — отклонение от нормы 760: зелёное `+7.5`, если давление
  выше нормы, красное `-3.7` — если ниже (шкала под цифрами при этом сдвигается,
  чтобы центральная отметка осталась под серединой цифр, а не под отклонением);
* внизу карточки давления — узкая шкала (примерно треть ширины карточки) из 7
  отметок (центральная и по три с каждой стороны): она стоит под цифрами значения,
  а центральная отметка (выше и толще остальных) — ровно под серединой цифр, это
  текущее значение. Норма `760` — не отдельная подпись, а само деление шкалы:
  она заменяет собой отметку на ближайшем делении (**слева** от центральной, если
  давление выше нормы, **справа** — если ниже). Одно деление — 3 мм рт. ст.,
  отклонение больше 9 мм упирается в крайнюю отметку;
* у давления после «мм» — отклонение от нормы 760: зелёное `+7.5`, если давление
  выше нормы, красное `-3.7` — если ниже;
* внизу мелкая строка:
  `обновлено 12:39:46 · аптайм 685 мин 26 сек · esp32-meteo · IP 192.168.1.133`,
  под ней — индикатор Wi-Fi `WiFi ▂▄▆ -62 dBm`;
* кнопка «Обновить» — ручное обновление (данные и так приходят сами).

Индикатор Wi-Fi (`ui/WifiSignalIndicator.kt`) — четыре полоски разной высоты,
как у сотовой связи: заполненные полоски — цветом текста, пустые — с прозрачностью
25 %. Уровень считается по RSSI (`wifiSignalLevel`):

| RSSI, dBm | Полосок |
|---|---|
| ≥ −55 | 4 (отлично) |
| −56…−67 | 3 (хорошо) |
| −68…−78 | 2 (средне) |
| −79…−88 | 1 (слабо) |
| < −88 | 0 |

Пороги покрыты юнит-тестом `WifiSignalLevelTest`.

Поведение `WeatherViewModel`:

* опрос `/data` каждые **5 секунд**, как на веб-странице;
* опрос идёт только пока экран на переднем плане: `collectAsStateWithLifecycle`
  в фоне отписывается, `subscriptionCount` становится 0 и цикл останавливается;
* если очередной опрос не удался, последние значения остаются на экране,
  а нижняя строка заменяется на «нет связи с устройством»
  (`Success(isStale = true)`);
* пока данных ещё не было — спиннер; если не удалось вообще ничего загрузить —
  экран ошибки с кнопкой «Повторить».

Цвета темы (`ui/theme/Color.kt`) взяты со страницы устройства, Dynamic Color
(Material You) отключён — иначе система перекрашивала бы экран под обои.
Для тёмной темы есть отдельная приглушённая палитра, светлая — точь-в-точь как
на веб-странице.

---

## 7. Что можно развивать дальше

* **Hilt** — заменить `AppContainer` на `@HiltViewModel` + `@Inject`
  (в AGP 9 kapt заменён на `com.android.legacy-kapt` / KSP).
* **Кэш** — Room или DataStore, чтобы показывать последние данные офлайн.
* **Обновление по расписанию** — WorkManager.
* **Несколько экранов** — Navigation Compose (`androidx.navigation:navigation-compose`).
* **Разделение на модули** — `:app`, `:core:network`, `:core:domain`, `:feature:weather`.
