package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ExperimentInput
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.model.TrajectoryPoint
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class CalculateProjectileExperimentUseCase(
    private val validateExperimentInput: ValidateExperimentInputUseCase
) {
    operator fun invoke(input: ExperimentInput): Result<ProjectileExperiment> {
        return validateExperimentInput(input).mapCatching {
            val angleRadians = Math.toRadians(input.launchAngleDegrees)
            val horizontalVelocity = input.initialVelocity * cos(angleRadians)
            val verticalVelocity = input.initialVelocity * sin(angleRadians)
            val flightTime = (2.0 * input.initialVelocity * sin(angleRadians)) / input.gravity
            val maxHeight = input.initialVelocity.pow(2.0) * sin(angleRadians).pow(2.0) / (2.0 * input.gravity)
            val horizontalRange = input.initialVelocity.pow(2.0) * sin(2.0 * angleRadians) / input.gravity
            val trajectory = buildTrajectory(horizontalVelocity, verticalVelocity, input.gravity, flightTime)

            ProjectileExperiment(
                initialVelocity = input.initialVelocity,
                launchAngleDegrees = input.launchAngleDegrees,
                gravity = input.gravity,
                horizontalVelocity = horizontalVelocity,
                verticalVelocity = verticalVelocity,
                flightTime = flightTime,
                maxHeight = maxHeight,
                horizontalRange = horizontalRange.coerceAtLeast(0.0),
                trajectory = trajectory
            )
        }
    }

    private fun buildTrajectory(
        horizontalVelocity: Double,
        verticalVelocity: Double,
        gravity: Double,
        flightTime: Double
    ): List<TrajectoryPoint> {
        if (flightTime <= 0.0) {
            return listOf(TrajectoryPoint(time = 0.0, x = 0.0, y = 0.0))
        }

        val samples = 100
        return (0..samples).map { index ->
            val time = flightTime * index / samples
            val x = horizontalVelocity * time
            val currentVerticalVelocity = verticalVelocity - (gravity * time)
            val y = (verticalVelocity * time) - (0.5 * gravity * time.pow(2.0))
            
            val instantaneousVelocity = kotlin.math.sqrt(horizontalVelocity.pow(2.0) + currentVerticalVelocity.pow(2.0))
            val instantaneousAngle = Math.toDegrees(kotlin.math.atan2(currentVerticalVelocity, horizontalVelocity))
            
            TrajectoryPoint(
                time = time,
                x = x,
                y = y.coerceAtLeast(0.0),
                instantaneousVelocity = instantaneousVelocity,
                instantaneousAngle = instantaneousAngle
            )
        }
    }
}
