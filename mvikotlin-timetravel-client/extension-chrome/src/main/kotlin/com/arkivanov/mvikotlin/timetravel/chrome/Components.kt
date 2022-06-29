@file:Suppress("MatchingDeclarationName")

package com.arkivanov.mvikotlin.timetravel.chrome

import mui.material.IconButton
import react.FC
import react.PropsWithChildren

external interface ImageButtonProps : PropsWithChildren {
    var iconName: String
    var title: String
    var isEnabled: Boolean
    var onClick: () -> Unit
}

val ImageButton: FC<ImageButtonProps> = FC { props ->
    IconButton {
        title = props.title
        disabled = !props.isEnabled
        onClick = { props.onClick() }
        +props.children
    }
}
