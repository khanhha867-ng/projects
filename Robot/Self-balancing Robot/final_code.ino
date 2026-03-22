#include <Arduino.h>
#include <ESP32Encoder.h>

// ==== Pin Definitions ====
#define JOY_VRX_PIN 34 // Joystick X-axis input (analog)
#define JOY_VRY_PIN 39 // Joystick Y-axis input (analog) - not used in this version
#define JOY_SW_PIN  35  // Joystick button (active LOW)
#define LED_PIN     13 // Debugging LED

// Motor1
#define M1_BIN_1    26 // Motor driver1 input 1 (PWM1)
#define M1_BIN_2    25 // Motor driver1 input 2 (PWM2)

// Motor2
#define M2_BIN_1    14 // Motor driver2 input 1 (PWM1)
#define M2_BIN_2    32 // Motor driver2 input 2 (PWM2)

// Motor1 encoder
#define ENC1_A      27 // Encoder1 channel A
#define ENC1_B      33 // Encoder1 channel B

// Motor2 encoder
#define ENC2_A      15 // Encoder2 channel A
#define ENC2_B      37 // Encoder2 channel B

// ==== PWM Configuration ====
const int freq        = 5000;
const int resolution  = 8;
const int MAX_PWM     = 255;

// ==== Joystick Threshold Settings ====
int joyCenterX = 2048;
int joyCenterY = 2048;
const int JOY_STRONG_NEG  = 1900; // Thresholds for forward/left
const int JOY_STRONG_POS = 600; // Thresholds for reverse/right

// ==== Finite State Machine ====
enum DriveState { 
  STATE_STOP = 0, 
  STATE_FWD  = 1, 
  STATE_REV  = 2,
  STATE_TURN_LEFT  = 3,
  STATE_TURN_RIGHT = 4
};
DriveState currentState = STATE_STOP; // Set initial state to STOP (=0) from DriveState enum

// ==== Timer Setup ====
hw_timer_t* timer0 = NULL;
volatile bool tick = false;
volatile bool deltaT = false;  // check timer interrupt
portMUX_TYPE timerMux0 = portMUX_INITIALIZER_UNLOCKED;
portMUX_TYPE encMux = portMUX_INITIALIZER_UNLOCKED;

// ==== Encoder Setup ====
ESP32Encoder encoder1; // Encoder for Motor1
ESP32Encoder encoder2; // Encoder for Motor2

volatile int encCount1 = 0; // encoder count for motor1
volatile int encCount2 = 0; // encoder count for motor2

// Motor speed variables initialized to 0
// These values are updated every 10 ms by the timer interrupt
int omegaSpeed1 = 0; // Motor1 speed (counts per 10 ms)
int omegaSpeed2 = 0; // Motor2 speed (counts per 10 ms)
int omegaDes_L = 0; // left motor target
int omegaDes_R = 0; // right motor target

/// ==== PI Controller ====
int Kp = 30;
int Ki = 5;
float I1 = 0;
float I2 = 0;

// ==== Smooth Acceleration ====
float D1_s = 0;
float D2_s = 0;
const float ACCEL_LIMIT = 10;

// ===== Timer ISR =====
void IRAM_ATTR onTime0() {
  portENTER_CRITICAL_ISR(&timerMux0);
  tick = true;
  portEXIT_CRITICAL_ISR(&timerMux0);

  // Read encoder counts for both motors and reset
  portENTER_CRITICAL_ISR(&encMux);
  encCount1 = encoder1.getCount();
  encoder1.clearCount();
  encCount2 = encoder2.getCount();
  encoder2.clearCount();

  deltaT = true; // Signal that new encoder data is available
  portEXIT_CRITICAL_ISR(&encMux);
}

// ===== Smooth acceleration function =====
float smooth(float target, float current) {
  float diff = target - current;
  if (diff > ACCEL_LIMIT) diff = ACCEL_LIMIT;
  if (diff < -ACCEL_LIMIT) diff = -ACCEL_LIMIT;
  return current + diff;
}

// ===== Apply Motor PWM and Direction Based on Current State =====
void applyState(DriveState s, int pwm1, int pwm2) {

  int duty1 = constrain(abs(pwm1), 0, MAX_PWM); // Motor1 PWM strength
  int duty2 = constrain(abs(pwm2), 0, MAX_PWM); // Motor2 PWM strength

  switch (s) {
    case STATE_FWD: // both motors spin forward
      // Motor1: FWD
      ledcWrite(M1_BIN_1, 0);
      ledcWrite(M1_BIN_2, duty1);

      // Motor2: FWD
      ledcWrite(M2_BIN_1, 0);
      ledcWrite(M2_BIN_2, duty2);

      digitalWrite(LED_PIN, HIGH); // LED indicator
      break;

    case STATE_REV: // both motors spin reverse
      // Motor1: REV
      ledcWrite(M1_BIN_1, duty1);
      ledcWrite(M1_BIN_2, 0);

      // Motor2: REV
      ledcWrite(M2_BIN_1, duty2);
      ledcWrite(M2_BIN_2, 0);

      digitalWrite(LED_PIN, HIGH); // LED indicator
      break;
    
    case STATE_TURN_LEFT:
      ledcWrite(M1_BIN_1, 0);
      ledcWrite(M1_BIN_2, duty1);

      ledcWrite(M2_BIN_1, 0);
      ledcWrite(M2_BIN_2, 0);

      digitalWrite(LED_PIN, HIGH);
      break;

    case STATE_TURN_RIGHT:
      ledcWrite(M1_BIN_1, 0);
      ledcWrite(M1_BIN_2, 0);

      ledcWrite(M2_BIN_1, 0);
      ledcWrite(M2_BIN_2, duty2);

      digitalWrite(LED_PIN, HIGH);
      break;

    case STATE_STOP: // both motors off
    default:
      // Motor1 stop
      ledcWrite(M1_BIN_1, 0);
      ledcWrite(M1_BIN_2, 0);
      // Motor2 stop
      ledcWrite(M2_BIN_1, 0);
      ledcWrite(M2_BIN_2, 0);

      digitalWrite(LED_PIN, LOW); // LED indicator
      break;
  }
}


// ===== SETUP =====
void setup() {
  Serial.begin(115200);

  // Configure PWM outputs for both motors
  ledcAttach(M1_BIN_1, freq, resolution);
  ledcAttach(M1_BIN_2, freq, resolution);
  ledcAttach(M2_BIN_1, freq, resolution);
  ledcAttach(M2_BIN_2, freq, resolution);

  // LED output
  pinMode(LED_PIN, OUTPUT);

  // joystick calibration
  pinMode(JOY_VRX_PIN, INPUT);
  pinMode(JOY_VRY_PIN, INPUT);
  pinMode(JOY_SW_PIN, INPUT_PULLUP);
  delay(200);
  long sumX=0, sumY=0;
  for (int i=0;i<100;i++){
    sumX += analogRead(JOY_VRX_PIN);
    sumY += analogRead(JOY_VRY_PIN);
    delay(2);
  }
  joyCenterX = sumX/100;
  joyCenterY = sumY/100;
  Serial.printf("joyCenterX=%d joyCenterY=%d\n", joyCenterX, joyCenterY);

  // Encoder
  ESP32Encoder::useInternalWeakPullResistors = puType::up;
  encoder1.attachHalfQuad(ENC1_A, ENC1_B);
  encoder1.setCount(0);
  encoder2.attachHalfQuad(ENC2_A, ENC2_B);
  encoder2.setCount(0);

  // Configure 10ms timer interrupt
  timer0 = timerBegin(1000000);
  timerAttachInterrupt(timer0, &onTime0);
  timerAlarm(timer0, 10000, true, 0); // 10 ms interval
  timerStart(timer0);
}

// ===== Plotter =====
// Columns: omegaSpeed1, omegaSpeed2, omegaDes
void plotControlData() {
  Serial.print(omegaSpeed1);  // Motor1 speed
  Serial.print(" ");
  Serial.print(omegaSpeed2);  // Motor2 speed
  Serial.print(" ");
  Serial.println(omegaDes_L);
  Serial.print(" ");
  Serial.println(omegaDes_R);
}


// ===== Main LOOP =====
void loop() {

  // Check 10 ms tick by timer interrupt
  bool localTick = false;

  portENTER_CRITICAL(&timerMux0);
  if (tick) { 
    localTick = true; 
    tick = false; // reset tick flag
    }
  portEXIT_CRITICAL(&timerMux0);

  // If 10 ms has not passed, exit loop
  if (!localTick) return;

  //------------------------------------------------------
  // 1) Joystick: Determine drive state
  //------------------------------------------------------
  int x_raw = analogRead(JOY_VRX_PIN);
  int y_raw = analogRead(JOY_VRY_PIN);
  bool btnPressed = (digitalRead(JOY_SW_PIN) == LOW);

  int dx = x_raw - joyCenterX;
  int dy = y_raw - joyCenterY;

  // ==== Determine state ====
  if (btnPressed || dy < -JOY_STRONG_NEG) currentState = STATE_FWD;
  else if (dy > JOY_STRONG_POS) currentState = STATE_REV;
  else if (dx < -JOY_STRONG_NEG) currentState = STATE_TURN_LEFT;
  else if (dx > JOY_STRONG_POS) currentState = STATE_TURN_RIGHT;
  else currentState = STATE_STOP;

  // ==== Set target speeds based on state ====
  switch(currentState) {
    case STATE_FWD:      omegaDes_L=14; omegaDes_R=14; break;
    case STATE_REV:      omegaDes_L=-14; omegaDes_R=-14; break;
    case STATE_TURN_LEFT: omegaDes_L=7; omegaDes_R=14; break;
    case STATE_TURN_RIGHT: omegaDes_L=14; omegaDes_R=7; break;
    case STATE_STOP: default: omegaDes_L=0; omegaDes_R=0; break;
  }

  //------------------------------------------------------
  // 2) Encoder: Update measured speeds for both motors
  //------------------------------------------------------
  bool localDT = false;

  // Check if new encoder data is available (set by timer ISR)
  portENTER_CRITICAL(&encMux);
  if (deltaT) { 
    deltaT=false; 
    localDT=true; 
    }
  portEXIT_CRITICAL(&encMux);

  // If new data is ready, update motor speeds
  if (localDT) {
    omegaSpeed1 = encCount1;  // Motor1 speed
    omegaSpeed2 = -encCount2; // Motor2 speed (invert sign to match Motor1 convention)
  }

  // ==== PI Control ====
  int e1 = omegaDes_L - omegaSpeed1;
  int e2 = omegaDes_R - omegaSpeed2;

  I1 += e1; I2 += e2;
  I1 = constrain(I1,-300,300); I2=constrain(I2,-300,300);

  int D1 = Kp*e1 + Ki*I1;
  int D2 = Kp*e2 + Ki*I2;

  D1_s = smooth(D1,D1_s);
  D2_s = smooth(D2,D2_s);

  // ==== Apply motors ====
  applyState(currentState, D1_s, D2_s);

  // ==== Debug ====
  Serial.printf("State=%d L=%.0f R=%.0f spd1=%d spd2=%d\n",
                currentState, D1_s, D2_s, omegaSpeed1, omegaSpeed2);
}