package com.masakasakasama.reisedeutsch.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun AnimatedTravelCompanion() {
    ReiseCard(Modifier.fillMaxWidth()) {
        CompanionCharacter(Modifier.fillMaxWidth().height(310.dp),controls=true)
    }
}

/** Every character placement uses the same real volume mesh and skeleton. */
@Composable fun CompanionCharacter(modifier: Modifier=Modifier,controls:Boolean=false) {
    RiggedCompanion(modifier,controls)
}
