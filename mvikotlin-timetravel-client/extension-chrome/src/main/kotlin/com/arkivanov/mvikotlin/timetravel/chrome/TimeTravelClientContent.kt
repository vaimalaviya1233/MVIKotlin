package com.arkivanov.mvikotlin.timetravel.chrome

import com.arkivanov.mvikotlin.timetravel.client.internal.client.TimeTravelClient
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.value.ValueNode
import com.badoo.reaktive.observable.subscribe
import csstype.AlignItems
import csstype.Cursor
import csstype.Display
import csstype.Flex
import csstype.FlexFlow
import csstype.FlexWrap
import csstype.FontFamily
import csstype.FontSize
import csstype.NamedColor
import csstype.None
import csstype.Overflow
import csstype.Padding
import csstype.WhiteSpace
import csstype.number
import csstype.pct
import csstype.px
import csstype.vh
import csstype.vw
import emotion.react.css
import kotlinx.browser.window
import mui.icons.material.BugReport
import mui.icons.material.ChevronLeft
import mui.icons.material.ChevronRight
import mui.icons.material.Close
import mui.icons.material.FiberManualRecord
import mui.icons.material.Phonelink
import mui.icons.material.PhonelinkOff
import mui.icons.material.SkipNext
import mui.icons.material.SkipPrevious
import mui.icons.material.Stop
import mui.material.Typography
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul
import react.useEffectOnce
import react.useState

external interface TimeTravelClientContentProps : Props {
    var client: TimeTravelClient
}

val TimeTravelClientContent: FC<TimeTravelClientContentProps> = FC { props ->
    val client = props.client

    var model by useState { client.models.value }

    useEffectOnce {
        val disposable = client.models.subscribe {
            console.log(it)
            model = it
        }
        cleanup(disposable::dispose)
    }

    div {
        css {
            width = 100.vw
            height = 100.vh
            display = Display.flex
            flexFlow = FlexFlow.column
            flexWrap = FlexWrap.nowrap
        }

        ButtonBar {
            buttons = model.buttons
            onConnect = client::onConnectClicked
            onDisconnect = client::onDisconnectClicked
            onStartRecording = client::onStartRecordingClicked
            onStopRecording = client::onStopRecordingClicked
            onMoveToStart = client::onMoveToStartClicked
            onStepBackward = client::onStepBackwardClicked
            onStepForward = client::onStepForwardClicked
            onMoveToEnd = client::onMoveToEndClicked
            onCancel = client::onCancelClicked
            onDebug = client::onDebugEventClicked
        }

        Events {
            console.log(model.events)
            events = model.events
            currentEventIndex = model.currentEventIndex
            selectedEventIndex = model.selectedEventIndex
            selectedEventValue = model.selectedEventValue
            onClick = client::onEventSelected
        }
    }

    model.errorText?.also {
        useEffectOnce {
            window.alert(it)
            client.onDismissErrorClicked()
        }
    }
}

//@Composable
//fun TimeTravelClientContent(client: TimeTravelClient) {
//    val model by client.models.subscribeAsState()
//
//    Div(
//        attrs = {
//            style {
//                width(100.percent)
//                height(100.vh)
//                display(DisplayStyle.Flex)
//                flexFlow(FlexDirection.Column, FlexWrap.Nowrap)
//            }
//        }
//    ) {
//        ButtonBar(
//            buttons = model.buttons,
//            onConnect = client::onConnectClicked,
//            onDisconnect = client::onDisconnectClicked,
//            onStartRecording = client::onStartRecordingClicked,
//            onStopRecording = client::onStopRecordingClicked,
//            onMoveToStart = client::onMoveToStartClicked,
//            onStepBackward = client::onStepBackwardClicked,
//            onStepForward = client::onStepForwardClicked,
//            onMoveToEnd = client::onMoveToEndClicked,
//            onCancel = client::onCancelClicked,
//            onDebug = client::onDebugEventClicked,
//            attrs = {
//                style {
//                    flex("0 1 auto")
//                }
//            }
//        )
//
//        Events(
//            events = model.events,
//            currentEventIndex = model.currentEventIndex,
//            selectedEventIndex = model.selectedEventIndex,
//            selectedEventValue = model.selectedEventValue,
//            attrs = {
//                style {
//                    flex("1 1 auto")
//                    overflowY("hidden")
//                }
//            },
//            onClick = client::onEventSelected,
//        )
//    }
//
//    model.errorText?.also {
//        DisposableEffect(it) {
//            window.alert(it)
//            client.onDismissErrorClicked()
//            onDispose {}
//        }
//    }
//}

external interface ButtonBarProps : Props {
    var buttons: TimeTravelClient.Model.Buttons
    var onConnect: () -> Unit
    var onDisconnect: () -> Unit
    var onStartRecording: () -> Unit
    var onStopRecording: () -> Unit
    var onMoveToStart: () -> Unit
    var onStepBackward: () -> Unit
    var onStepForward: () -> Unit
    var onMoveToEnd: () -> Unit
    var onCancel: () -> Unit
    var onDebug: () -> Unit
}

val ButtonBar: FC<ButtonBarProps> = FC { props ->
    div {
        css {
            flex = Flex(grow = number(0.0), shrink = number(1.0))

            width = 100.pct
            display = Display.flex
            flexFlow = FlexFlow.row
            flexWrap = FlexWrap.nowrap
            alignItems = AlignItems.center
        }

        ConnectionButtons {
            buttons = props.buttons
            onConnect = props.onConnect
            onDisconnect = props.onDisconnect
        }

        ControlButtons {
            buttons = props.buttons
            onStartRecording = props.onStartRecording
            onStopRecording = props.onStopRecording
            onMoveToStart = props.onMoveToStart
            onStepBackward = props.onStepBackward
            onStepForward = props.onStepForward
            onMoveToEnd = props.onMoveToEnd
            onCancel = props.onCancel
            onDebug = props.onDebug
        }
    }
}

external interface ControlButtonsProps : Props {
    var buttons: TimeTravelClient.Model.Buttons
    var onStartRecording: () -> Unit
    var onStopRecording: () -> Unit
    var onMoveToStart: () -> Unit
    var onStepBackward: () -> Unit
    var onStepForward: () -> Unit
    var onMoveToEnd: () -> Unit
    var onCancel: () -> Unit
    var onDebug: () -> Unit
}

val ControlButtons: FC<ControlButtonsProps> = FC { props ->
    ImageButton {
        title = "Start Recording"
        isEnabled = props.buttons.isStartRecordingEnabled
        onClick = props.onStartRecording

        FiberManualRecord()
    }

    ImageButton {
        title = "Stop recording"
        isEnabled = props.buttons.isStopRecordingEnabled
        onClick = props.onStopRecording

        Stop()
    }

    ImageButton {
        title = "Move to start"
        isEnabled = props.buttons.isMoveToStartEnabled
        onClick = props.onMoveToStart

        SkipPrevious()
    }

    ImageButton {
        title = "Step backward"
        isEnabled = props.buttons.isStepBackwardEnabled
        onClick = props.onStepBackward

        ChevronLeft()
    }

    ImageButton {
        title = "Step forward"
        isEnabled = props.buttons.isStepForwardEnabled
        onClick = props.onStepForward

        ChevronRight()
    }

    ImageButton {
        title = "Move to end"
        isEnabled = props.buttons.isMoveToEndEnabled
        onClick = props.onMoveToEnd

        SkipNext()
    }

    ImageButton {
        title = "Cancel"
        isEnabled = props.buttons.isCancelEnabled
        onClick = props.onCancel

        Close()
    }

    ImageButton {
        title = "Debug"
        isEnabled = props.buttons.isDebugEventEnabled
        onClick = props.onDebug

        BugReport()
    }
}

external interface ConnectionButtonsProps : Props {
    var buttons: TimeTravelClient.Model.Buttons
    var onConnect: () -> Unit
    var onDisconnect: () -> Unit
}

val ConnectionButtons: FC<ConnectionButtonsProps> = FC { props ->
    ImageButton {
        iconName = "phonelink"
        title = "Connect to application"
        isEnabled = props.buttons.isConnectEnabled
        onClick = {
            println("Connecting")
            props.onConnect()
        }

        Phonelink()
    }

    ImageButton {
        title = "Disconnect from application"
        isEnabled = props.buttons.isDisconnectEnabled
        onClick = props.onDisconnect

        PhonelinkOff()
    }
}

external interface EventsProps : Props {
    var events: List<String>
    var currentEventIndex: Int
    var selectedEventIndex: Int
    var selectedEventValue: ValueNode?
    var onClick: (Int) -> Unit
}

val Events: FC<EventsProps> = FC { props ->
    div {
        css {
            flex = Flex(grow = number(1.0), shrink = number(1.0))
            overflowY = Overflow.hidden

            width = 100.pct
            display = Display.flex
            flexFlow = FlexFlow.row
            flexWrap = FlexWrap.nowrap
        }

        EventList {
            events = props.events
            currentEventIndex = props.currentEventIndex
            selectedEventIndex = props.selectedEventIndex
            onClick = props.onClick
        }

        EventDetails {
            value = props.selectedEventValue
        }
    }
}

external interface EventListProps : Props {
    var events: List<String>
    var currentEventIndex: Int
    var selectedEventIndex: Int
    var onClick: (Int) -> Unit
}

val EventList: FC<EventListProps> = FC { props ->
    ul {
        css {
            flex = Flex(grow = number(2.0), shrink = number(1.0), basis = 0.px)
            overflow = Overflow.scroll
            listStyleType = None.none
            padding = 0.px

            width = 100.pct
            margin = 0.px
        }

        props.events.forEachIndexed { index, item ->
            Event {
                text = item
                isActive = index <= props.currentEventIndex
                isSelected = index == props.selectedEventIndex
                onClick = { props.onClick(index) }
            }
        }
    }
}

external interface EventProps : Props {
    var text: String
    var isActive: Boolean
    var isSelected: Boolean
    var onClick: () -> Unit
}

val Event: FC<EventProps> = FC { props ->
    li {
        css {
            width = 100.pct
            padding = Padding(vertical = 0.px, horizontal = 16.px)
            cursor = Cursor.pointer

            if (props.isSelected) {
                backgroundColor = NamedColor.lightgray
            }
        }

        onClick = { props.onClick() }

        Typography {
            css {
                height = 24.px
                whiteSpace = WhiteSpace.nowrap
                display = Display.flex
                alignItems = AlignItems.center
                color = if (props.isActive) NamedColor.black else NamedColor.gray
            }

            +props.text
        }
    }
}

external interface EventDetailsProps : Props {
    var value: ValueNode?
}

val EventDetails: FC<EventDetailsProps> = FC { props ->
    Typography {
        sx {
            flex = Flex(grow = number(3.0), shrink = number(1.0), basis = 0.px)
            overflow = Overflow.scroll

            width = 100.pct
            padding = 16.px
            whiteSpace = WhiteSpace.pre
            fontFamily = FontFamily.monospace
            fontSize = FontSize.small
        }

        +(props.value?.title ?: "")
    }
}
