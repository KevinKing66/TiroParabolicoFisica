package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.TrajectoryPoint

data class TrajectoryPointDto(
    val time: Double = 0.0,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val velocity: Double = 0.0,
    val angle: Double = 0.0
) {
    fun toDomain(): TrajectoryPoint = TrajectoryPoint(
        time = time,
        x = x,
        y = y,
        instantaneousVelocity = velocity,
        instantaneousAngle = angle
    )
}

fun TrajectoryPoint.toDto(): TrajectoryPointDto = TrajectoryPointDto(
    time = time,
    x = x,
    y = y,
    velocity = instantaneousVelocity,
    angle = instantaneousAngle
)
