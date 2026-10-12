package com.masakasakasama.reisedeutsch.learning

import com.masakasakasama.reisedeutsch.data.PhraseCard
import java.text.Normalizer
import java.util.Locale

object AnswerGrader{
    fun normalize(value:String):String=Normalizer.normalize(value,Normalizer.Form.NFC).trim().replace(Regex("\\s+")," ").replace(Regex("[.!?]+$"),"").trim()
    fun matches(card:PhraseCard,answer:String):Boolean{
        val actual=normalize(answer)
        if(actual.isBlank())return false
        val candidates=card.acceptedAnswers.ifEmpty{listOf(card.german)}
        return candidates.any{expected->
            if(card.kind=="PHRASE")normalize(expected).lowercase(Locale.ROOT)==actual.lowercase(Locale.ROOT)
            else normalize(expected)==actual
        }
    }
}
