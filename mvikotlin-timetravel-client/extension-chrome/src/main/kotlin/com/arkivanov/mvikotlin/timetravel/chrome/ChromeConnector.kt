package com.arkivanov.mvikotlin.timetravel.chrome

import com.arkivanov.mvikotlin.timetravel.client.internal.client.Connector
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetraveleventvalue.TimeTravelEventValue
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelexport.TimeTravelExport
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelstateupdate.TimeTravelStateUpdate
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.ProtoDecoder
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.ProtoEncoder
import com.badoo.reaktive.base.setCancellable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableEmitter
import com.badoo.reaktive.observable.observable
import com.badoo.reaktive.observable.onErrorReturn
import com.badoo.reaktive.observable.takeUntil

class ChromeConnector : Connector {

    override fun connect(): Observable<Connector.Event> =
        observable<Connector.Event> { ConnectorImpl(it).connect() }
            .takeUntil { it is Connector.Event.Error }
            .onErrorReturn { Connector.Event.Error(it.message) }

    private class ConnectorImpl(
        private val emitter: ObservableEmitter<Connector.Event>
    ) {
        private var scriptPort: chrome.runtime.Port? = null

        fun connect() {
            println("Connect")
            val onClientConnected = ::onClientConnected

            chrome.runtime.onConnect.addListener(onClientConnected)

            emitter.setCancellable {
                chrome.runtime.onConnect.removeListener(onClientConnected)
                scriptPort?.disconnect()
                scriptPort = null
            }

            queryTabs(::injectContentScript)
        }

        private fun queryTabs(callback: (chrome.tabs.Tab) -> Unit) {
            chrome.tabs.query(
                jsObject {
                    active = true
                    currentWindow = true
                }
            ) { tabs ->
                if (tabs.isNotEmpty()) {
                    callback(tabs[0])
                } else {
                    emitter.onNext(Connector.Event.Error("No active tab"))
                }
            }
        }

        private fun injectContentScript(tab: chrome.tabs.Tab) {
            chrome.scripting.executeScript(
                jsObject {
                    target = jsObject { tabId = tab.id }
                    func = js("contentScript")
                }
            ) { results ->
                if (results.firstOrNull()?.result != 0) {
                    emitter.onNext(Connector.Event.Error("Error injecting content script"))
                }
            }
        }

        private fun onClientConnected(port: chrome.runtime.Port) {
            println("Client connected")
            val protoDecoder = ProtoDecoder()

            port.onMessage.addListener { message, _ ->
                val proto = protoDecoder.decode(message.unsafeCast<ByteArray>())
                emitter.onNext(
                    when (proto) {
                        is TimeTravelStateUpdate -> Connector.Event.StateUpdate(proto)
                        is TimeTravelEventValue -> Connector.Event.EventValue(eventId = proto.eventId, value = proto.value)
                        is TimeTravelExport -> Connector.Event.ExportEvents(proto.data)
                        else -> Connector.Event.Error(text = "Unsupported proto object type: $proto")
                    }
                )
            }

            port.onDisconnect.addListener {
                emitter.onNext(Connector.Event.Error("Client disconnected"))
            }

            if (scriptPort == null) {
                scriptPort = port
                val protoEncoder = ProtoEncoder { data, size -> port.postMessage(data.copyOf(size)) }
                emitter.onNext(Connector.Event.Connected(protoEncoder::encode))
            }
        }
    }
}
