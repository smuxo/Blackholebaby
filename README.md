# Дыр — Android overlay-компаньон

## Структура

- `character/Character.kt` — состояние персонажа (позиция, анимация, речь)
- `character/SpriteAnimator.kt` — нарезка `assets/spritesheet.png` (6×8) на кадры, без внешних библиотек
- `network/ConfigLoader.kt` — парсер `assets/config.yaml`
- `network/ApiClient.kt` — HTTP-запрос к OpenAI-совместимому API (`HttpURLConnection`, без Retrofit)
- `network/DyrResponse.kt` — разбор JSON-ответа модели
- `skills/SkillRouter.kt` — `open_url`, `open_app`, `set_timer`, `smalltalk`
- `overlay/OverlayService.kt` — `SYSTEM_ALERT_WINDOW` сервис, хостит Compose UI
- `overlay/OverlayLifecycleOwner.kt` — Lifecycle/ViewModelStore/SavedState для ComposeView внутри Service
- `ui/CharacterView.kt` — сам персонаж, движение к точке, тап открывает поле ввода
- `ui/ChatBubble.kt` — облачко речи
- `MainActivity.kt` — запрос разрешения на overlay, запуск сервиса

## Ключ API

Вставь ключ в `app/src/main/assets/config.yaml`, поле `apiKey`.

## Сборка

Через GitHub Actions (`.github/workflows/build.yml`) — пуш в `main`, готовый `app-debug.apk` в артефактах.

Локально: `gradle assembleDebug` (Gradle 8.7, JDK 17). Wrapper не включён — используется установленный Gradle или `gradle/actions/setup-gradle` в CI.

## Архитектура под десктоп

Сетевой и доменный слой (`network/`, `character/`, `skills/`) не зависит от Android UI — при портировании на десктоп меняется только `overlay/` и `ui/` (сейчас Compose+WindowManager, на десктопе — Compose Desktop окно).
