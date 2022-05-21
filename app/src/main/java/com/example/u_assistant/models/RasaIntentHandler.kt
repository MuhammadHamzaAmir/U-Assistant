package com.example.u_assistant.models

import android.app.Activity
import android.content.Intent
import android.provider.AlarmClock
import android.provider.ContactsContract

sealed class RasaIntentHandler(val intent: RasaIntent) {
    class PhoneCall(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
            with(activity) {
                val intent = Intent(Intent.ACTION_DIAL)
                startActivity(intent)
            }
        }

        override fun invoke(activity: Activity, args: List<String>) {
            if (args.isEmpty()) invoke(activity)
        }
    }

    class AddContact(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
            with(activity) {
                val intent = Intent(ContactsContract.Intents.Insert.ACTION)
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                startActivity(intent)
            }
        }

        override fun invoke(activity: Activity, args: List<String>) {

        }
    }

    class RemoveContact(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
        }

        override fun invoke(activity: Activity, args: List<String>) {

        }
    }

    class ShowWeather(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {

        }

        override fun invoke(activity: Activity, args: List<String>) {

        }
    }

    class OpenApp(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {

        }

        override fun invoke(activity: Activity, args: List<String>) {

        }
    }

    class SetAnAlarm(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
            with(activity) {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        override fun invoke(activity: Activity, args: List<String>) {

        }
    }

    abstract operator fun invoke(activity: Activity)
    abstract operator fun invoke(activity: Activity, args: List<String>)
}

fun RasaIntent.handle() = when (name) {
    "make_phone_call" -> RasaIntentHandler.PhoneCall(this)
    "add_contact" -> RasaIntentHandler.AddContact(this)
    "remove_contact" -> RasaIntentHandler.RemoveContact(this)
    "show_weather" -> RasaIntentHandler.ShowWeather(this)
    "open_app" -> RasaIntentHandler.OpenApp(this)
    "set_an_alarm" -> RasaIntentHandler.SetAnAlarm(this)
    else -> throw IllegalArgumentException("Unknown intent: $name")
}