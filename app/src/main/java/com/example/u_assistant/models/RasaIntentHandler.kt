package com.example.u_assistant.models

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
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
            with(activity){
                if (args.isEmpty()){
                    invoke(activity)
                }
                else {
                    val num = getContactList(activity)
                    Log.d("NUMBER",num)
                    val intentDial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                    startActivity(intentDial)
                }
            }
        }

        @SuppressLint("Range")
        private fun getContactList(activity: Activity):String {
            var numberContact = ""
            val cr: ContentResolver = activity.contentResolver
            val cur: Cursor? = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            )

            if ((if (cur != null) cur.count else 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    val id: String = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val name: String = cur.getString(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME
                        )
                    )
                    if (cur.getInt(
                            cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER
                            )
                        ) > 0
                    ) {
                        val pCur: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        if (pCur != null) {
                            while (pCur.moveToNext()) {
                                val phoneNo: String = pCur.getString(
                                    pCur.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER
                                    )
                                )
                                if (phoneNo.isNotEmpty()){
                                numberContact = phoneNo}
                            }
                        }

                    }
                }
            }
            cur?.close()
            return numberContact
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
                var name = ""
                if (args.size == 1) {
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, args.first().value)
                } else {
                    for (value in args) {
                        name = name + " " + value.value
                    }
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, name)

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
            try {
                activity.startActivity(
                    activity.packageManager.getLaunchIntentForPackage(
                        getPackage(
                            activity,
                            "Weather"
                        )
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        override fun invoke(activity: Activity, args: List<RasaEntity>) {
            invoke(activity)
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
                }
                if (args.isNotEmpty()) {
                    invoke(activity, "Google", args.first().value)
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


        private operator fun invoke(activity: Activity, name: String, searchString: String) {

            try {
                activity.startActivity(
                    activity.packageManager.getLaunchIntentForPackage(
                        getPackage(
                            activity,
                            name
                        )
                    )!!.setData(Uri.parse("https://www.google.com/#q=$searchString"))
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

            if (args.isEmpty()) {
                invoke(activity)
            } else {
                val numbersInUrdu = mapOf(
                    "ایک" to 1,
                    "اک" to 1,
                    "دو" to 2,
                    "ڈو" to 2,
                    "تین" to 3,
                    "ٹین" to 3,
                    "چار" to 4,
                    "چاڑ" to 4,
                    "پانچ" to 5,
                    "چ" to 6,
                    "چھ" to 6,
                    "چہ" to 6,
                    "سات" to 7,
                    "ساٹ" to 7,
                    "اٹٹھ" to 8,
                    "آٹھ" to 8,
                    "نو" to 9,
                    "نؤ" to 9,
                    "دس" to 10,
                    "ڈس" to 10,
                    "دصص" to 10,
                    "دص" to 10,
                    "گیارہ" to 11,
                    "گیارا" to 11,
                    "گیاڑا" to 11,
                    "گیاڑہ" to 11,
                    "بارا" to 12,
                    "بارہ" to 12,
                    "باڑہ" to 12,
                    "باڑا" to 12,
                )
                val alarmTime = listOf("شام","رات","دوپہر","صبح")

                if (args.first().value in numbersInUrdu && args.first().entity == "alarm_time_4" ) {
                    var hourSet = 0

                    hourSet = numbersInUrdu[args.first().value]!!

                    with(activity) {
                        val i = Intent(AlarmClock.ACTION_SET_ALARM)
                        i.putExtra(AlarmClock.EXTRA_HOUR, hourSet)
                        i.putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        i.putExtra(AlarmClock.EXTRA_MESSAGE,"الارم یو اسسٹنٹ کے ذریعہ سیٹ کیا گیا ہے۔")
                        startActivity(i)
                    }
                } else {
                    invoke(activity)
                }
            }
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