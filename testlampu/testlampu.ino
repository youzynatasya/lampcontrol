#include <WiFi.h>
#include <FirebaseESP32.h>
#include <TimeLib.h>

#define WIFI_SSID "iPhone"
#define WIFI_PASSWORD "12345678"

#define FIREBASE_HOST "lampu-f8a57-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "cogNU13OOenthjU5piMuZ6dnJgYAL64QH5wG5zbF"

#define LAMP_PIN 13
#define LDR_PIN 34

FirebaseData firebaseData;
boolean lampuStatus = false;
boolean ledcAttached = false;

void setup() {
  Serial.begin(9600);
  pinMode(LAMP_PIN, OUTPUT);
  pinMode(LDR_PIN, INPUT);

  // Inisialisasi WiFi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to WiFi");
  Serial.println("Hello, world!");

  // Inisialisasi Firebase
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  

}


void loop() {
  if (Firebase.getInt(firebaseData, "/mode") && firebaseData.dataType() == "int") {
    int mode = firebaseData.intData();
    Serial.print("Mode: ");
    Serial.println(mode);

    // Mengontrol lampu berdasarkan mode yang diterima dari Firebase
    if (mode == 1) {
      // Mematikan lampu atau melepas LEDC pin jika masih terhubung saat berpindah mode
      if (Firebase.getInt(firebaseData, "/lampMan") && firebaseData.dataType() == "int") {
        int brightness = firebaseData.intData();
        if (!ledcAttached) {
          ledcSetup(0, 5000, 8); // Konfigurasi saluran LEDC, 0 no saluran yg akan dikonfigurasi,5000 nilai frekuensi pwm yg diinginkan, 8 tingkat kecerahan dr lmpu
          ledcAttachPin(LAMP_PIN, 0); // Menghubungkan pin dengan saluran LEDC
          ledcAttached = true;
        }
        ledcWrite(0, brightness); // Menulis nilai ke saluran LEDC
      }
    } else if (mode == 2) {
      // Mematikan lampu atau melepas LEDC pin jika masih terhubung saat berpindah mode 
      if (ledcAttached) {
        ledcDetachPin(LAMP_PIN);
        ledcAttached = false;
      }
      // Mengeksekusi lampJad hanya sekali saat status berubah
      if (Firebase.getInt(firebaseData, "/lampJad/status") && firebaseData.dataType() == "int") {
        int status = firebaseData.intData();
        // Status lampu menyala
        if (status == 1 ) {
          if(!lampuStatus){
          nyalakanLampu();
          Serial.print("Lampu Menyala ");
          }
        } else {
          if(lampuStatus){
          // Status lampu mati
          matikanLampu();
          Serial.print("Lampu Mati ");
          }
        }
      }
    } else if (mode == 3) {
      // Mematikan lampu atau melepas LEDC pin jika masih terhubung saat berpindah mode
      
      if (ledcAttached) {
        ledcDetachPin(LAMP_PIN);
        ledcAttached = false;
      }

      // Mengeksekusi mode 3
      int ldrValue = analogRead(LDR_PIN);
      Serial.println(ldrValue);
      if (ldrValue < 500) {
        if (!lampuStatus) {
          nyalakanLampu();
          Serial.print("Lampu Menyala ");
        }
      } else {
        if (lampuStatus) {
          matikanLampu();
          Serial.print("Lampu Mati ");
        }
      }
    }
  }


  // Membaca nilai dari sensor LDR
  int ldrValue = analogRead(LDR_PIN);

  // Menyimpan nilai LDR ke Firebase
  Firebase.setInt(firebaseData, "/ldrVal", ldrValue);

  //delay(1000);
}

void nyalakanLampu() {
  digitalWrite(LAMP_PIN, HIGH);
  lampuStatus = true;
  Serial.println("Lampu dinyalakan");
}

void matikanLampu() {
  digitalWrite(LAMP_PIN, LOW);
  lampuStatus = false;
  Serial.println("Lampu dimatikan");
}

String getCurrentTime() {
  time_t now = time(nullptr);
  struct tm* timeinfo;
  timeinfo = localtime(&now);
  char timeBuffer[9];
  sprintf(timeBuffer, "%02d:%02d", timeinfo->tm_hour, timeinfo->tm_min);
  return String(timeBuffer);
}

boolean isTimeInRange(String currentTime, String start, String end) {
  return currentTime >= start && currentTime <= end;
}
