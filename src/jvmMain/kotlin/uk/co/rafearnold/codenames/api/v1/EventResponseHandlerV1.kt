package uk.co.rafearnold.codenames.api.v1

import uk.co.rafearnold.codenames.event.GameEventResponse

interface EventResponseHandlerV1 {

    fun handle(response: GameEventResponse)
}
