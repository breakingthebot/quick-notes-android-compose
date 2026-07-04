/*
 * Renders the Quick Notes home-screen widget with Jetpack Glance.
 * Connects to: QuickNotesWidgetSnapshotLoader, MainActivity, and launcher widget metadata.
 * Created: 2026-07-04
 */
package com.breakingthebot.quicknotes.widget

import android.content.Intent
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.breakingthebot.quicknotes.MainActivity
import com.breakingthebot.quicknotes.R

private val WidgetBackgroundColor = ColorProvider(day = Color(0xFFF4EFE7), night = Color(0xFF1F1A17))
private val WidgetSurfaceColor = ColorProvider(day = Color(0xFFFFFBFF), night = Color(0xFF2B2522))
private val WidgetAccentColor = ColorProvider(day = Color(0xFF7A4B2E), night = Color(0xFFFFB68A))
private val WidgetPrimaryTextColor = ColorProvider(day = Color(0xFF251D1A), night = Color(0xFFF2EDE8))
private val WidgetSecondaryTextColor = ColorProvider(day = Color(0xFF5E5148), night = Color(0xFFD5C3B8))

/**
 * Glance widget that surfaces recent active notes from Room.
 */
class QuickNotesWidget : GlanceAppWidget() {
    /**
     * Provides the widget UI tree for an individual widget instance.
     *
     * @param context Android context used to load widget data.
     * @param id Unique widget instance identifier.
     */
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val snapshot = QuickNotesWidgetSnapshotLoader.load(context)

        provideContent {
            QuickNotesWidgetContent(snapshot = snapshot)
        }
    }
}

/**
 * App-widget receiver registered in the manifest for launcher integration.
 */
class QuickNotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickNotesWidget()
}

@Composable
private fun QuickNotesWidgetContent(snapshot: QuickNotesWidgetSnapshot) {
    val context = LocalContext.current
    val openAppAction = actionStartActivity(
        Intent(context, MainActivity::class.java),
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackgroundColor)
            .clickable(openAppAction)
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = context.getString(R.string.widget_title),
            style = TextStyle(
                color = WidgetAccentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = snapshot.noteCountLabel,
            style = TextStyle(
                color = WidgetSecondaryTextColor,
                fontSize = 12.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(12.dp))

        if (snapshot.notes.isEmpty()) {
            EmptyWidgetState(snapshot.emptyMessage)
        } else {
            snapshot.notes.forEachIndexed { index, note ->
                WidgetNoteRow(note = note)
                if (index != snapshot.notes.lastIndex) {
                    Spacer(modifier = GlanceModifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyWidgetState(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetSurfaceColor)
            .padding(14.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = "Nothing here yet",
            style = TextStyle(
                color = WidgetPrimaryTextColor,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = message,
            style = TextStyle(
                color = WidgetSecondaryTextColor,
                fontSize = 12.sp,
            ),
        )
    }
}

@Composable
private fun WidgetNoteRow(note: QuickNotesWidgetNote) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetSurfaceColor)
            .padding(14.dp),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start,
    ) {
        Text(
            text = note.title,
            style = TextStyle(
                color = WidgetPrimaryTextColor,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
            maxLines = 1,
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = note.preview,
            style = TextStyle(
                color = WidgetSecondaryTextColor,
                fontSize = 12.sp,
            ),
            maxLines = 2,
        )
    }
}
