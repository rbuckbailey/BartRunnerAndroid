package `in`.izyum.bart.ui

import `in`.izyum.bart.model.Departure
import `in`.izyum.bart.model.Line
import `in`.izyum.bart.model.Station
import `in`.izyum.bart.model.TripLeg
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NextArrivalProjectionTest {

    @Test
    fun nextArrivalSelectsNextDepartureOnRouteAcrossDifferentColorLines() {
        val redLineDeparture = createDeparture("red-1", Line.RED, estimate = 1000L, requiresTransfer = false)
        val yellowLineDeparture = createDeparture("yellow-1", Line.YELLOW, estimate = 2000L, requiresTransfer = false)
        val orangeLineDeparture = createDeparture("orange-1", Line.ORANGE, estimate = 3000L, requiresTransfer = false)

        val candidateDepartures = listOf(redLineDeparture, yellowLineDeparture, orangeLineDeparture)

        val nextDeparture = selectNextDeparture(redLineDeparture, candidateDepartures, showTransfers = true)

        assertNotNull(nextDeparture)
        assertEquals("yellow-1", nextDeparture?.tripLegs?.firstOrNull()?.tripId)
        assertEquals(Line.YELLOW, nextDeparture?.line)
    }

    @Test
    fun nextArrivalSkipsTransferDepartureWhenShowTransfersIsFalse() {
        val directDeparture1 = createDeparture("red-1", Line.RED, estimate = 1000L, requiresTransfer = false)
        val transferDeparture = createDeparture("xfer-1", Line.YELLOW, estimate = 2000L, requiresTransfer = true)
        val directDeparture2 = createDeparture("orange-1", Line.ORANGE, estimate = 3000L, requiresTransfer = false)

        val allDepartures = listOf(directDeparture1, transferDeparture, directDeparture2)

        val filteredDepartures = allDepartures.filter { !it.requiresTransfer && !it.hasTransfers() }
        val nextDeparture = selectNextDeparture(directDeparture1, filteredDepartures, showTransfers = false)

        assertNotNull(nextDeparture)
        assertEquals("orange-1", nextDeparture?.tripLegs?.firstOrNull()?.tripId)
        assertEquals(Line.ORANGE, nextDeparture?.line)
    }

    @Test
    fun nextArrivalIncludesTransferDepartureWhenShowTransfersIsTrue() {
        val directDeparture1 = createDeparture("red-1", Line.RED, estimate = 1000L, requiresTransfer = false)
        val transferDeparture = createDeparture("xfer-1", Line.YELLOW, estimate = 2000L, requiresTransfer = true)
        val directDeparture2 = createDeparture("orange-1", Line.ORANGE, estimate = 3000L, requiresTransfer = false)

        val allDepartures = listOf(directDeparture1, transferDeparture, directDeparture2)

        val nextDeparture = selectNextDeparture(directDeparture1, allDepartures, showTransfers = true)

        assertNotNull(nextDeparture)
        assertEquals("xfer-1-1", nextDeparture?.tripLegs?.firstOrNull()?.tripId)
        assertEquals(Line.YELLOW, nextDeparture?.line)
    }

    private fun selectNextDeparture(
        departure: Departure,
        candidateDepartures: List<Departure>,
        showTransfers: Boolean,
    ): Departure? {
        val filtered = if (showTransfers) {
            candidateDepartures
        } else {
            candidateDepartures.filter { !it.requiresTransfer && !it.hasTransfers() }
        }
        val currentIndex = filtered.indexOfFirst { it.identity == departure.identity }
        val next = if (currentIndex >= 0 && currentIndex + 1 < filtered.size) {
            filtered[currentIndex + 1]
        } else {
            filtered.firstOrNull { candidate ->
                candidate.identity != departure.identity &&
                    candidate.getMeanEstimate() > departure.getMeanEstimate()
            }
        }
        return next?.takeIf { it.identity != departure.identity }
    }

    private fun createDeparture(
        tripId: String,
        line: Line,
        estimate: Long,
        requiresTransfer: Boolean,
    ): Departure = Departure.builder()
        .setOrigin(Station.MONT)
        .setTrainDestination(Station.RICH)
        .setPassengerDestination(Station.RICH)
        .setLine(line)
        .setDirection("north")
        .setPlatform("1")
        .setMinEstimate(estimate)
        .setMaxEstimate(estimate + 60_000L)
        .setRequiresTransfer(requiresTransfer)
        .setTripLegs(if (requiresTransfer) {
            listOf(
                TripLeg(line, Station.MONT, Station._19TH, Station._19TH, "$tripId-1", estimate, estimate + 10_000L, emptyList()),
                TripLeg(Line.RED, Station._19TH, Station.RICH, Station.RICH, "$tripId-2", estimate + 12_000L, estimate + 20_000L, emptyList()),
            )
        } else {
            listOf(TripLeg(line, Station.MONT, Station.RICH, Station.RICH, tripId, estimate, estimate + 20_000L, emptyList()))
        })
        .build()
}
