package com.masakasakasama.reisedeutsch.data

import org.json.JSONArray
import org.json.JSONObject

data class PhraseCard(val id:String,val german:String,val japanese:String,val scene:String,val kind:String,val city:String="",val plural:String="",val grammar:String="",val acceptedAnswers:List<String> = listOf(german))
data class Lesson(val id:String,val city:String,val scene:String,val title:String,val cardIds:List<String>,val grammar:String)
data class City(val id:String,val name:String,val order:Int,val lessonIds:List<String>)
data class Turn(val speaker:String,val german:String,val japanese:String,val cardId:String?)
data class Conversation(val id:String,val scene:String,val title:String,val turns:List<Turn>)
data class ContentPack(val cards:List<PhraseCard>,val lessons:List<Lesson>,val cities:List<City>,val conversations:List<Conversation>){
    fun card(id:String)=cards.firstOrNull{it.id==id}
    fun lesson(id:String)=lessons.firstOrNull{it.id==id}
    companion object{
        private fun JSONArray.strings()=(0 until length()).map{getString(it)}
        fun parse(json:String):ContentPack{
            val root=JSONObject(json)
            require(root.getInt("schemaVersion")==1&&root.getString("language")=="de"&&root.getString("uiLanguage")=="ja")
            val cards=root.getJSONArray("cards").let{a->(0 until a.length()).map{i->val c=a.getJSONObject(i);PhraseCard(c.getString("id"),c.getString("german"),c.getString("japanese"),c.optString("scene"),c.getString("kind"),c.optString("city"),c.optString("plural"),c.optString("grammar"),c.optJSONArray("acceptedAnswers")?.strings()?:listOf(c.getString("german")))}}
            require(cards.map{it.id}.toSet().size==cards.size&&cards.all{it.german.isNotBlank()&&it.japanese.isNotBlank()})
            val lessons=root.getJSONArray("lessons").let{a->(0 until a.length()).map{i->val x=a.getJSONObject(i);Lesson(x.getString("id"),x.getString("city"),x.getString("scene"),x.getString("title"),x.getJSONArray("phraseCardIds").strings(),x.getString("grammar"))}}
            require(lessons.size==20&&lessons.all{it.cardIds.size==3&&it.cardIds.all{id->cards.any{it.id==id}}})
            val cities=root.getJSONArray("cities").let{a->(0 until a.length()).map{i->val x=a.getJSONObject(i);City(x.getString("id"),x.getString("nameDe"),x.getInt("order"),x.getJSONArray("lessonIds").strings())}}
            require(cities.size==5&&cities.all{c->c.lessonIds.size==4&&c.lessonIds.all{id->lessons.any{it.id==id&&it.city==c.name}}})
            val conversations=root.getJSONArray("conversations").let{a->(0 until a.length()).map{i->val x=a.getJSONObject(i);val turns=x.getJSONArray("turns");Conversation(x.getString("id"),x.getString("scene"),x.getString("titleJa"),(0 until turns.length()).map{j->val t=turns.getJSONObject(j);Turn(t.getString("speaker"),t.getString("german"),t.getString("japanese"),t.optString("cardId").takeIf{it.isNotBlank()})})}}
            require(conversations.size==12&&conversations.all{it.turns.size==3&&it.turns.all{t->t.german.isNotBlank()&&t.japanese.isNotBlank()}})
            return ContentPack(cards,lessons,cities,conversations)
        }
    }
}
