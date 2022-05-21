package com.example.u_assistant.models

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.widget.Toast
import java.util.*

sealed class RasaIntentHandler(val intent: RasaIntent) {
    class PhoneCall(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
            with(activity) {
                val intent = Intent(Intent.ACTION_DIAL)
                startActivity(intent)
            }
        }

        override fun invoke(activity: Activity, args: List<RasaEntity>) {
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

        override fun invoke(activity: Activity, args: List<RasaEntity>) {
            with(activity) {
                val intent = Intent(ContactsContract.Intents.Insert.ACTION)
                intent.type = ContactsContract.RawContacts.CONTENT_TYPE;
                if(args.size > 1){
                    intent.putExtra(ContactsContract.Intents.Insert.NAME,args.first().value)
                }

                startActivity(intent)
            }
        }
    }

    class RemoveContact(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
        }

        override fun invoke(activity: Activity, args: List<RasaEntity>) {

        }
    }

    class ShowWeather(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {

        }

        override fun invoke(activity: Activity, args: List<RasaEntity>) {

        }
    }

    class OpenApp(intent: RasaIntent) : RasaIntentHandler(intent) {
        override fun invoke(activity: Activity) {
            this(activity, "Google")
        }

        override fun invoke(activity: Activity, args: List<RasaEntity>) {
            val apps = mapOf(
                "یوٹیوب" to "YouTube",
                "اوبر" to "uber",
                "کروم" to "Chrome",
                "پلےاسٹور" to "Play Store",
                "پلے اسٹور" to "Play Store",
                "موسیقی" to "Music",
                "میوسک" to "Music",
                "میوزک" to "Music",
                "میوسق" to "Music",
                "میوزق" to "Music",
                "انسٹاگرام" to "Instagram",
                "فیسبک" to "Facebook",
                "فیس بک" to "Facebook",
                "واٹسایپ" to "WhatsApp",
                "فوڈپانڈا" to "FoodPanda",
                "چیتے" to "Cheetay",
                "ایظی پیسہ" to "EasyPaisa",
                "ٹک ٹاک" to "TikTok",
                "اسنیپ چیٹ" to "SnapChat",
                "اسنیپچیٹ" to "SnapChat",
                "گوگل" to "Google",
                "لینکد ان" to "LinkedIn",
                "لینکڈان" to "LinkedIn",
                "میپس" to "Maps",
            )
            if (args.isEmpty()) {
                invoke(activity)
            } else {
                if (args.first().value in apps) {
                    apps[args.first().value]?.let { this(activity, it) }
                } else {
                    invoke(activity)
                }
            }

        }

        private operator fun invoke(activity: Activity, name: String) {
            try {
                activity.startActivity(
                    activity.packageManager.getLaunchIntentForPackage(
                        getPackage(
                            activity,
                            name
                        )
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        private fun getPackage(activity: Activity, name: String): String {
            return activity.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .associate {
                    it.loadLabel(activity.packageManager).toString()
                        .lowercase(Locale.ENGLISH) to it.packageName
                }[name.lowercase(Locale.ENGLISH)]
                ?: throw IllegalArgumentException("No package found for $name")
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

        override fun invoke(activity: Activity, args: List<RasaEntity>) {

        }
    }

    abstract operator fun invoke(activity: Activity)
    abstract operator fun invoke(activity: Activity, args: List<RasaEntity>)
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