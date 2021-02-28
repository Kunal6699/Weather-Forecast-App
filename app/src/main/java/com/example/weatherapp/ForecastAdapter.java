package com.example.weatherapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weatherapp.utilities.SunshineDateUtils;
import com.example.weatherapp.utilities.SunshineWeatherUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

    // constant ids for viewtype of today and tmorrow..

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /*
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */
    final private ForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickHandler {

        void onClick(long date);
    }

    private boolean mUseTodayLayout;

    private Cursor mCursor;

    /**
     * Creates a ForecastAdapter.
     *
     * @param context Used to talk to the UI and app resources
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        //Set mUseTodayLayout to the value specified in resources
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId;

        switch (viewType) {

//          If the view type of the layout is today, use today layout
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }

//           If the view type of the layout is future day, use future day layout
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.forecast_list_item;
                break;
            }

//           Otherwise, throw an IllegalArgumentException since today ya future hi bas ho skta hai
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }


        View view = LayoutInflater
                .from(mContext)
                .inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new ForecastAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);


        /****************
         * Weather Icon *
         ****************/

        int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId;

        int viewType = getItemViewType(position);

        switch (viewType) {
//           If the view type of the layout is today, display a large icon
            case VIEW_TYPE_TODAY:
                weatherImageId = SunshineWeatherUtils
                        .getLargeArtResourceIdForWeatherCondition(weatherId);
                break;

//           If the view type of the layout is today, display a small icon
            case VIEW_TYPE_FUTURE_DAY:
                weatherImageId = SunshineWeatherUtils
                        .getSmallArtResourceIdForWeatherCondition(weatherId);
                break;

//           Otherwise, throw an IllegalArgumentException
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }



        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/

        /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
        /* Get human readable string using our utility method */
        String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        forecastAdapterViewHolder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/
        String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, weatherId);

        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.descriptionView.setText(description);
        forecastAdapterViewHolder.descriptionView.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/

        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);

        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius);
        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.highTempView.setText(highString);
        forecastAdapterViewHolder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/

        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);

        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);

        /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.lowTempView.setText(lowString);
        forecastAdapterViewHolder.lowTempView.setContentDescription(lowA11y);

    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        //       Within getItemViewtype, if mUseTodayLayout is true and position is 0, return the ID for today viewType
        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
//       Otherwise, return the ID for future day viewType
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;

        //      COMPLETED (5) Add an ImageView for the weather icon
        final ImageView iconView;

        ForecastAdapterViewHolder(View view) {
            super(view);

            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
//           Instead of passing the String for the clicked item, pass the date from the cursor
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);
        }
    }
}