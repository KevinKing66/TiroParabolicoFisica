# Laboratorio Virtual de Fisica - Tiro Parabolico

Aplicacion Android desarrollada en Kotlin para simular el movimiento de proyectiles, visualizar la trayectoria parabolica, autenticar usuarios y guardar experimentos en una base de datos remota.

El proyecto esta organizado con Clean Architecture, MVVM, Hilt, Coroutines, Flow, ViewBinding, Material Design 3 y pruebas unitarias para los calculos fisicos.

## Funcionalidades

- Login y registro de usuarios contra una API REST.
- Simulacion de tiro parabolico con velocidad inicial, angulo y gravedad configurable.
- Calculo automatico de:
  - Velocidad horizontal `vx = v0 * cos(theta)`.
  - Velocidad vertical `vy = v0 * sin(theta)`.
  - Posicion `x = vx * t`, `y = vy * t - 1/2 * g * t^2`.
  - Tiempo de vuelo.
  - Altura maxima.
  - Alcance horizontal.
- Visualizacion de trayectoria usando Canvas.
- Guardado remoto de experimentos en Firebase Firestore.
- Historial remoto de experimentos para comparacion.
- Soporte para modo claro y oscuro.
- Paleta visual basada en `#034C6F`.

## Arquitectura

La aplicacion mantiene separacion por capas:

```text
app/src/main/java/com/king/kevin/tiroparabolico/
  core/
    constants/
    extensions/
    utils/
  data/
    dto/
    remote/
    repository/
  domain/
    model/
    repository/
    usecases/
  presentation/
    screens/
    state/
    viewmodel/
  di/
```

### Presentation

Contiene las pantallas, estados y ViewModels.

- `AuthActivity`: pantalla de login y registro.
- `MainActivity`: pantalla principal del laboratorio.
- `TrajectoryCanvasView`: vista personalizada para dibujar la trayectoria.
- `AuthViewModel`: coordina autenticacion.
- `ExperimentViewModel`: coordina simulacion, guardado e historial.
- `AuthUiState` y `ExperimentUiState`: estados inmutables para la UI.

### Domain

Contiene reglas de negocio puras, independientes de Android y Firebase.

- Modelos:
  - `ExperimentInput`
  - `ProjectileExperiment`
  - `TrajectoryPoint`
  - `LoginInput`
  - `RegisterInput`
  - `UserSession`
- Casos de uso:
  - `CalculateProjectileExperimentUseCase`
  - `ValidateExperimentInputUseCase`
  - `SaveExperimentUseCase`
  - `ObserveExperimentsUseCase`
  - `LoginUseCase`
  - `RegisterUseCase`
  - `ValidateAuthInputUseCase`
  - `GetCurrentSessionUseCase`
- Repositorios como interfaces:
  - `ExperimentRepository`
  - `AuthRepository`

### Data

Contiene implementaciones concretas de persistencia, red y mapeo.

- `ExperimentRemoteDataSource`: conexion con Firestore.
- `AuthRemoteDataSource`: envio HTTP a los endpoints de autenticacion.
- `JwtParser`: decodifica el JWT para obtener datos del usuario.
- `AuthSessionStorage`: guarda sesion local en `SharedPreferences`.
- DTOs para separar contratos remotos del dominio.
- Repositorios concretos:
  - `ExperimentRepositoryImpl`
  - `AuthRepositoryImpl`

## Endpoints de autenticacion

La URL base esta centralizada en:

```kotlin
app/src/main/java/com/king/kevin/tiroparabolico/core/constants/PhysicsConstants.kt
```

Valor actual:

```text
http://127.0.0.7:8080
```

### Login

Endpoint:

```text
POST /ws/auth/login
```

Body enviado:

```json
{
  "email": "usuario@correo.com",
  "password": "123456"
}
```

Respuesta esperada:

```json
{
  "jsonwebtoken": "eyJ..."
}
```

Tambien se aceptan respuestas con `jwt`, `token`, `accessToken` o el JWT como texto plano.

### Registro

Endpoint:

```text
POST /ws/auth/register
```

Body enviado:

```json
{
  "fullname": "Nombre Completo",
  "password": "123456",
  "email": "usuario@correo.com",
  "nombreInstitucion": "Institucion",
  "codigoCurso": "FIS101"
}
```

`codigoCurso` es opcional y solo se envia si tiene valor.

> Nota: si pruebas desde un emulador Android y el backend corre en tu computador, normalmente debes usar `http://10.0.2.2:8080` en lugar de `127.0.0.7`.

## Firebase Firestore

Los experimentos se guardan en la coleccion:

```text
projectile_experiments
```

Para activar persistencia real:

1. Crear un proyecto en Firebase.
2. Registrar la app Android con el `applicationId`:

```text
com.king.kevin.tiroparabolico
```

3. Descargar `google-services.json`.
4. Ubicarlo en:

```text
app/google-services.json
```

5. Revisar las reglas sugeridas en:

```text
firestore.rules
```

Si Firebase no esta configurado, la app muestra un mensaje de error controlado y evita cierres inesperados.

## Paleta de colores

La interfaz usa una paleta basada en `#034C6F`.

Archivos:

```text
app/src/main/res/values/colors.xml
app/src/main/res/values-night/colors.xml
```

Colores principales:

- Primario: `#034C6F`.
- Primario oscuro: `#02344D`.
- Secundario: azul medio.
- Acento: naranja calido para destacar trayectoria/proyectil y detalles.
- Fondos: neutros azulados claros y oscuros para legibilidad.

## Calidad ISO/IEC 25010

### Calidad interna

- Clean Architecture con separacion Presentation, Domain y Data.
- MVVM para desacoplar UI y logica.
- Repository Pattern para abstraer fuentes remotas.
- Hilt para inyeccion de dependencias.
- Data classes para modelos y DTOs.
- Validaciones encapsuladas en casos de uso.
- Calculos fisicos puros y testeables.
- Bajo acoplamiento entre Android, Firebase, API REST y dominio.

### Calidad externa

- Funcionalidad: formulas fisicas implementadas y probadas.
- Fiabilidad: errores de red, Firebase no configurado y validaciones se manejan sin cerrar la app.
- Usabilidad: interfaz Material 3, formularios claros y mensajes de error.
- Eficiencia: Canvas liviano, Flow/Coroutines para operaciones asincronas.
- Compatibilidad: `minSdk = 26`, compatible con Android 8+.
- Seguridad: validacion de entradas, almacenamiento local de token y reglas Firestore sugeridas.
- Mantenibilidad: organizacion por capas y nombres descriptivos.

### Calidad en uso

- Efectividad: permite simular, guardar y comparar experimentos.
- Eficiencia: flujo directo de ingreso de parametros y simulacion.
- Satisfaccion: interfaz moderna con modo claro/oscuro.
- Prevencion de errores: validacion de velocidad, angulo, gravedad, email y password.
- Accesibilidad: contraste adecuado, textos legibles y componentes Material.

## Pruebas

Las pruebas unitarias estan en:

```text
app/src/test/java/com/king/kevin/tiroparabolico/domain/usecases/CalculateProjectileExperimentUseCaseTest.kt
```

Ejecutar:

```bash
.\gradlew.bat :app:testDebugUnitTest
```

Validan:

- Precision de componentes de velocidad.
- Tiempo de vuelo.
- Altura maxima.
- Alcance horizontal.
- Validacion de velocidad invalida.
- Validacion de angulo invalido.
- Efecto de gravedad personalizada.

## Compilacion

Compilar APK debug:

```bash
.\gradlew.bat :app:assembleDebug
```

APK generado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Consideraciones tecnicas

- El proyecto usa AGP 9.2.1.
- Se mantiene `android.builtInKotlin=false` y `android.newDsl=false` por compatibilidad con KAPT/Hilt en esta configuracion.
- No ejecutar dos tareas Gradle pesadas en paralelo, porque Kotlin puede bloquear caches incrementales. Ejecutar pruebas y ensamblado en secuencia.

## Estructura de recursos UI

Layouts principales:

```text
app/src/main/res/layout/activity_auth.xml
app/src/main/res/layout/activity_main.xml
app/src/main/res/layout/item_experiment.xml
```

Drawables principales:

```text
app/src/main/res/drawable/header_background.xml
app/src/main/res/drawable/panel_background.xml
app/src/main/res/drawable/tint_panel_background.xml
app/src/main/res/drawable/accent_line.xml
```

## Flujo de usuario

1. El usuario abre la app.
2. Se muestra login/registro.
3. Login envia credenciales y recibe JWT.
4. La app decodifica el JWT y guarda la sesion.
5. El usuario ingresa velocidad, angulo y gravedad.
6. La app calcula y anima la trayectoria.
7. El usuario guarda el experimento.
8. El historial remoto se actualiza desde Firestore.
