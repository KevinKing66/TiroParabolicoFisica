package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ExperimentInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateProjectileExperimentUseCaseTest {
    private lateinit var useCase: CalculateProjectileExperimentUseCase

    @Before
    fun setUp() {
        useCase = CalculateProjectileExperimentUseCase(ValidateExperimentInputUseCase())
    }

    @Test
    fun `calculate projectile experiment returns precise physics values`() {
        val result = useCase(
            ExperimentInput(
                initialVelocity = 10.0,
                launchAngleDegrees = 45.0,
                gravity = 9.81
            )
        )

        assertTrue(result.isSuccess)
        val experiment = result.getOrThrow()
        assertEquals(7.071, experiment.horizontalVelocity, 0.001)
        assertEquals(7.071, experiment.verticalVelocity, 0.001)
        assertEquals(1.442, experiment.flightTime, 0.001)
        assertEquals(2.548, experiment.maxHeight, 0.001)
        assertEquals(10.194, experiment.horizontalRange, 0.001)
        assertTrue(experiment.trajectory.size >= 60)
    }

    @Test
    fun `invalid velocity fails validation`() {
        val result = useCase(
            ExperimentInput(
                initialVelocity = 0.0,
                launchAngleDegrees = 45.0,
                gravity = 9.81
            )
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `invalid angle fails validation`() {
        val result = useCase(
            ExperimentInput(
                initialVelocity = 10.0,
                launchAngleDegrees = 91.0,
                gravity = 9.81
            )
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `custom gravity changes range consistently`() {
        val earth = useCase(ExperimentInput(10.0, 45.0, 9.81)).getOrThrow()
        val moon = useCase(ExperimentInput(10.0, 45.0, 1.62)).getOrThrow()

        assertTrue(moon.horizontalRange > earth.horizontalRange)
        assertTrue(moon.flightTime > earth.flightTime)
    }
}
