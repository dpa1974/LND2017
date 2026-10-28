package com.dpa.cuentaatras

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountdownTest {

    @Test
    fun desglosaDiasHorasMinutosSegundos() {
        val target = ((2 * 86_400) + (3 * 3_600) + (4 * 60) + 5) * 1000L
        assertEquals(Remaining(2, 3, 4, 5), remainingUntil(target, nowMillis = 0))
    }

    @Test
    fun descartaLosMilisegundosSobrantes() {
        assertEquals(Remaining(0, 0, 0, 1), remainingUntil(1_999, nowMillis = 0))
    }

    @Test
    fun fechaPasadaDevuelveCeroYTerminado() {
        val r = remainingUntil(targetMillis = 1_000, nowMillis = 5_000)
        assertEquals(Remaining(0, 0, 0, 0), r)
        assertTrue(r.isOver)
    }

    @Test
    fun noTerminaMientrasQuedeAlgunSegundo() {
        assertFalse(remainingUntil(1_000, nowMillis = 0).isOver)
    }
}
