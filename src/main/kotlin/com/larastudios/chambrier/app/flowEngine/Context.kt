package com.larastudios.chambrier.app.flowEngine

/**
 * The context should be an immutable data class that describes the context in
 * which the flow is executed. It could contain the current user, the
 * permissions that can be used, the data the flow can use but not modify.
 *
 * Data that should be stored and passed to other nodes should be stored in the
 * [Scope].
 */
interface Context
