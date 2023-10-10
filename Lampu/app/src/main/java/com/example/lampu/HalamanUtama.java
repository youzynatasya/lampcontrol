package com.example.lampu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ZoomControls;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.google.android.gms.internal.maps.zzaa;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class HalamanUtama extends AppCompatActivity  {
    private FirebaseUser firebaseUser;
    private TextView textNama, tanggal, jam, suhuTextView;
    ImageView ic_menu;
    Dialog mDialog;
    private MapView mapView;
    private Button btnLampu;
    private Handler handler = new Handler();
    FirebaseDatabase mDatabase;
    DatabaseReference dref;
    private boolean isHandlerRunning = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);
        textNama = findViewById(R.id.txtNama);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            textNama.setText(firebaseUser.getDisplayName());
        } else {
            textNama.setText("Login Gagal");
        }

        mDialog = new Dialog(this);



        String url = "https://lampu-f8a57-default-rtdb.firebaseio.com/";
        mDatabase = FirebaseDatabase.getInstance();
        dref = mDatabase.getReference();

        ic_menu = findViewById(R.id.menu);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        ic_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                item.setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (id) {
                    case R.id.nav_home:
                        replaceFragment(new HomeFragment());
                        break;
                    case R.id.nav_helpcenter:
                        replaceFragment(new HelpCenterFragment());
                        break;
                    case R.id.nav_settings:
                        replaceFragment(new SettingsFragment());
                        break;
                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        tanggal = findViewById(R.id.tanggal);
        // Membuat handler untuk memperbarui tampilan tanggal setiap detik
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Mendapatkan tanggal saat ini
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();

                // Mengubah tanggal menjadi format yang diinginkan (contoh: Senin, 8 Januari 2023)
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());
                String tanggalString = dateFormat.format(date);

                // Mengatur tanggal ke TextView
                tanggal.setText(tanggalString);

                // Memanggil kembali runnable setelah 1 detik (1000 milidetik)
                handler.postDelayed(this, 1000);
            }
        };
        // Memulai pembaruan tampilan tanggal
        handler.post(runnable);

        jam = findViewById(R.id.jam);
        // Mendapatkan waktu saat ini
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        // Membuat Timer
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Mendapatkan waktu saat ini
                final String currentTime = timeFormat.format(new Date());

                // Memperbarui TextView "jam" di UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        jam.setText(currentTime);
                    }
                });
            }
        }, 0, 1000);

        suhuTextView = findViewById(R.id.suhu);
        // Mendapatkan referensi Firebase Database
        // Menambahkan pendengar (listener) untuk pembaruan data
        mDatabase.getReference("lampOt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Mendapatkan data suhu dari Firebase Database
                Integer suhu = dataSnapshot.getValue(Integer.class);

                // Memperbarui TextView "suhu" di UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        suhuTextView.setText(String.valueOf(suhu));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Penanganan kesalahan pembacaan data dari Firebase Database
                Log.e("HalamanUtama", "Error reading suhu value", databaseError.toException());
            }
        });


        // Inisialisasi konfigurasi OSMdroid (harus dilakukan sebelum mengatur konten tampilan)
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        // Mendapatkan referensi ke MapView dari layout
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);

        // Mendapatkan koordinat geografis (latitude dan longitude) untuk lokasi tombol (Pradita University)
        double latitude = -6.26064;
        double longitude = 106.61812;

        // Atur tampilan awal peta pada Pradita University
        mapView.getController().setCenter(new GeoPoint(latitude, longitude));
        mapView.getController().setZoom(15);


        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);

// Mendapatkan koordinat geografis (latitude dan longitude) untuk marker
        double markerLatitude = -6.26064;
        double markerLongitude = 106.61812;

// Membuat marker
        ArrayList<OverlayItem> overlayItems = new ArrayList<>();
        GeoPoint markerPosition = new GeoPoint(markerLatitude, markerLongitude);
        OverlayItem markerItem = new OverlayItem("Marker Title", "Marker Description", markerPosition);
        overlayItems.add(markerItem);

        ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<>(this, overlayItems, null);
        mapView.getOverlayManager().add(itemizedIconOverlay);

        btnLampu = findViewById(R.id.btnLampu);
        btnLampu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mengambil nilai mode dari Firebase
                DatabaseReference modeRef = mDatabase.getReference("mode");
                modeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int mode = dataSnapshot.getValue(Integer.class);
                            // Memanggil metode yang sesuai berdasarkan nilai mode
                            if (mode == 1) {
                                ShowPopupManual(v);
                            } else if (mode == 2) {
                                ShowPopupJadwal(v);
                            } else if (mode == 3) {
                                ShowPopupOtomatis(v);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Menangani kesalahan jika ada
                    }
                });
            }
        });


    }


    public void ShowPopupManual(View view) {
        Button btnJadwal, btnOtomatis;
        Slider slider;

        mDialog.setContentView(R.layout.activity_lampu_manual);
        btnJadwal = mDialog.findViewById(R.id.btnJadwal);
        btnOtomatis = mDialog.findViewById(R.id.btnOtomatis);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mDatabase.getReference("mode").setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Tindakan yang dilakukan saat berhasil menyimpan nilai di Firebase
                    // Misalnya, menampilkan pesan atau mengubah tampilan
                }else {
                    // Tindakan yang dilakukan jika terjadi kesalahan saat menyimpan nilai di Firebase
                    // Misalnya, menampilkan pesan kesalahan
                }
            }
        });

        slider = mDialog.findViewById(R.id.slider);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                int brightness = (int) value;
                dref.child("lampMan").setValue(brightness).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Tindakan yang dilakukan saat berhasil mengubah nilai di Firebase
                            // Misalnya, menampilkan pesan atau mengubah tampilan
                        }
                    }
                });
            }
        });

        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    public void ShowPopupJadwal(View view) {
        Button btnManual, btnOtomatis;
        EditText timepickerstart, timepickerend;
        DatabaseReference JadRef;

        mDialog.setContentView(R.layout.activity_lampu_jadwal);
        btnManual = mDialog.findViewById(R.id.btnManual);
        btnOtomatis = mDialog.findViewById(R.id.btnOtomatis);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mDatabase.getReference("mode").setValue(2).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Tindakan yang dilakukan saat berhasil menyimpan nilai di Firebase
                    // Misalnya, menampilkan pesan atau mengubah tampilan
                } else {
                    // Tindakan yang dilakukan jika terjadi kesalahan saat menyimpan nilai di Firebase
                    // Misalnya, menampilkan pesan kesalahan
                }
            }
        });

        timepickerstart = mDialog.findViewById(R.id.timepickerstart);
        timepickerend = mDialog.findViewById(R.id.timepickerend);

        timepickerstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(timepickerstart);
            }
        });
        timepickerend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(timepickerend);
            }
        });

        JadRef = mDatabase.getReference("lampJad");
        JadRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String start = dataSnapshot.child("start").getValue(String.class);
                    String end = dataSnapshot.child("end").getValue(String.class);

                    // Set nilai start dan end ke EditText
                    timepickerstart.setText(start);
                    timepickerend.setText(end);

                    // Perbarui status berdasarkan waktu saat ini
                    checkAndUpdateStatus();
                    startStatusUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Menangani kesalahan jika ada
            }
        });

        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    public void showTimePickerDialog(final EditText editTextTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editTextTime.setText(selectedTime);

                EditText timepickerstart = mDialog.findViewById(R.id.timepickerstart);
                EditText timepickerend = mDialog.findViewById(R.id.timepickerend);

                String start = timepickerstart.getText().toString();
                String end = timepickerend.getText().toString();

                DatabaseReference jadwalRef = FirebaseDatabase.getInstance().getReference("lampJad");
                jadwalRef.child("start").setValue(start);
                jadwalRef.child("end").setValue(end).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HalamanUtama.this, "Waktu berhasil dikirim ke Firebase", Toast.LENGTH_SHORT).show();
                            checkAndUpdateStatus();
                        } else {
                            Toast.makeText(HalamanUtama.this, "Gagal mengirim waktu ke Firebase", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void checkAndUpdateStatus() {
        // Dapatkan waktu saat ini
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        EditText timepickerstart = mDialog.findViewById(R.id.timepickerstart);
        EditText timepickerend = mDialog.findViewById(R.id.timepickerend);

        String start = timepickerstart.getText().toString();
        String end = timepickerend.getText().toString();

        if (start.isEmpty() || end.isEmpty()) {
            // Menampilkan pesan atau melakukan tindakan jika input kosong
            return;
        }

        // Melakukan parsing waktu start dan end dari string menjadi integer
        int startHour, startMinute, endHour, endMinute;
        try {
            startHour = Integer.parseInt(start.split(":")[0]);
            startMinute = Integer.parseInt(start.split(":")[1]);
            endHour = Integer.parseInt(end.split(":")[0]);
            endMinute = Integer.parseInt(end.split(":")[1]);
        } catch (NumberFormatException e) {
            // Menampilkan pesan atau melakukan tindakan jika parsing gagal
            return;
        }

        boolean lampOn = false;

        // Periksa apakah waktu saat ini berada di antara start dan end
        if (currentHour > startHour || (currentHour == startHour && currentMinute >= startMinute)) {
            if (currentHour < endHour || (currentHour == endHour && currentMinute < endMinute)) {
                // Waktu saat ini berada di antara start dan end
                // Ubah gambar dan teks pada ImageView dan TextView sesuai dengan kondisi yang diinginkan
                lampOn = true;
            }
        }

        // Ubah status pada Firebase
        DatabaseReference lampJadRef = mDatabase.getReference("lampJad/status");
        lampJadRef.setValue(lampOn ? 1 : 0)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        } else {

                        }
                    }
                });

        // Memperbarui tampilan
        updateUI(lampOn);
    }

    private void updateUI(boolean lampOn) {
        ImageView lampumati = mDialog.findViewById(R.id.lampumati);
        TextView txtOff = mDialog.findViewById(R.id.txtOff);

        if (lampOn) {
            lampumati.setImageResource(R.drawable.iconlampunyala);
            txtOff.setText("ON");
            txtOff.setTextColor(ContextCompat.getColor(mDialog.getContext(), R.color.green));
        } else {
            lampumati.setImageResource(R.drawable.iconlampumati);
            txtOff.setText("OFF");
            txtOff.setTextColor(ContextCompat.getColor(mDialog.getContext(), R.color.red));
        }
    }

    private void startStatusUpdate() {
        // Memastikan hanya satu pembaruan status yang berjalan pada suatu waktu
        if (!isHandlerRunning) {
            // Memulai pembaruan status
            handler.post(updateStatusRunnable);
            isHandlerRunning = true;
        }
    }

    private void stopStatusUpdate() {
        // Menghentikan pembaruan status
        handler.removeCallbacks(updateStatusRunnable);
        isHandlerRunning = false;
    }

    private Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            // Perbarui status berdasarkan waktu saat ini
            checkAndUpdateStatus();

            // Jalankan pembaruan status setiap menit (atau interval yang Anda inginkan)
            handler.postDelayed(updateStatusRunnable, 60000); // 60000 ms = 1 menit
        }
    };



    public void ShowPopupOtomatis (View view) {
        Button btnManual, btnJadwal;
        DatabaseReference ldrRef, suhuRef;

        mDialog.setContentView(R.layout.activity_lampu_otomatis);
        btnManual = mDialog.findViewById(R.id.btnManual);
        btnJadwal = mDialog.findViewById(R.id.btnJadwal);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        mDatabase.getReference("mode").setValue(3).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Tindakan yang dilakukan saat berhasil menyimpan nilai di Firebase
                    // Misalnya, menampilkan pesan atau mengubah tampilan
                } else {
                    // Tindakan yang dilakukan jika terjadi kesalahan saat menyimpan nilai di Firebase
                    // Misalnya, menampilkan pesan kesalahan
                }
            }
        });

        TextView suhuOt = mDialog.findViewById(R.id.suhuOt);

        suhuRef = mDatabase.getReference("lampOt");
        // Menambahkan pendengar (listener) untuk pembaruan data
        suhuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Mendapatkan data suhu dari Firebase Database
                int lampOtom = dataSnapshot.getValue(Integer.class);

                // Memperbarui TextView "suhu" di UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        suhuOt.setText(String.valueOf(lampOtom));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Penanganan kesalahan pembacaan data dari Firebase Database
                Log.e("HalamanUtama", "Error reading suhu value", databaseError.toException());
            }
        });

        ImageView lampumati = mDialog.findViewById(R.id.lampumati);
        TextView txtOff = mDialog.findViewById(R.id.txtOff);

        ldrRef = mDatabase.getReference("ldrVal");
        ldrRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int ldrVal = dataSnapshot.getValue(Integer.class);

                    if (ldrVal < 500) {
                        lampumati.setImageResource(R.drawable.iconlampunyala);
                        txtOff.setText("ON");
                        txtOff.setTextColor(ContextCompat.getColor(mDialog.getContext(), R.color.green));
                    } else {
                        lampumati.setImageResource(R.drawable.iconlampumati);
                        txtOff.setText("OFF");
                        txtOff.setTextColor(ContextCompat.getColor(mDialog.getContext(), R.color.red));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Menangani kesalahan jika ada
            }
        });

        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }



    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }
}