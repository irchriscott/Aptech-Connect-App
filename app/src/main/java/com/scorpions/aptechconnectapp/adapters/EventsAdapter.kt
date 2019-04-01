package com.scorpions.aptechconnectapp.adapters

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.scorpions.aptechconnectapp.R
import com.scorpions.aptechconnectapp.models.Event
import com.scorpions.aptechconnectapp.utils.Routes
import com.scorpions.aptechconnectapp.utils.Utils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.event_layout.view.*
import org.joda.time.DateTime
import android.provider.CalendarContract.Reminders
import android.support.v4.app.ActivityCompat.requestPermissions
import java.util.*
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent


class EventsAdapter(private val events: List<Event>) :  RecyclerView.Adapter<EventViewHolder>(){

    private val routes: Routes = Routes()
    lateinit var utils: Utils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val eventCell = layoutInflater.inflate(R.layout.event_layout, parent, false)
        utils = Utils(parent.context)
        return EventViewHolder(eventCell)
    }

    override fun getItemCount(): Int {
        return this.events.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = this.events[position]
        holder.view.eventName.text = event.name
        holder.view.eventTime.text = utils.formatDateTime("${event.date} ${event.time}")
        holder.view.eventVenue.text = event.venue
        holder.view.eventDescription.text = event.description

        Picasso.get().load("${this.routes.SLASH_END_POINT}${event.image}").into(holder.view.eventPoster)

        holder.view.addToReminder.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(holder.view.context)
            alertDialogBuilder.setTitle("Add Event To Calendar")
            alertDialogBuilder.setMessage("Do you want to add this event to your calendar ?")
            alertDialogBuilder.setPositiveButton("YES", DialogInterface.OnClickListener{_,_ ->
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    if (holder.view.context.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                        && holder.view.context.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(holder.view.context as Activity, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 10)
                    } else {

                        var addReminder = false
                        val intent = Intent(Intent.ACTION_EDIT)
                        intent.type = "vnd.android.cursor.item/event"
                        intent.putExtra("beginTime", utils.dateTimeToMillis("${event.date} ${event.time}"))
                        intent.putExtra("allDay", false)
                        intent.putExtra("rrule", "FREQ=DAILY")
                        intent.putExtra("endTime", utils.dateTimeToMillis("${event.date} ${event.time}") + 2 * 60 * 60 * 60 * 1000)
                        intent.putExtra("title", event.time)
                        holder.view.context.startActivity(intent)

                        if(addReminder){
                            val calendar = Calendar.getInstance()
                            val EVENT_URI = Uri.parse(utils.getCalendarUriBase(holder.view.context as Activity) + "events")
                            val contentResolver = holder.view.context.contentResolver
                            val timeZone = TimeZone.getDefault()
                            val contentValues = ContentValues()
                            contentValues.put(CalendarContract.Events.CALENDAR_ID, (100..10000).random())
                            contentValues.put(CalendarContract.Events.TITLE, event.name)
                            contentValues.put(CalendarContract.Events.DESCRIPTION, event.description)
                            contentValues.put(CalendarContract.Events.ALL_DAY, 0)
                            contentValues.put(
                                CalendarContract.Events.DTSTART,
                                utils.dateTimeToMillis("${event.date} ${event.time}")
                            );
                            contentValues.put(
                                CalendarContract.Events.DTEND,
                                utils.dateTimeToMillis("${event.date} ${event.time}") + 2 * 60 * 60 * 60 * 1000
                            );
                            contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.id);
                            contentValues.put(CalendarContract.Events.HAS_ALARM, 1);
                            val eventUri = contentResolver.insert(EVENT_URI, contentValues)

                            val REMINDERS_URI = Uri.parse(utils.getCalendarUriBase(holder.view.context as Activity) + "reminders")
                            val values = ContentValues()
                            values.put(
                                CalendarContract.Reminders.EVENT_ID,
                                java.lang.Long.parseLong(eventUri.lastPathSegment)
                            )
                            values.put(CalendarContract.Reminders.METHOD, Reminders.METHOD_ALERT)
                            values.put(CalendarContract.Reminders.MINUTES, 120)
                            contentResolver.insert(REMINDERS_URI, values)
                        }
                    }

                } else {
                    Toast.makeText(holder.view.context, "Not Supported", Toast.LENGTH_LONG).show()
                }
            })
            alertDialogBuilder.setNegativeButton("NO", DialogInterface.OnClickListener{_,_ -> })
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

}

private fun IntRange.random() = Random().nextInt((endInclusive + 1) - start) +  start


class EventViewHolder(val view: View) : RecyclerView.ViewHolder(view){}