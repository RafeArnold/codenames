package uk.co.rafearnold.codenames.event

interface EventRequestHandler {

    fun handle(event: GameEventRequest): GameEventResponse
}
