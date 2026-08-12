package com.manhtu.jsontoview.model

/**
 * Declarative action emitted by the backend and handled by the app.
 *
 * @param type semantic type, e.g. `open_url`, `navigate`, `custom`
 * @param payload free-form data (URL, route id, JSON string, …)
 */
data class NodeAction(
    val type: String,
    val payload: String? = null,
)
