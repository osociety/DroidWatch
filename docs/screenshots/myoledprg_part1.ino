
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <SoftwareSerial.h>

 // serial connection for the bluetooth module
SoftwareSerial mySerial(5, 6);//RX,TX

// If using software SPI (the default case):
#define OLED_MOSI   9
#define OLED_CLK   10
#define OLED_DC    11
#define OLED_CS    12
#define OLED_RESET 13
Adafruit_SSD1306 display(OLED_MOSI, OLED_CLK, OLED_DC, OLED_RESET, OLED_CS);

/* Uncomment this block to use hardware SPI
#define OLED_DC     6
#define OLED_CS     7
#define OLED_RESET  8
Adafruit_SSD1306 display(OLED_DC, OLED_RESET, OLED_CS);
*/

#define NUMFLAKES 10
#define XPOS 0
#define YPOS 1
#define DELTAY 2
unsigned int vhh;// last hour stored
unsigned int vmm;// last minute stored
unsigned int vss;// last second stored
unsigned int pm;
unsigned int icall; // call sensor
unsigned int imsg;// msg sensor

unsigned int buttonstate; //button state to on/off lcd
const int pinButton = 2;
unsigned int lastButtonState= LOW;
unsigned int pushButtonToggle = HIGH;

const int motorPin=3;
const int speakerPin=4;

unsigned int rtoggle=0; // real time toggle

unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
unsigned long debounceDelay = 50;    // the debounce time; increase if the output flickers



#define LOGO16_GLCD_HEIGHT 16 
#define LOGO16_GLCD_WIDTH  16 
// adafruit logo or you can place yours using lcd assistant.
static const unsigned char PROGMEM logo16_glcd_bmp[] =
{ B00000000, B11000000,
  B00000001, B11000000,
  B00000001, B11000000,
  B00000011, B11100000,
  B11110011, B11100000,
  B11111110, B11111000,
  B01111110, B11111111,
  B00110011, B10011111,
  B00011111, B11111100,
  B00001101, B01110000,
  B00011011, B10100000,
  B00111111, B11100000,
  B00111111, B11110000,
  B01111100, B11110000,
  B01110000, B01110000,
  B00000000, B00110000 };

#if (SSD1306_LCDHEIGHT != 32)
#error("Height incorrect, please fix Adafruit_SSD1306.h!");
#endif

void setup()   {                
  mySerial.begin(38400);

  pinMode(pinButton,INPUT);
  pinMode(motorPin,OUTPUT);
  digitalWrite(motorPin,HIGH);
 
  
  // by default, we'll generate the high voltage from the 3.3v line internally! (neat!)
  display.begin(SSD1306_SWITCHCAPVCC);
  // init done
  
  // Show image buffer on the display hardware.
  // Since the buffer is intialized with an Adafruit splashscreen
  // internally, this will display the splashscreen.
  display.display();
  delay(2000);
  // Clear the buffer.
  display.clearDisplay();

 digitalWrite(motorPin,LOW);
 

  
  // draw a single pixel
  display.drawPixel(10, 10, WHITE);
  // Show the display buffer on the hardware.
  // NOTE: You _must_ call display after making any drawing commands
  // to make them visible on the display hardware!
  display.display();
  delay(2000);
  display.clearDisplay();

  // text display tests
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0,0);
  display.print("Hello, world!");
  display.display();
  delay(2000);
   display.clearDisplay();

}

void loop() {
  // put your main code here, to run repeatedly:

// loop when the device is not connected with bluetooth 
while(rtoggle!=0)
{
//**********increase the time for the real sync of watch ************
   if(vss>=60)
 {
   vmm++;
   vss=0;
 }
 if(vmm>=60)
 {
  vhh++;
  vmm=0;
 }
 if(vhh>12)
 {
  vhh=1;
 }

  // fetch the current time in the string 
  String hh = String(vhh);
  
  String mm = String(vmm);
  
  String ss = String(vss);

  /* read the input from the push button, to understand below code
   refer to the debounce code at 
   https://www.arduino.cc/en/Tutorial/Debounce
  */
  int reading = digitalRead(pinButton);
 
  if(reading!=lastButtonState)
  {
    lastDebounceTime = millis();
  }

  if((millis()-lastDebounceTime)>debounceDelay)
  {

     if(reading!=buttonstate)
     {
      buttonstate = reading;
        if(buttonstate==HIGH)
        {
          pushButtonToggle = !pushButtonToggle;
         // Serial.println("pushbuttontoggle");
        // Serial.println(pushButtonToggle);
        }
      
     }
  }

 lastButtonState = reading;
  // fetch the current am or pm 
  String spm;
  if(pm==1)
  {
    spm="PM";
  }
  else
  {
    spm="AM";
  }
  // club all the information into one string to display on screen.
  String myTime = hh+":"+mm+" "+spm;
  
  // if button is hold show the time
  if(lastButtonState==LOW)
  {
  display.setTextSize(2);
  display.setTextColor(WHITE);
  display.setCursor(20,8);
  display.println(myTime);
  display.display(); 
  delay(1000);
  vss++;
  }
  else // show nothing
  {
  display.display();    
  delay(1000);
  vss++;
  }
  display.clearDisplay();

  // if the device is connected in between change the value of rtoggle
  // and this will exit the current loop
  if(mySerial.available()>0)
  {
    rtoggle=0;
  }
}

// loop when the device is connected with the bluetooth
while(mySerial.available()>0){
 

  /* read the information from android app in the format
   * 05:07:54:am\01\0paras\070427xxxxx\01\070427xxxxx\0this is the demo text\0
   * be careful that \0 belongs to null in the above string received 
   * from the app
  */
  String hh =  mySerial.readStringUntil(':');
  
  String mm =  mySerial.readStringUntil(':');
  
  String ss =  mySerial.readStringUntil(':');

  String spm = mySerial.readStringUntil('\0');

  String call = mySerial.readStringUntil('\0');

  String iName = mySerial.readStringUntil('\0');

  String iNum = mySerial.readStringUntil('\0');

  String msg = mySerial.readStringUntil('\0');

  String mNum = mySerial.readStringUntil('\0');

  String mBody = mySerial.readStringUntil('\0');

 // convert the strings to integer values
  pm = spm.toInt(); 
  if(pm==1)
  {
    spm="PM";
  }
  else
  {
    spm="AM";
  }
  String myTime = hh+":"+mm+" "+spm;
  
  vhh = hh.toInt();
  vmm = mm.toInt();
  vss = ss.toInt();
  icall = call.toInt();
  imsg = msg.toInt();

  // again same as the above, read the input from button and use debounce
 int reading = digitalRead(pinButton);
 
  if(reading!=lastButtonState)
  {
    lastDebounceTime = millis();
  }

  if((millis()-lastDebounceTime)>debounceDelay)
  {

     if(reading!=buttonstate)
     {
      buttonstate = reading;
        if(buttonstate==HIGH)
        {
          pushButtonToggle = !pushButtonToggle;
         // Serial.println("pushbuttontoggle");
        // Serial.println(pushButtonToggle);
        }
      
     }
  }

 lastButtonState = reading;

// if value received from the connection is 1 
//then display the information of call
 if(icall==1)
  {
  display.clearDisplay();
  display.setTextSize(1.7);
  display.setTextColor(WHITE);
  display.setCursor(30,7);
  display.print(iName+" Calling");
  display.setCursor(20,17);
  display.print(iNum);
  Serial.println(iName+" Calling");
  display.display();
  delay(1000);
  digitalWrite(motorPin,HIGH);
  delay(500);
  digitalWrite(motorPin,LOW);
  delay(500);
  digitalWrite(motorPin,HIGH);
  delay(500);
  digitalWrite(motorPin,LOW);
  delay(500);
  }
  else if(imsg==1)// else if only message is received then display
  {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0,0);
  display.print(mNum);
  display.println(" Messaged");
  display.print(mBody);
  display.display();
  delay(1000);
  digitalWrite(motorPin,HIGH);
  delay(250);
  digitalWrite(motorPin,LOW);
  delay(250);
  digitalWrite(motorPin,HIGH);
  delay(250);
  digitalWrite(motorPin,LOW);
  delay(250);
  }
  // else if only the button is pressed to see the time
  else if(lastButtonState==LOW) 
  {
  display.setTextSize(2);
  display.setTextColor(WHITE);
  display.setCursor(20,8);
  display.print(myTime);
  display.display();
  delay(1000);
  }
  display.clearDisplay();
}

rtoggle = 1;

}
