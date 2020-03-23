#include <ArduinoJson.h>
#include <HX711.h>
#include <SoftwareSerial.h>
#include <Filter.h>

// Sets the program to run in test mode if true.
bool test_mode = false;
// 35000 gram maximum (35kg) weight.
float max_weight = 35000; 
// 100 gram minimum (0.01kg) weight.
float min_weight = 100; 

// Input & Output pins.
const byte data_pin = 5; // HX711 sensor pin
const byte clock_pin = 4; // HX711 sensor pin
const byte interrupt_pin = 3; // Opto sensor pin
const byte signal_max_weight_pin = 11; // Signal pin (HIGH above max_weight, otherwise LOW)
const byte signal_min_weight_pin = 12; // Signal pin (HIGH below min_weight, otherwise LOW)

// HX711 scale, load cell & filtering vars.
float calibration_factor = 65; // this calibration factor is adjusted according to the load cell
float units;
float raw_units;
/* Moving average for load cell sensor data filtering*/
Moving_average ma(5, 0);
/* HX7111 load cell init with pins */
HX711 scale(data_pin, clock_pin);

// Wheel counter.
volatile int pulsecount;

// State for signals on pins
bool above_max_weight = false;
bool below_min_weight = false;

// Bluetooth Serial init.
SoftwareSerial my_serial(6, 7); // RX, TX
// Timeout for serial init.
const int timeout = 800;

// Counts every wheel turn.
void CountWheelTurn() {
  pulsecount++;
}

// Reads the scale value into the raw_units.
void ReadRubberPull() {
  raw_units = scale.get_units();
  if (raw_units < 0)
  {
    raw_units = 0.00;
  }
}

// Filters the raw units and writes the filtered value into units.
void FilterPullForce() {
  units = ma.filter(raw_units);
}

// Sends a signal to pin 11 if the measured weight is above max_weight.
void SignalOnMaxWeightPin() {
  if (test_mode) {
    Serial.print("Units: ");
    Serial.print(units);
    Serial.println();
  }
  if (units >= max_weight && !above_max_weight) {
    noInterrupts();
    digitalWrite(signal_max_weight_pin, HIGH);
    above_max_weight = true;
    interrupts();
    if (test_mode) {
      Serial.print("\nHIGH\n");
    }
  }

  if (units < max_weight && above_max_weight) {
    noInterrupts();
    digitalWrite(signal_max_weight_pin, LOW);
    above_max_weight = false;
    interrupts();
    if (test_mode) {
      Serial.print("\nLOW\n");
    }
  }
}

// Sends a signal to pin 12 if the measured weight is below min_weight.
void SignalOnMinWeightPin() {
  if (test_mode) {
    Serial.print("Units: ");
    Serial.print(units);
    Serial.println();
  }
  if (units < min_weight && !below_min_weight) {
//    noInterrupts();
    digitalWrite(signal_min_weight_pin, HIGH);
    below_min_weight = true;
//    interrupts();
    if (test_mode) {
      Serial.print("\nHIGH\n");
    }
  }

  if (units >= min_weight && below_min_weight) {
//    noInterrupts();
    digitalWrite(signal_min_weight_pin, LOW);
    below_min_weight = false;
//    interrupts();
    if (test_mode) {
      Serial.print("\nLOW\n");
    }
  }
}

// Sends the measurement on bluetooth.
void SendJSONBluetooth() {
  DynamicJsonBuffer jsonBuffer;
  JsonObject& measurement = jsonBuffer.createObject();
  measurement["unit"] = "gram";
  measurement["force"] = units;
  measurement["count"] = pulsecount;    
  
  JsonObject& root = jsonBuffer.createObject();
  root["version"] = 1;
  root["unit"] = "cm";
  root["leap"] = 4;
  
  JsonArray& data = root.createNestedArray("measurements");
  data.add(measurement);

  jsonBuffer.clear();

  my_serial.print("start_");
  root.printTo(my_serial);
  my_serial.print("_end\n");

  if (test_mode) {
    root.printTo(Serial);
    Serial.print("\n\n");
  }
}

void HandleWheelTurn() {
  CountWheelTurn();
  SendJSONBluetooth();
}

void setup() {
  // The serial setup for PC & Arduino communication.
  Serial.begin(9600);
  Serial.setTimeout(timeout);
  // The bluetooth serial communication at 9600 baud rate. This has to be changed to make it faster.
  my_serial.begin(115200);
  my_serial.setTimeout(timeout);

  // Initialize the scale
  scale.set_scale();
  scale.tare();  //Reset the scale to 0
  scale.set_scale(calibration_factor); //Adjust to this calibration factor

  // The current calibration factor.
  Serial.print("Calibration_factor: ");
  Serial.print(calibration_factor);
  Serial.println();

  // Pin configuration & defaults.
  pinMode(signal_max_weight_pin, OUTPUT);
  digitalWrite(signal_max_weight_pin, LOW);
  pinMode(signal_min_weight_pin, OUTPUT);
  digitalWrite(signal_min_weight_pin, LOW);
  pinMode(interrupt_pin, INPUT_PULLUP);
  // Setup interrupt for wheel turns.
  attachInterrupt(digitalPinToInterrupt(interrupt_pin), HandleWheelTurn, FALLING);
  pulsecount = 0;
}

void loop() {
  // Continuously read, filter the measurements and signal to pin.
  delay(50);
  ReadRubberPull();
  FilterPullForce();
  SignalOnMaxWeightPin();
  SignalOnMinWeightPin();
  if (test_mode) {
    HandleWheelTurn();
  }
}
