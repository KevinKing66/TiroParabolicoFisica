# Agregar Apartados de Análisis y Retos con Persistencia en Firebase

Este plan describe la implementación de dos nuevas secciones educativas ("Preguntas de Análisis" y "Retos de Decisión") para el Laboratorio Virtual de Física, incluyendo la persistencia de las respuestas de los estudiantes en Firebase Firestore.

## User Review Required

- **Diseño de Pantallas**: Se crearán `AnalysisActivity` y `ChallengesActivity` con campos de texto para respuestas.
- **Persistencia**: Se guardarán las respuestas en una nueva colección de Firestore vinculada a la sesión del usuario si está disponible.
- **Validación**: Se requerirá que todos los campos tengan texto antes de permitir el guardado para asegurar la calidad de la entrega académica.

## Proposed Changes

### 1. Interfaz de Usuario (Layouts)

#### [activity_main.xml](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/res/layout/activity_main.xml)
- Añadir panel `educationalPanel` con botones `btnAnalysis` y `btnChallenges` antes del historial.

#### [NEW] [activity_analysis.xml](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/res/layout/activity_analysis.xml)
- `NestedScrollView` con 5 `MaterialCardView`, cada una con un `TextView` para la pregunta y un `TextInputLayout` con `TextInputEditText` para la respuesta.
- Botón "Guardar Respuestas" al final.

#### [NEW] [activity_challenges.xml](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/res/layout/activity_challenges.xml)
- Similar a `activity_analysis.xml` pero con los 3 retos de decisión.

---

### 2. Modelo de Datos y Repositorio (Domain & Data)

#### [NEW] [AcademicResponse.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/domain/model/AcademicResponse.kt)
- Entidad que contiene el tipo de sección (Análisis o Reto), un mapa de pregunta-respuesta, y metadatos (timestamp, userId).

#### [NEW] [AcademicResponseDto.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/data/dto/AcademicResponseDto.kt)
- DTO para la serialización en Firestore.

#### [AcademicRepository.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/domain/repository/AcademicRepository.kt)
- Interfaz con el método `saveAcademicResponse(response: AcademicResponse): Result<Unit>`.

---

### 3. Implementación de Persistencia (Firebase)

#### [AcademicRemoteDataSource.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/data/remote/AcademicRemoteDataSource.kt)
- Lógica para insertar documentos en la colección `academic_responses` de Firestore.

#### [PhysicsConstants.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/core/constants/PhysicsConstants.kt)
- Añadir constante `ACADEMIC_COLLECTION = "academic_responses"`.

---

### 4. Lógica de Presentación (Activities & ViewModel)

#### [AnalysisActivity.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/presentation/screens/AnalysisActivity.kt)
- Gestionar la recolección de textos y llamar al repositorio para guardar.

#### [ChallengesActivity.kt](file:///C:/Users/kevin/AndroidStudioProjects/TiroParabolico/app/src/main/java/com/king/kevin/tiroparabolico/presentation/screens/ChallengesActivity.kt)
- Lógica similar para los retos.

## Verification Plan

### Manual Verification
1. Navegar a "Preguntas de Análisis" desde el Main.
2. Completar todas las preguntas y presionar "Guardar".
3. Verificar mensaje de éxito (Toast).
4. (Opcional si hay acceso a consola) Verificar en Firestore que el documento se creó correctamente en `academic_responses`.
5. Repetir para "Retos de Decisión".
