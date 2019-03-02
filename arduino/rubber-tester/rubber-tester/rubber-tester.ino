#include <ArduinoJson.h>
#include <HX711.h>
#include <SoftwareSerial.h>

const byte dataPin = 5; // HX711 sensor pin
const byte clockPin = 4; // HX711 sensor pin
const byte interruptPin = 3; // Opto sensor pin

// Load Cell
float calibration_factor = 65; // this calibration factor is adjusted according to my load cell
float units;

// Wheel
volatile int pulsecount;
byte unit = 2;
double windspeed;
int sensorVal;

/* HX7111 load cell init with pins */
HX711 scale(dataPin, clockPin);

/* Bluetooth Serial init */
SoftwareSerial mySerial(6, 7); // RX, TX

/* Timeout for serial init */
const int timeout = 800;

double windRange[2] = { 0, 15};

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(timeout);
  mySerial.begin(9600);
  mySerial.setTimeout(timeout);

  scale.set_scale();
  scale.tare();  //Reset the scale to 0
  scale.set_scale(calibration_factor); //Adjust to this calibration factor

  Serial.print("Calibration_factor: ");
  Serial.print(calibration_factor);
  Serial.println();

  pinMode(interruptPin, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(interruptPin), handleWheelTurn, FALLING);
  pulsecount = 0;
}

void loop() {
  // nothing to do here.
}

void handleWheelTurn() {
  countWheelTurn();
  readRubberPull();
  sendJSONBluetooth();
  clearReadRubberPull();
}

void countWheelTurn() {
  pulsecount++;
}

void readRubberPull() {
  units = scale.get_units(), 10;
  if (units < 0)
  {
    units = 0.00;
  }
}

void clearReadRubberPull() {
  units = 0;
}

void sendJSONBluetooth() {
  int nodeCount = 1;
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

  mySerial.print("start_");
  root.printTo(mySerial);
  mySerial.print("_end\n");
}
