package ru.geekbrains.cityinfo;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

// Фрагмент для вывода герба
public class CoatOfArmsFragment extends Fragment {

    public static final String PARCEL = "parcel";
    private Handler handler;
    private TextView city;
    private TextView temp;
    private TextView sky;
    protected TextView gradus;
    private TextView detailsText;
    private TextView data;

    // фабричный метод, создает фрагмент и передадет параметр
    public static CoatOfArmsFragment create(Parcel parcel) {
        CoatOfArmsFragment f = new CoatOfArmsFragment();    // создание

        // передача параметра
        Bundle args = new Bundle();
        args.putSerializable(PARCEL, parcel);
        f.setArguments(args);
        return f;
    }

    // получить индекс из списка (фактически из параметра)
    public Parcel getParcel() {
        return (Parcel) getArguments().getSerializable(PARCEL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        handler = new Handler();


        View layout = inflater.inflate(R.layout.fragment_coatofarm, container, false);

        Parcel parcel = getParcel();
        temp = layout.findViewById(R.id.temperature);
        temp.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeueCyr-UltraLight.otf"));
        sky = layout.findViewById(R.id.sky);
        sky.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf"));
        gradus = layout.findViewById(R.id.gradus);
        gradus.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeueCyr-UltraLight.otf"));
        gradus.setText("\u00b0C");
        detailsText = layout.findViewById(R.id.details);
        city = layout.findViewById(R.id.city);
        data = layout.findViewById(R.id.data);

        updateWeatherData(parcel.getCityName());



//        // определить какой герб надо показать, и показать его
//        //ImageView coatOfArms  = layout.findViewById(R.id.imageView);
//        TextView cityNameView = layout.findViewById(R.id.city);
//
//        // получить из ресурсов массив указателей на изображения гербов
//        TypedArray imgs = getResources().obtainTypedArray(R.array.coatofarms_imgs);
//
//        // выбрать по индексу подходящий
//        //coatOfArms.setImageResource(imgs.getResourceId(parcel.getImageIndex(), -1));
//        imgs.recycle();
//        cityNameView.setText(parcel.getCityName());

        return layout; // Вместо макета используем сразу картинку
    }

    //Обновление/загрузка погодных данных
    private void updateWeatherData(final String city) {
        new Thread() {
            public void run() {
                final JSONObject json = WeatherData.getJSONData(CoatOfArmsFragment.this, city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
//                            Toast.makeText(CoatOfArmsFragment.this, CoatOfArmsFragment.this.getString(R.string.place_not_found),
//                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    //Обработка загруженных данных
    private void renderWeather(JSONObject json) {
        try {
            city.setText(json.getString("name").toUpperCase(Locale.US) + ", "
                    + json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsText.setText(details.getString("description").toUpperCase(Locale.US) + "\n" + getResources().getString(R.string.humidity)
                    + ": " + main.getString("humidity") + "%" + "\n" + getResources().getString(R.string.pressure)
                    + ": " + main.getString("pressure") + " hPa");
            detailsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            detailsText.setLineSpacing(0,1.4f);

            temp.setText(String.format("%.1f", main.getDouble("temp")));

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            data.setText(getResources().getString(R.string.last_update) + " " + updatedOn);

            setWeatherIcon(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        } catch (Exception e) {
            Log.e("Weather", "One or more fields not found in the JSON data");
        }
    }

    //Подстановка нужной иконки
    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = CoatOfArmsFragment.this.getString(R.string.weather_sunny);
            } else {
                icon = CoatOfArmsFragment.this.getString(R.string.weather_clear_night);
            }
        } else {
            Log.d("SimpleWeather", "id " + id);
            switch (id) {
                case 2:
                    icon = CoatOfArmsFragment.this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = CoatOfArmsFragment.this.getString(R.string.weather_drizzle);
                    break;
                case 5:
                    icon = CoatOfArmsFragment.this.getString(R.string.weather_rainy);
                    break;
                case 6:
                    icon = CoatOfArmsFragment.this.getString(R.string.weather_snowy);
                    break;
                case 7:
                    icon = CoatOfArmsFragment.this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = CoatOfArmsFragment.this.getString(R.string.weather_cloudy);
                    break;
            }
        }
        sky.setText(icon);
    }
}
