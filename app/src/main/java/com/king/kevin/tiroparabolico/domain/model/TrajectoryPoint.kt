package com.king.kevin.tiroparabolico.domain.model

data class TrajectoryPoint(
    val time: Double,
    val x: Double,
    val y: Double,
    val instantaneousVelocity: Double = 0.0,
    val instantaneousAngle: Double = 0.0
)
