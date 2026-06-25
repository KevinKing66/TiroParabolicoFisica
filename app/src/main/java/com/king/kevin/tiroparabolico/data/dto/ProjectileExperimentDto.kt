package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment

data class ProjectileExperimentDto(
    val userCode: String = "",
    val initialVelocity: Double = 0.0,
    val launchAngleDegrees: Double = 0.0,
    val gravity: Double = 0.0,
    val horizontalVelocity: Double = 0.0,
    val verticalVelocity: Double = 0.0,
    val flightTime: Double = 0.0,
    val maxHeight: Double = 0.0,
    val horizontalRange: Double = 0.0,
    val trajectory: List<TrajectoryPointDto> = emptyList(),
    val createdAtMillis: Long = 0L
) {
    fun toDomain(id: String): ProjectileExperiment {
        return ProjectileExperiment(
            id = id,
            initialVelocity = initialVelocity,
            launchAngleDegrees = launchAngleDegrees,
            gravity = gravity,
            horizontalVelocity = horizontalVelocity,
            verticalVelocity = verticalVelocity,
            flightTime = flightTime,
            maxHeight = maxHeight,
            horizontalRange = horizontalRange,
            trajectory = trajectory.map { it.toDomain() },
            createdAtMillis = createdAtMillis
        )
    }
}

fun ProjectileExperiment.toDto(userCode: String = ""): ProjectileExperimentDto {
    return ProjectileExperimentDto(
        userCode = userCode,
        initialVelocity = initialVelocity,
        launchAngleDegrees = launchAngleDegrees,
        gravity = gravity,
        horizontalVelocity = horizontalVelocity,
        verticalVelocity = verticalVelocity,
        flightTime = flightTime,
        maxHeight = maxHeight,
        horizontalRange = horizontalRange,
        trajectory = trajectory.map { it.toDto() },
        createdAtMillis = createdAtMillis
    )
}
