# Bar Attestation Trainer (Android)

## Как собрать готовый APK за 2 минуты без установки Android Studio:
1. Создайте новый репозиторий на GitHub (можно с телефона).
2. Распакуйте этот архив и загрузите все файлы в корень репозитория (включая скрытую папку `.github/workflows/build_apk.yml`).
3. Откройте вкладку **Actions** в вашем репозитории на GitHub.
4. Дождитесь завершения задачи **Build Android APK** (зеленая галочка).
5. Нажмите на завершенную задачу и внизу в блоке **Artifacts** скачайте `BarAttestationTrainer-debug` (внутри лежит готовый `app-debug.apk`).

## Локальная сборка в Android Studio:
Откройте папку `BarAttestationTrainer` в Android Studio Ladybug (или новее) и нажмите **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
