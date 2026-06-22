package com.king.kevin.tiroparabolico.domain.model

data class ProjectileExperiment(
    val id: String = "",
    val initialVelocity: Double,
    val launchAngleDegrees: Double,
    val gravity: Double,
    val horizontalVelocity: Double,
    val verticalVelocity: Double,
    val flightTime: Double,
    val maxHeight: Double,
    val horizontalRange: Double,
    val trajectory: List<TrajectoryPoint>,
    val createdAtMillis: Long = System.currentTimeMillis()
)
