package com.example.weatherapp.utilities;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.DetailActivity;
import com.example.weatherapp.R;
import com.example.weatherapp.data.SunshinePreferences;
import com.example.weatherapp.data.WeatherContract;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationUtils {

    public static final String[] WEATHER_NOTIFICATION_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;

    // Constant value for notification
    private static final int WEATHER_NOTIFICATION_ID = 3004;
    public static final String WEATHER_REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    public static void notifyUserOfNewWeather(Context context)
    {
        /* Build the URI for today's weather in order to show up to date data in notification */
        Uri todaysWeatherUri = WeatherContract.WeatherEntry
                .buildWeatherUriWithDate(SunshineDateUtils.normalizeDate(System.currentTimeMillis()));

        Cursor todayWeatherCursor = context.getContentResolver().query(
                todaysWeatherUri,
                WEATHER_NOTIFICATION_PROJECTION,
                null,
                null,
                null);
        if (todayWeatherCursor.moveToFirst()) {

            /* Weather ID as returned by API, used to identify the icon to be used */
            int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
            double high = todayWeatherCursor.getDouble(INDEX_MAX_TEMP);
            double low = todayWeatherCursor.getDouble(INDEX_MIN_TEMP);

            Resources resources = context.getResources();
            int largeArtResourceId = SunshineWeatherUtils
                    .getLargeArtResourceIdForWeatherCondition(weatherId);

            Bitmap largeIcon = BitmapFactory.decodeResource(
                    resources,
                    largeArtResourceId);

            String notificationTitle = context.getString(R.string.app_name);

            String notificationText = getNotificationText(context, weatherId, high, low);

            /* getSmallArtResourceIdForWeatherCondition returns the proper art to show given an ID */
            int smallArtResourceId = SunshineWeatherUtils
                    .getSmallArtResourceIdForWeatherCondition(weatherId);

            /*
             * NotificationCompat Builder is a very convenient way to build backward-compatible
             * notifications. In order to use it, we provide a context and specify a color for the
             * notification, a couple of different icons, the title for the notification, and
             * finally the text of the notification, which in our case in a summary of today's
             * forecast.
             */

             NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    WEATHER_REMINDER_NOTIFICATION_CHANNEL_ID,
                    "Primary",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(mChannel);
        }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    context,WEATHER_REMINDER_NOTIFICATION_CHANNEL_ID)
                    .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                    .setSmallIcon(smallArtResourceId)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setAutoCancel(true);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            {
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
            }



            Intent detailIntentForToday = new Intent(context, DetailActivity.class);
            detailIntentForToday.setData(todaysWeatherUri);

//        Use TaskStackBuilder to create the proper PendingINtent
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
            taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);
            PendingIntent resultPendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

//
            notificationBuilder.setContentIntent(resultPendingIntent);

//

//          COMPLETED (7) Notify the user with the ID WEATHER_NOTIFICATION_ID
            /* WEATHER_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

//          COMPLETED (8) Save the time at which the notification occurred using SunshinePreferences
            /*
             * Since we just showed a notification, save the current time. That way, we can check
             * next time the weather is refreshed if we should show another notification.
             */
            SunshinePreferences.saveLastNotificationTime(context, System.currentTimeMillis());
        }

        /* Always close your cursor when you're done with it to avoid wasting resources. */
        todayWeatherCursor.close();
    }

    /**
     * Constructs and returns the summary of a particular day's forecast using various utility
     * methods and resources for formatting. This method is only used to create the text for the
     * notification that appears when the weather is refreshed.
     * <p>
     * The String returned from this method will look something like this:
     * <p>
     * Forecast: Sunny - High: 14°C Low 7°C
     *
     * @param context   Used to access utility methods and resources
     * @param weatherId ID as determined by Open Weather Map
     * @param high      High temperature (either celsius or fahrenheit depending on preferences)
     * @param low       Low temperature (either celsius or fahrenheit depending on preferences)
     * @return Summary of a particular day's forecast
     */
    private static String getNotificationText(Context context, int weatherId, double high, double low) {

        /*
         * Short description of the weather, as provided by the API.
         * e.g "clear" vs "sky is clear".
         */
        String shortDescription = SunshineWeatherUtils
                .getStringForWeatherCondition(context, weatherId);

        String notificationFormat = context.getString(R.string.format_notification);

        /* Using String's format method, we create the forecast summary */
        String notificationText = String.format(notificationFormat,
                shortDescription,
                SunshineWeatherUtils.formatTemperature(context, high),
                SunshineWeatherUtils.formatTemperature(context, low));

        return notificationText;
    }
}