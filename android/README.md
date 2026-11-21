# GabGames Medicina — Android nativo

Aplicativo Android nativo em Kotlin + Jetpack Compose para montar uma tier list das disciplinas de Medicina.

## Recursos
- 52 disciplinas.
- Seis categorias personalizadas (S, A, B, C, D e E).
- Arrastar e soltar com gesto de pressionar e segurar.
- Salvamento local com Preferences DataStore.
- Funciona offline, sem conta e sem permissões especiais.

## Compilar localmente
Requisitos: JDK 17 e Android SDK (API 35).

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions
Cada push que alterar `android/**` compila o APK e publica o arquivo como artefato `gabgames-medicina-debug-apk` na execução do workflow.

Pacote: `br.com.gabgames.medicina`.
